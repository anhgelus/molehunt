package world.anhgelus.molehunt.client;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class MolehuntClient implements ClientModInitializer {

    public static final String MOD_ID = "molehunt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public final static GameProfile ANONYMOUS_PROFILE = new GameProfile(UUID.fromString("015f3266-4e0a-412e-9b80-1ca76af79453"), "Molehunt");

    @Override
    public void onInitializeClient() {
    }
}
