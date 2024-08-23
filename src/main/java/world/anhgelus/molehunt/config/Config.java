package world.anhgelus.molehunt.config;

import world.anhgelus.molehunt.Molehunt;

public class Config {
    public final int GAME_DURATION;
    public final double MOLE_PERCENTAGE;
    public final int MOLE_COUNT;

    public Config(String fileName) {
        final SimpleConfig CONFIG = SimpleConfig.of(fileName).provider(Config::defaultConfig).request();

        // In seconds
        GAME_DURATION = CONFIG.getOrDefault("game_duration", 90) * 60;
        MOLE_PERCENTAGE = CONFIG.getOrDefault("mole_percentage", 25);
        MOLE_COUNT = CONFIG.getOrDefault("mole_count", -1);
    }

    private static String defaultConfig(String s) {
        return """
                # Molehunt mod configuration file

                # The duration of a molehunt game, in minutes.
                # Default : 90 minutes (1 hour 30 minutes).
                game_duration = 90
                
                # Mole percentage.
                # For example, a mole percentage of 25% will get 1 mole every 4 players.
                # Default : 25 %.
                mole_percentage = 25
                
                # Mole count (absolute)
                # If set, this setting will overwrite the mole_percentage setting.
                # Default : not set
                #mole_count = 2
                """;
    }
}
