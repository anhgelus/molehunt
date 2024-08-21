package world.anhgelus.molehunt;

import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import world.anhgelus.molehunt.utils.TimeUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Game {

    final private Timer timer = new Timer();
    final public static int DEFAULT_TIME = 90*60; // 1:30
    private int remaining = DEFAULT_TIME;

    final private MinecraftServer server;

    final private List<ServerPlayerEntity> moles = new ArrayList<>();

    public Game(MinecraftServer server) {
        this.server = server;
    }

    public void start() {
        final int n = (server.getCurrentPlayerCount() - server.getCurrentPlayerCount() % 4)/4;
        final var playerManager = server.getPlayerManager();
        final var players = playerManager.getPlayerList();
        for (int i = 0; i < n; i++) {
            final var r = ThreadLocalRandom.current().nextInt(0, players.size());
            final var mole = players.get(r);
            if (mole == null) throw new IllegalStateException("Mole is null!");
            moles.add(mole);
            players.remove(r);
        }

        server.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES).set(false, server);
        server.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);

        final var title = new TitleS2CPacket(Text.of("You are..."));
        final var timing = new TitleFadeS2CPacket(20, 40, 20);
        playerManager.getPlayerList().forEach(p -> {
            p.networkHandler.sendPacket(timing);
            p.networkHandler.sendPacket(title);
        });
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                playerManager.getPlayerList().forEach(p -> {
                    p.networkHandler.sendPacket(timing);
                    if (moles.contains(p)) {
                        p.networkHandler.sendPacket(new TitleS2CPacket(Text.of("The Mole!")));
                        p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.of("get the list of moles with /molehunt moles")));
                    } else {
                        p.networkHandler.sendPacket(new TitleS2CPacket(Text.of("Not the Mole!")));
                    }
                });
                server.getOverworld().setTimeOfDay(0);
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        remaining--;
                        playerManager.sendToAll(new OverlayMessageS2CPacket(Text.of(getShortRemainingText())));
                        if (remaining == 0) {
                            end();
                        }
                    }
                }, 1000L, 1000L);
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        playerManager.broadcast(getRemainingText(), false);
                    }
                }, 10*60*1000L, 10*60*1000L);
            }
        }, 4*1000);
    }

    public void stop() {
        server.getPlayerManager().broadcast(Text.of("Game stopped"), false);
        end();
    }

    public void end() {
        timer.cancel();
        // affiche les gagnants
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

    public boolean isAMole(ServerPlayerEntity player) {
        return moles.contains(player);
    }

    public boolean gameFinished() {
        return new HashSet<>(moles).containsAll(server.getPlayerManager().getPlayerList());
    }
}
