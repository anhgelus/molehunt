package world.anhgelus.molehunt.game;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import net.minecraft.world.rule.GameRules;
import world.anhgelus.molehunt.Molehunt;
import world.anhgelus.molehunt.timer.TickTask;
import world.anhgelus.molehunt.timer.TimerAccess;
import world.anhgelus.molehunt.utils.TimeUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Game {

    public final int defaultTime = Molehunt.CONFIG.getGameDuration() * 60;
    private final MinecraftServer server;
    private final List<UUID> moles = new ArrayList<>();
    private final TitleFadeS2CPacket timing = new TitleFadeS2CPacket(20, 40, 20);
    private int remaining = defaultTime;
    private boolean started = false;

    public Game(MinecraftServer server) {
        this.server = server;
    }

    public void start() {
        final int n = Molehunt.CONFIG.getMoleCount() < 0
                ? Math.floorDiv(server.getCurrentPlayerCount(), Math.floorDiv(100, Molehunt.CONFIG.getMolePercentage()))
                : Molehunt.CONFIG.getMoleCount();

        final var playerManager = server.getPlayerManager();

        final var players = new ArrayList<>(playerManager.getPlayerList());
        for (int i = 0; i < n && !players.isEmpty(); i++) {
            final var r = ThreadLocalRandom.current().nextInt(0, players.size());
            final var mole = players.get(r);
            if (mole == null) throw new IllegalStateException("Mole is null!");
            moles.add(mole.getUuid());
            players.remove(r);
        }

        final var gamerules = server.getOverworld().getGameRules();
        // immutable gamerules
        gamerules.setValue(GameRules.SHOW_DEATH_MESSAGES, false, server);
        gamerules.setValue(GameRules.ANNOUNCE_ADVANCEMENTS, false, server);
        // gamerules for the start
        gamerules.setValue(GameRules.DO_IMMEDIATE_RESPAWN, true, server);

        final var timer = TimerAccess.getTimerFromOverworld(server);

        final var worldBorder = server.getOverworld().getWorldBorder();
        worldBorder.setSize(Molehunt.CONFIG.getInitialWorldSize());
        if (Molehunt.CONFIG.getBorderShrinkingStartingTimeOffset() < Molehunt.CONFIG.getGameDuration()) {
            timer.dds_runTask(new TickTask(() -> worldBorder.interpolateSize(
                    Molehunt.CONFIG.getInitialWorldSize(),
                    Molehunt.CONFIG.getFinalWorldSize(),
                    (Molehunt.CONFIG.getGameDuration() - Molehunt.CONFIG.getBorderShrinkingStartingTimeOffset()) * 60 * 20L,
                    0L
            ), Molehunt.CONFIG.getBorderShrinkingStartingTimeOffset() * 60 * 20L));
        }

        final var title = new TitleS2CPacket(Text.translatable("molehunt.game.start.suspense"));
        playerManager.getPlayerList().forEach(p -> {
            p.getInventory().clear();
            p.kill(p.getEntityWorld());
            p.networkHandler.sendPacket(timing);
            p.networkHandler.sendPacket(title);
            p.changeGameMode(GameMode.SURVIVAL);
            if (Molehunt.CONFIG.foodOnStart()) p.giveItemStack(new ItemStack(Items.COOKED_BEEF, 64));
        });

        server.setDefaultGameMode(GameMode.SPECTATOR);

        timer.dds_runTask(new TickTask(() -> {
            playerManager.getPlayerList().forEach(p -> {
                p.networkHandler.sendPacket(timing);
                if (moles.contains(p.getUuid())) {
                    p.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("molehunt.game.start.mole.title")));
                    p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("molehunt.game.start.mole.subtitle")));
                } else {
                    p.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("molehunt.game.start.survivor.title")));
                    p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("molehunt.game.start.survivor.subtitle")));
                }
                // reset health and food level
                p.setHealth(p.getMaxHealth());
                p.getHungerManager().setFoodLevel(20);
                p.getHungerManager().setSaturationLevel(5.0f);
            });
            // reset gamerules after the start
            gamerules.setValue(GameRules.DO_IMMEDIATE_RESPAWN, false, server);
            // reset time and weather
            server.getOverworld().setTimeOfDay(0);
            server.getOverworld().resetWeather();
            changeState(true);
            timer.dds_runTask(new TickTask(() -> {
                remaining--;
                playerManager.getPlayerList().forEach(player -> {
                    if (Molehunt.timerVisibility.getOrDefault(player.getUuid(), true)) {
                        player.networkHandler.sendPacket(new OverlayMessageS2CPacket(Text.of(getRemainingText())));
                    }
                });
                playerManager.sendToAll(timing);
                if (remaining == 0) end();
            }, 5 * 20, 20));
        }, 4 * 20));
    }

    public void stop() {
        server.getPlayerManager().broadcast(Text.translatable("commands.molehunt.stop.success"), false);
        end();
    }

    public void end() {
        final var timer = TimerAccess.getTimerFromOverworld(server);
        timer.dds_cancel();

        final var worldBorder = server.getOverworld().getWorldBorder();
        // Stops the border shrinking.
        worldBorder.setSize(worldBorder.getSize());

        changeState(false);
        final var pm = server.getPlayerManager();
        final var winnerSuspense = new TitleS2CPacket(Text.translatable("molehunt.game.end.suspense.title"));
        pm.getPlayerList().forEach(p -> {
            p.networkHandler.sendPacket(timing);
            p.networkHandler.sendPacket(winnerSuspense);
            p.changeGameMode(GameMode.CREATIVE);
        });
        timer.dds_runTask(new TickTask(() -> {
            TitleS2CPacket winner;
            if (wonByMoles()) {
                winner = new TitleS2CPacket(Text.translatable("molehunt.game.end.winners.moles.title"));
            } else {
                winner = new TitleS2CPacket(Text.translatable("molehunt.game.end.winners.survivors.title"));
            }
            pm.sendToAll(new SubtitleS2CPacket(Text.translatable("molehunt.game.end.winners.subtitle", getMolesAsString())));
            pm.sendToAll(winner);
            pm.sendToAll(timing);
            moles.clear();
        }, 4 * 20));
    }

    public Text getRemainingText() {
        return Text.of("§c" + TimeUtils.generateShortString(remaining));
    }

    public List<ServerPlayerEntity> getMoles() {
        return moles.stream()
                .map(uuid -> server.getPlayerManager().getPlayer(uuid))
                .filter(Objects::nonNull)
                .filter(p -> !p.isSpectator())
                .toList();
    }

    public String getMolesAsString() {
        return getMoles().stream()
                .map(PlayerEntity::getDisplayName)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    public boolean isMole(ServerPlayerEntity player) {
        return moles.contains(player.getUuid());
    }

    public boolean wonByMoles() {
        return new HashSet<>(moles).containsAll(
                server.getPlayerManager()
                        .getPlayerList()
                        .stream()
                        .filter(p -> !p.isSpectator() && !p.isCreative())
                        .map(Entity::getUuid)
                        .toList()
        );
    }

    public boolean started() {
        return started;
    }

    private void changeState(boolean hasStarted) {
        started = hasStarted;
        final var payload = new GamePayload(hasStarted);
        server.getPlayerManager().getPlayerList().forEach(p -> ServerPlayNetworking.send(p, payload));
    }
}
