package world.anhgelus.molehunt;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Molehunt implements ModInitializer {

    public static final String MOD_ID = "molehunt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Molehunt");
    }
}
