package world.anhgelus.molehunt.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerModelPart;
import world.anhgelus.molehunt.config.ConfigPayload;
import world.anhgelus.molehunt.game.GamePayload;

public class MolehuntClient implements ClientModInitializer {

    private static boolean SHOW_SKINS = false;
    private static boolean SHOW_NAMETAGS = false;
    private static boolean SHOW_TAB = false;

    private static boolean GAME_STARTED = false;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.ID, (payload, context) -> context.client().execute(() -> {
            SHOW_SKINS = payload.showSkins();
            SHOW_NAMETAGS = payload.showNametags();
            SHOW_TAB = payload.showTab();
        }));
        ClientPlayNetworking.registerGlobalReceiver(GamePayload.ID, (payload, context) -> context.client().execute(() -> GAME_STARTED = payload.gameLaunched()));

        // Needed because else `client.options` is null
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            var options = client.options;

            options.setPlayerModelPart(PlayerModelPart.CAPE, true);
            options.setPlayerModelPart(PlayerModelPart.HAT, true);
            options.setPlayerModelPart(PlayerModelPart.JACKET, true);
            options.setPlayerModelPart(PlayerModelPart.LEFT_SLEEVE, true);
            options.setPlayerModelPart(PlayerModelPart.RIGHT_SLEEVE, true);
            options.setPlayerModelPart(PlayerModelPart.LEFT_PANTS_LEG, true);
            options.setPlayerModelPart(PlayerModelPart.RIGHT_PANTS_LEG, true);
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

    public static boolean gameStarted() {
        return GAME_STARTED;
    }
}
