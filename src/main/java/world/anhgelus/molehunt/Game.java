package world.anhgelus.molehunt;

import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import world.anhgelus.molehunt.utils.TimeUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Game {

    private Timer timer = new Timer();
    public final int defaultTime = Molehunt.CONFIG.getGameDuration()*60;
    private int remaining = defaultTime;

    private Timer borderTimeOffsetTimer = new Timer();
    private final TimerTask borderTimeOffsetTask = new TimerTask() {
        @Override
        public void run() {
            final var worldBorder = server.getOverworld().getWorldBorder();
            worldBorder.interpolateSize(
                    Molehunt.CONFIG.getInitialWorldSize(),
                    Molehunt.CONFIG.getFinalWorldSize(),
                    (long) (Molehunt.CONFIG.getGameDuration() - Molehunt.CONFIG.getBorderShrinkingStartingTimeOffset()) * 60);
        }
    };

    private final MinecraftServer server;

    private final List<ServerPlayerEntity> moles = new ArrayList<>();

    private final TitleFadeS2CPacket timing = new TitleFadeS2CPacket(20, 40, 20);

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
            moles.add(mole);
            players.remove(r);
        }

        final var gamerules = server.getGameRules();
        // immutable gamerules
        gamerules.get(GameRules.SHOW_DEATH_MESSAGES).set(false, server);
        gamerules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);
        // gamerules for the start
        gamerules.get(GameRules.DO_IMMEDIATE_RESPAWN).set(true, server);
        gamerules.get(GameRules.DO_ENTITY_DROPS).set(false, server);

        final var worldBorder = server.getOverworld().getWorldBorder();
        worldBorder.setSize(Molehunt.CONFIG.getInitialWorldSize());
        if (Molehunt.CONFIG.getBorderShrinkingStartingTimeOffset() < Molehunt.CONFIG.getGameDuration()) {
            borderTimeOffsetTimer.schedule(borderTimeOffsetTask, (long) Molehunt.CONFIG.getBorderShrinkingStartingTimeOffset() * 60 * 1000);
        }

        final var title = new TitleS2CPacket(Text.translatable("molehunt.game.start.suspense"));
        playerManager.getPlayerList().forEach(p -> {
            p.kill();
            p.networkHandler.sendPacket(timing);
            p.networkHandler.sendPacket(title);
            p.changeGameMode(GameMode.SURVIVAL);
        });

        server.setDefaultGameMode(GameMode.SPECTATOR);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                playerManager.getPlayerList().forEach(p -> {
                    p.networkHandler.sendPacket(timing);
                    if (moles.contains(p)) {
                        p.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("molehunt.game.start.mole.title")));
                        p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.translatable("molehunt.game.start.mole.subtitle")));
                    } else {
                        p.networkHandler.sendPacket(new TitleS2CPacket(Text.translatable("molehunt.game.start.survivor")));
                    }
                    // reset health and food level
                    p.setHealth(p.getMaxHealth());
                    p.getHungerManager().setFoodLevel(20);
                    p.getHungerManager().setSaturationLevel(5.0f);
                });
                // reset gamerules after the start
                gamerules.get(GameRules.DO_IMMEDIATE_RESPAWN).set(false, server);
                gamerules.get(GameRules.DO_ENTITY_DROPS).set(true, server);
                // reset time and weather
                server.getOverworld().setTimeOfDay(0);
                server.getOverworld().resetWeather();
                started = true;

                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        remaining--;
                        playerManager.getPlayerList().forEach(player -> {
                            if (Molehunt.timerVisibility.getOrDefault(player, true)) {
                                player.networkHandler.sendPacket(new OverlayMessageS2CPacket(Text.of(getShortRemainingText())));
                            }
                        });
                        playerManager.sendToAll(timing);
                        if (remaining == 0) end();
                    }
                }, 5*1000, 1000);
            }
        }, 4*1000);
    }

    public void stop() {
        server.getPlayerManager().broadcast(Text.translatable("commands.molehunt.stop.success"), false);
        end();
    }

    public void end() {
        timer.cancel();
        timer = new Timer();

        borderTimeOffsetTimer.cancel();
        borderTimeOffsetTimer = new Timer();

        final var worldBorder = server.getOverworld().getWorldBorder();
        // Stops the border shrinking.
        worldBorder.setSize(worldBorder.getSize());

        started = false;
        final var pm = server.getPlayerManager();
        final var winnerSuspense = new TitleS2CPacket(Text.translatable("molehunt.game.end.suspense.title"));
        pm.getPlayerList().forEach(p -> {
            p.networkHandler.sendPacket(timing);
            p.networkHandler.sendPacket(winnerSuspense);
            p.changeGameMode(GameMode.CREATIVE);
        });
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                TitleS2CPacket winner;
                if (gameWonByMoles()) {
                    winner = new TitleS2CPacket(Text.translatable("molehunt.game.end.winners.moles.title"));
                } else {
                    winner = new TitleS2CPacket(Text.translatable("molehunt.game.end.winners.survivors.title"));
                }
                pm.sendToAll(new SubtitleS2CPacket(Text.translatable("molehunt.game.end.winners.subtitle")
                        .append(" " + getMolesAsString()))
                );
                pm.sendToAll(winner);
                pm.sendToAll(timing);
                moles.clear();
            }
        }, 4*1000);
    }

    public Text getShortRemainingText() {
        return Text.of("§c" + TimeUtils.printShortTime(remaining));
    }

    public List<ServerPlayerEntity> getMoles() {
        return moles;
    }

    public String getMolesAsString() {
        return moles.stream()
                .map(ServerPlayerEntity::getDisplayName)
                .filter(Objects::nonNull)
                .map(Text::getString)
                .collect(Collectors.joining(", "));
    }

    public boolean isAMole(ServerPlayerEntity player) {
        return moles.contains(player);
    }

    public boolean gameWonByMoles() {
        return new HashSet<>(moles).containsAll(server.getPlayerManager().getPlayerList());
    }

    public void updateMole(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer) {
        moles.remove(oldPlayer);
        moles.add(newPlayer);
    }

    public boolean hasStarted() {
        return started;
    }
}
