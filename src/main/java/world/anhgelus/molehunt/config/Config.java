package world.anhgelus.molehunt.config;

public class Config {
    public final int gameDuration;
    public final double molePercentage;
    public final int moleCount;
    public final boolean showNametags;
    public final boolean showTab;
    public final boolean showSkins;

    public Config(String fileName) {
        final SimpleConfig CONFIG = SimpleConfig.of(fileName).provider(Config::defaultConfig).request();

        // In seconds
        gameDuration = CONFIG.getOrDefault("game_duration", 90) * 60;
        molePercentage = CONFIG.getOrDefault("mole_percentage", 25);
        moleCount = CONFIG.getOrDefault("mole_count", -1);
        // bool
        showNametags = CONFIG.getOrDefault("show_nametags", false);
        showSkins = CONFIG.getOrDefault("show_skins", false);
        showTab = CONFIG.getOrDefault("show_tab", false);
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
