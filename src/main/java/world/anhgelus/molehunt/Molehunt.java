package world.anhgelus.molehunt;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.anhgelus.molehunt.config.Config;
import world.anhgelus.molehunt.config.ConfigPayload;
import world.anhgelus.molehunt.config.SimpleConfig;
import world.anhgelus.molehunt.game.Game;
import world.anhgelus.molehunt.game.GamePayload;

import java.util.HashMap;

import static net.minecraft.server.command.CommandManager.literal;


public class Molehunt implements ModInitializer {

    public static final String MOD_ID = "molehunt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Config CONFIG;

    public static final SimpleConfig CONFIG_FILE = Config.configFile(MOD_ID);

    public static final GameRules.Key<GameRules.IntRule> GAME_DURATION = GameRuleRegistry.register(
            MOD_ID +":gameDurationMinutes",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(CONFIG_FILE.getOrDefault("game_duration", 90))
    );
    public static final GameRules.Key<GameRules.IntRule> MOLE_PERCENTAGE = GameRuleRegistry.register(
            MOD_ID +":molePercentage",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(CONFIG_FILE.getOrDefault("mole_percentage", 25))
    );
    public static final GameRules.Key<GameRules.IntRule> MOLE_COUNT = GameRuleRegistry.register(
            MOD_ID +":moleCount",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(CONFIG_FILE.getOrDefault("mole_count", -1))
    );
    public static final GameRules.Key<GameRules.BooleanRule> SHOW_NAMETAGS = GameRuleRegistry.register(
            MOD_ID +":showNametags",
            GameRules.Category.MISC,
            GameRuleFactory.createBooleanRule(CONFIG_FILE.getOrDefault("show_nametags", false), (server, val) -> {
                if (CONFIG == null) return;
                CONFIG.sendConfigPayload();
            })
    );
    public static final GameRules.Key<GameRules.BooleanRule> SHOW_TAB = GameRuleRegistry.register(
            MOD_ID +":showTab"
            , GameRules.Category.MISC,
            GameRuleFactory.createBooleanRule(CONFIG_FILE.getOrDefault("show_tab", false), (server, val) -> {
                if (CONFIG == null) return;
                CONFIG.sendConfigPayload();
            })
    );
    public static final GameRules.Key<GameRules.BooleanRule> SHOW_SKINS = GameRuleRegistry.register(
            MOD_ID +":showSkins",
            GameRules.Category.MISC,
            GameRuleFactory.createBooleanRule(CONFIG_FILE.getOrDefault("show_skins", false), (server, val) -> {
                if (CONFIG == null) return;
                CONFIG.sendConfigPayload();
            })
    );
    public static final GameRules.Key<GameRules.IntRule> INITIAL_WORLD_SIZE = GameRuleRegistry.register(
            MOD_ID +":initialWorldSize",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(CONFIG_FILE.getOrDefault("initial_world_size", 200), 0)
    );
    public static final GameRules.Key<GameRules.IntRule> FINAL_WORLD_SIZE = GameRuleRegistry.register(
            MOD_ID +":finalWorldSize",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(CONFIG_FILE.getOrDefault("final_world_size", 50), 0)
    );
    public static final GameRules.Key<GameRules.IntRule> MOVING_STARTING_TIME_OFFSET = GameRuleRegistry.register(
            MOD_ID +":borderMovingStartingTimeOffsetMinutes",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(CONFIG_FILE.getOrDefault("border_moving_starting_time_offset", 10), 0)
    );

    public Game game;

    public static HashMap<ServerPlayerEntity, Boolean> timerVisibility = new HashMap<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Molehunt");

        final var command = literal("molehunt");
        command.then(literal("start").requires(source -> source.hasPermissionLevel(1)).executes(context -> {
            game = new Game(context.getSource().getServer());
            game.start();
            return Command.SINGLE_SUCCESS;
        }));
        command.then(literal("timer").requires(ServerCommandSource::isExecutedByPlayer).then(
                literal("show").executes(context -> {
                    timerVisibility.put(context.getSource().getPlayer(), true);
                    context.getSource().sendFeedback(() -> Text.translatable("commands.molehunt.timer.show"), false);

                    var player = context.getSource().getPlayer();
                    assert player != null;

                    if (game == null || !game.hasStarted()) {
                        player.networkHandler.sendPacket(new OverlayMessageS2CPacket(
                            Text.translatable("commands.molehunt.stop.failed").setStyle(Style.EMPTY.withColor(16733525))
                        ));
                    } else {
                        player.networkHandler.sendPacket(new OverlayMessageS2CPacket(Text.of(game.getShortRemainingText())));
                    }

                    return Command.SINGLE_SUCCESS;
                })
        ).then(
                literal("hide").executes(context -> {
                    timerVisibility.put(context.getSource().getPlayer(), false);
                    context.getSource().sendFeedback(() -> Text.translatable("commands.molehunt.timer.hide"), false);
                    return Command.SINGLE_SUCCESS;
                })
        ));
        command.then(literal("moles").requires(source -> game != null && game.isAMole(source.getPlayer())).executes(context -> {
            context.getSource().sendFeedback(() -> Text.translatable("commands.molehunt.moles.list").append(" " + game.getMolesAsString()),false);
            return Command.SINGLE_SUCCESS;
        }));
        command.then(literal("stop").requires(source -> source.hasPermissionLevel(1)).executes(context -> {
            if (game == null || !game.hasStarted()) {
                throw (new SimpleCommandExceptionType(Text.translatable("commands.molehunt.stop.failed"))).create();
            }

            game.stop();

            return Command.SINGLE_SUCCESS;
        }));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> CONFIG = new Config(server));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(command));

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> false);

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity) || game == null) return;
            if (!game.hasStarted()) return;
            if (game.gameWonByMoles()) game.end();
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (game == null) return;
            if (!game.hasStarted()) return;
            if (game.getMoles().contains(oldPlayer)) game.updateMole(oldPlayer, newPlayer);
            newPlayer.changeGameMode(GameMode.SPECTATOR);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayNetworking.send(
                    handler.player,
                    new ConfigPayload(CONFIG.areNametagsEnabled(), CONFIG.areSkinsEnabled(), CONFIG.isTabEnabled())
            );
            ServerPlayNetworking.send(
                    handler.player,
                    new GamePayload(game != null && game.hasStarted())
            );
        });

        PayloadTypeRegistry.playS2C().register(ConfigPayload.ID, ConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GamePayload.ID, GamePayload.CODEC);
    }
}
