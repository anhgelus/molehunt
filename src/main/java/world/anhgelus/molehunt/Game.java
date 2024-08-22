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
    public final static int DEFAULT_TIME = 90*60; // 1:30
    private int remaining = DEFAULT_TIME;

    private final MinecraftServer server;

    private final List<ServerPlayerEntity> moles = new ArrayList<>();

    private final TitleFadeS2CPacket timing = new TitleFadeS2CPacket(20, 40, 20);

    private boolean started = false;

    public Game(MinecraftServer server) {
        this.server = server;
    }

    public void start() {
        if (started) return;
        final int n = (server.getCurrentPlayerCount() - server.getCurrentPlayerCount() % 4)/4;
        final var playerManager = server.getPlayerManager();
        final var players = new ArrayList<>(playerManager.getPlayerList());
        for (int i = 0; i < n; i++) {
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

        final var title = new TitleS2CPacket(Text.of("§eYou are..."));
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
                        p.networkHandler.sendPacket(new TitleS2CPacket(Text.of("§cThe Mole!")));
                        p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.of("§6get the list of moles with /molehunt moles")));
                    } else {
                        p.networkHandler.sendPacket(new TitleS2CPacket(Text.of("§aNot the Mole!")));
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
                        playerManager.sendToAll(timing);
                        playerManager.sendToAll(new OverlayMessageS2CPacket(Text.of(getShortRemainingText())));
                        if (remaining == 0) {
                            end();
                        }
                    }
                }, 4*1000, 1000);
            }
        }, 4*1000);
    }

    public void stop() {
        server.getPlayerManager().broadcast(Text.of("Game stopped"), false);
        end();
    }

    public void end() {
        timer.cancel();
        timer = new Timer();
        started = false;
        final var pm = server.getPlayerManager();
        final var winnerSuspense = new TitleS2CPacket(Text.of("§eAnd the winners are..."));
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
                    winner = new TitleS2CPacket(Text.of("§cThe Moles!"));
                } else {
                    winner = new TitleS2CPacket(Text.of("§aNot the Mole!"));
                }
                pm.sendToAll(new SubtitleS2CPacket(Text.of("§6Moles were " + getMolesAsString())));
                pm.sendToAll(winner);
                pm.sendToAll(timing);
            }
        }, 4*1000);
    }

    public int getRemaining() {
        return remaining;
    }

    public Text getRemainingText() {
        return Text.of("Time remaining: "+ TimeUtils.printTime(remaining));
    }

    public Text getShortRemainingText() {
        return Text.of("§c" + TimeUtils.printShortTime(remaining));
    }

    public List<ServerPlayerEntity> getMoles() {
        return moles;
    }

    public String getMolesAsString() {
        return moles.stream().map(ServerPlayerEntity::getDisplayName).filter(Objects::nonNull).map(Text::toString).collect(Collectors.joining(", "));
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

    public boolean isStarted() {
        return started;
    }
}
