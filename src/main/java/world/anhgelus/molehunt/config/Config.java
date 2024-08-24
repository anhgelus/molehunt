package world.anhgelus.molehunt.config;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import world.anhgelus.molehunt.Molehunt;

public class Config {

    private final MinecraftServer server;

    public Config(MinecraftServer server) {
        this.server = server;

        sendConfigPayload(areNametagsEnabled(), areSkinsEnabled(), isTabEnabled());
    }

    public void sendConfigPayload() {
        final var payload = new ConfigPayload(areNametagsEnabled(), areSkinsEnabled(), isTabEnabled());
        server.getPlayerManager().getPlayerList().forEach(p -> ServerPlayNetworking.send(p, payload));
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

    public int getInitialWorldSize() {
        return server.getGameRules().getInt(Molehunt.INITIAL_WORLD_SIZE);
    }

    public int getFinalWorldSize() {
        return server.getGameRules().getInt(Molehunt.FINAL_WORLD_SIZE);
    }

    public int getBorderShrinkingStartingTimeOffset() {
        return server.getGameRules().getInt(Molehunt.MOVING_STARTING_TIME_OFFSET);
    }

    public static SimpleConfig configFile(String fileName) {
        return SimpleConfig.of(fileName).provider(Config::defaultConfig).request();
    }

    private static String defaultConfig(String s) {
        return """
                # Molehunt mod configuration file
                # To regenerate the default configuration, delete, move or rename this file.

                # Game settings
                
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
                
                
                # Client-side settings (applies to all players)
                
                # Show nametags
                # Default: false
                show_nametags = false
                
                # Show skins
                # Default: false
                show_skins = false
                
                # Show tab
                # Default: false
                show_tab = false
                
                
                # World border settings :
                
                # Initial world size (in blocks).
                # Default: 200 blocks.
                initial_world_size = 200
                
                # Final world size (in blocks).
                # Default: 50 blocks.
                final_world_size = 50
                
                # Moving starting time offset (in minutes)
                # The time before starting to move the world borders.
                # If this value is greater than the game duration, borders will never move.
                # Default: 10 minutes.
                border_moving_starting_time_offset = 10
                """;
    }
}
