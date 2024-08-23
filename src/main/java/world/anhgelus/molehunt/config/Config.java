package world.anhgelus.molehunt.config;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import world.anhgelus.molehunt.Molehunt;

public class Config {

    private final MinecraftServer server;

    public Config(String fileName, MinecraftServer server) {
        final SimpleConfig CONFIG = SimpleConfig.of(fileName).provider(Config::defaultConfig).request();

        this.server = server;

        final var rules = server.getGameRules();

        // In seconds
        final var gameDuration = CONFIG.getOrDefault("game_duration", 90) * 60;
        rules.get(Molehunt.GAME_DURATION).set(gameDuration/60, server);
        final var molePercentage = CONFIG.getOrDefault("mole_percentage", 25);
        rules.get(Molehunt.MOLE_PERCENTAGE).set(molePercentage, server);
        final var moleCount = CONFIG.getOrDefault("mole_count", -1);
        rules.get(Molehunt.MOLE_COUNT).set(moleCount, server);
        // bool
        final var showNametags = CONFIG.getOrDefault("show_nametags", false);
        rules.get(Molehunt.SHOW_NAMETAGS).set(showNametags, server);
        final var showSkins = CONFIG.getOrDefault("show_skins", false);
        rules.get(Molehunt.SHOW_SKINS).set(showSkins, server);
        final var showTab = CONFIG.getOrDefault("show_tab", false);
        rules.get(Molehunt.SHOW_TAB).set(showTab, server);

        sendConfigPayload(showNametags, showSkins, showTab);
    }

    public void sendConfigPayload() {
        final var payload = new ConfigPayload(areNametagsEnabled(), areSkinsEnabled(), isTabEnabled());
        server.getPlayerManager().getPlayerList().forEach(p -> {
            ServerPlayNetworking.send(p, payload);
        });
    }

    public void sendConfigPayload(boolean showNametags, boolean showSkins, boolean showTab) {
        final var payload = new ConfigPayload(showNametags, showSkins, showTab);
        server.getPlayerManager().getPlayerList().forEach(p -> ServerPlayNetworking.send(p, payload));
    }

    public int getGameDuration() {
        return server.getGameRules().getInt(Molehunt.GAME_DURATION);
    }

    public int getMolePercentage() {
        return server.getGameRules().getInt(Molehunt.MOLE_PERCENTAGE);
    }

    public int getMoleCount() {
        return server.getGameRules().getInt(Molehunt.MOLE_COUNT);
    }

    public boolean areNametagsEnabled() {
        return server.getGameRules().getBoolean(Molehunt.SHOW_NAMETAGS);
    }

    public boolean areSkinsEnabled() {
        return server.getGameRules().getBoolean(Molehunt.SHOW_SKINS);
    }

    public boolean isTabEnabled() {
        return server.getGameRules().getBoolean(Molehunt.SHOW_TAB);
    }

    private static String defaultConfig(String s) {
        return """
                # Molehunt mod configuration file

                # The duration of a molehunt game, in minutes.
                # Default: 90 minutes (1 hour 30 minutes).
                game_duration = 90
                
                # Mole percentage.
                # For example, a mole percentage of 25% will get 1 mole every 4 players.
                # Default: 25 %.
                mole_percentage = 25
                
                # Mole count (absolute).
                # This setting will overwrite the mole_percentage setting.
                # If set below 0, this setting is disabled.
                # Default: -1.
                mole_count = -1
                
                # Show nametags
                # Default: false
                show_nametags = false
                
                # Show skins
                # Default: false
                show_skins = false
                
                # Show tab
                # Default: false
                show_tab = false
                """;
    }
}
