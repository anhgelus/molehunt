package world.anhgelus.molehunt.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import world.anhgelus.molehunt.config.ConfigPayload;

public class MolehuntClient implements ClientModInitializer {

    private static boolean SHOW_SKINS = false;
    private static boolean SHOW_NAMETAGS = false;
    private static boolean SHOW_TAB = false;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.ID, (payload, context) -> {
            try (final var client = context.client()) {
                client.execute(() -> {
                    SHOW_SKINS = payload.showSkins();
                    SHOW_NAMETAGS = payload.showNametags();
                    SHOW_TAB = payload.showTab();
                });
            }
        });
    }

    public static boolean showSkins() {
        return SHOW_SKINS;
    }

    public static boolean showNameTags() {
        return SHOW_NAMETAGS;
    }

    public static boolean showTab() {
        return SHOW_TAB;
    }
}
