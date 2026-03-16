package world.anhgelus.molehunt.game;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;
import world.anhgelus.molehunt.Molehunt;
import world.anhgelus.molehunt.timer.TickTask;
import world.anhgelus.molehunt.timer.TimerAccess;
import world.anhgelus.molehunt.utils.TimeUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Game {

    public final int DEFAULT_TIME = Molehunt.CONFIG.getGameDuration() * 60;
    private final MinecraftServer server;
    private final List<UUID> moles = new ArrayList<>();
    private final ClientboundSetTitlesAnimationPacket timing = new ClientboundSetTitlesAnimationPacket(20, 40, 20);
    private boolean started = false;
    private int remaining = DEFAULT_TIME;

    public Game(MinecraftServer server) {
        this.server = server;
    }

    public void start() {
        final int n = Molehunt.CONFIG.getMoleCount() < 0
                ? Math.floorDiv(server.getPlayerCount(), Math.floorDiv(100, Molehunt.CONFIG.getMolePercentage()))
                : Molehunt.CONFIG.getMoleCount();

        final var playerManager = server.getPlayerList();

        final var players = new ArrayList<>(playerManager.getPlayers());
        for (int i = 0; i < n && !players.isEmpty(); i++) {
            final var r = ThreadLocalRandom.current().nextInt(0, players.size());
            final var mole = players.get(r);
            moles.add(mole.getUUID());
            players.remove(r);
        }

        final var gamerules = server.overworld().getGameRules();
        // immutable gamerules
        gamerules.set(GameRules.SHOW_DEATH_MESSAGES, false, server);
        gamerules.set(GameRules.SHOW_ADVANCEMENT_MESSAGES, false, server);
        // gamerules for the start
        gamerules.set(GameRules.IMMEDIATE_RESPAWN, true, server);

        final var timer = TimerAccess.getTimerFromOverworld(server);

        final var worldBorder = server.overworld().getWorldBorder();
        worldBorder.setSize(Molehunt.CONFIG.getInitialWorldSize());
        if (Molehunt.CONFIG.getBorderShrinkingStartingTimeOffset() < Molehunt.CONFIG.getGameDuration()) {
            timer.dds_runTask(new TickTask(() -> worldBorder.lerpSizeBetween(
                    Molehunt.CONFIG.getInitialWorldSize(),
                    Molehunt.CONFIG.getFinalWorldSize(),
                    (long) (Molehunt.CONFIG.getGameDuration() - Molehunt.CONFIG.getBorderShrinkingStartingTimeOffset()) * 60 * 1000,
                    0L
            ), (long) Molehunt.CONFIG.getBorderShrinkingStartingTimeOffset() * 60 * 1000));
        }

        final var title = new ClientboundSetTitleTextPacket(Component.translatable("molehunt.game.start.suspense"));
        playerManager.getPlayers().forEach(p -> {
            p.getInventory().clearContent();
            p.kill(p.level());
            p.connection.send(timing);
            p.connection.send(title);
            p.setGameMode(GameType.SURVIVAL);
            if (Molehunt.CONFIG.foodOnStart()) p.addItem(new ItemStack(Items.COOKED_BEEF, 64));
        });

        server.setDefaultGameType(GameType.SPECTATOR);

        timer.dds_runTask(new TickTask(() -> {
            playerManager.getPlayers().forEach(p -> {
                p.connection.send(timing);
                if (moles.contains(p.getUUID())) {
                    p.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("molehunt.game.start.mole.title")));
                    p.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("molehunt.game.start.mole.subtitle")));
                } else {
                    p.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("molehunt.game.start.survivor.title")));
                    p.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("molehunt.game.start.survivor.subtitle")));
                }
                // reset health and food level
                p.setHealth(p.getMaxHealth());
                p.getFoodData().setFoodLevel(20);
                p.getFoodData().setSaturation(5.0f);
            });
            // reset gamerules after the start
            gamerules.set(GameRules.IMMEDIATE_RESPAWN, false, server);
            // reset time and weather
            final var overworld = server.overworld();
            server.clockManager()
                    .setTotalTicks(overworld.getLevel().dimensionType().defaultClock().orElseThrow(), 0);
            overworld.resetWeatherCycle();
            changeState(true);
            timer.dds_runTask(new TickTask(() -> {
                remaining--;
                playerManager.getPlayers().forEach(player -> {
                    if (Molehunt.timerVisibility.getOrDefault(player.getUUID(), true)) {
                        player.connection.send(new ClientboundSetActionBarTextPacket(Component.translationArg(getRemainingText())));
                    }
                });
                playerManager.broadcastAll(timing);
                if (remaining == 0) end();
            }, 5 * 1000, 1000));
        }, 4 * 1000));
    }

    public void stop() {
        server.getPlayerList().broadcastSystemMessage(Component.translatable("commands.molehunt.stop.success"), false);
        end();
    }

    public void end() {
        final var timer = TimerAccess.getTimerFromOverworld(server);
        timer.dds_cancel();

        final var worldBorder = server.overworld().getWorldBorder();
        // Stops the border shrinking.
        worldBorder.setSize(worldBorder.getSize());

        changeState(false);
        final var pm = server.getPlayerList();
        final var winnerSuspense = new ClientboundSetTitleTextPacket(Component.translatable("molehunt.game.end.suspense.title"));
        pm.getPlayers().forEach(p -> {
            p.connection.send(timing);
            p.connection.send(winnerSuspense);
            p.setGameMode(GameType.CREATIVE);
        });

        timer.dds_runTask(new TickTask(() -> {
            ClientboundSetTitleTextPacket winner;
            if (wonByMoles()) {
                winner = new ClientboundSetTitleTextPacket(Component.translatable("molehunt.game.end.winners.moles.title"));
            } else {
                winner = new ClientboundSetTitleTextPacket(Component.translatable("molehunt.game.end.winners.survivors.title"));
            }
            pm.broadcastAll(new ClientboundSetSubtitleTextPacket(Component.translatable("molehunt.game.end.winners.subtitle", getMolesAsString())));
            pm.broadcastAll(winner);
            pm.broadcastAll(timing);
            moles.clear();
        }, 4 * 1000));
    }

    public Component getRemainingText() {
        return Component.nullToEmpty("§c" + TimeUtils.generateShortString(remaining));
    }

    public List<ServerPlayer> getMoles() {
        return moles.stream()
                .map(uuid -> server.getPlayerList().getPlayer(uuid))
                .filter(Objects::nonNull)
                .filter(p -> !p.isSpectator())
                .toList();
    }

    public String getMolesAsString() {
        return getMoles().stream()
                .map(Player::getDisplayName)
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    public boolean isMole(ServerPlayer player) {
        return moles.contains(player.getUUID());
    }

    public boolean wonByMoles() {
        return new HashSet<>(moles).containsAll(
                server.getPlayerList()
                        .getPlayers()
                        .stream()
                        .filter(p -> !p.isSpectator() && !p.isCreative())
                        .map(Entity::getUUID)
                        .toList()
        );
    }

    public boolean started() {
        return started;
    }

    private void changeState(boolean hasStarted) {
        started = hasStarted;
        final var payload = new GamePayload(hasStarted);
        server.getPlayerList().getPlayers().forEach(p -> ServerPlayNetworking.send(p, payload));
    }
}
