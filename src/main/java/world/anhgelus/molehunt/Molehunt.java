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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
            GameRuleFactory.createIntRule(CONFIG_FILE.getOrDefault("initial_world_size", 600), 0)
    );
    public static final GameRules.Key<GameRules.IntRule> FINAL_WORLD_SIZE = GameRuleRegistry.register(
            MOD_ID +":finalWorldSize",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(CONFIG_FILE.getOrDefault("final_world_size", 100), 0)
    );
    public static final GameRules.Key<GameRules.IntRule> MOVING_STARTING_TIME_OFFSET = GameRuleRegistry.register(
            MOD_ID +":borderMovingStartingTimeOffsetMinutes",
            GameRules.Category.MISC,
            GameRuleFactory.createIntRule(CONFIG_FILE.getOrDefault("border_moving_starting_time_offset", 30), 0)
    );
    public static final GameRules.Key<GameRules.BooleanRule> ENABLE_PORTALS = GameRuleRegistry.register(
            MOD_ID +":enablePortals",
            GameRules.Category.MISC,
            GameRuleFactory.createBooleanRule(CONFIG_FILE.getOrDefault("enable_portals", false))
    );
    public static final GameRules.Key<GameRules.BooleanRule> FOOD_ON_START = GameRuleRegistry.register(
            MOD_ID +":foodOnStart",
            GameRules.Category.MISC,
            GameRuleFactory.createBooleanRule(CONFIG_FILE.getOrDefault("food_on_start", true))
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

                    if (game == null || !game.started()) {
                        player.networkHandler.sendPacket(new OverlayMessageS2CPacket(
                            Text.translatable("commands.molehunt.error.game_not_started").formatted(Formatting.RED)
                        ));
                    } else {
                        player.networkHandler.sendPacket(new OverlayMessageS2CPacket(Text.of(game.getRemainingText())));
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
        command.then(literal("role").requires(ServerCommandSource::isExecutedByPlayer).executes(context -> {
            if (game == null || !game.started()) {
                throw (new SimpleCommandExceptionType(Text.translatable("commands.molehunt.error.game_not_started"))).create();
            }

            final var source = context.getSource();
            final var player = source.getPlayer();
            assert player != null;

            if (game.isMole(player)) {
                source.sendFeedback(
                        () -> Text.translatable("commands.molehunt.role.mole")
                                .append("\n\n")
                                .append(Text.translatable("commands.molehunt.role.mole.list", game.getMolesAsString())),
                        false);
            } else if (player.isSpectator()) {
                source.sendFeedback(
                        () -> Text.translatable("commands.molehunt.role.survivor.mole_count", game.getMoles().size()),
                        false);
            } else {
                source.sendFeedback(
                        () -> Text.translatable("commands.molehunt.role.survivor")
                                .append("\n\n")
                                .append(Text.translatable("commands.molehunt.role.survivor.mole_count", game.getMoles().size())),
                        false);
            }

            return Command.SINGLE_SUCCESS;
        }));
        command.then(literal("stop").requires(source -> source.hasPermissionLevel(1)).executes(context -> {
            if (game == null || !game.started()) {
                throw (new SimpleCommandExceptionType(Text.translatable("commands.molehunt.error.game_not_started"))).create();
            }

            game.stop();

            return Command.SINGLE_SUCCESS;
        }));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> CONFIG = new Config(server));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(command));

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> false);

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity) || game == null) return;
            if (!game.started()) return;
            if (game.wonByMoles()) game.end();
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (game == null) return;
            if (!game.started()) return;
            if (game.getMoles().contains(oldPlayer)) game.updateMole(oldPlayer, newPlayer);
            newPlayer.changeGameMode(GameMode.SPECTATOR);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayNetworking.send(
                    handler.player,
                    new ConfigPayload(CONFIG.nametagsEnabled(), CONFIG.skinsEnabled(), CONFIG.tabEnabled())
            );
            ServerPlayNetworking.send(
                    handler.player,
                    new GamePayload(game != null && game.started())
            );
        });

        PayloadTypeRegistry.playS2C().register(ConfigPayload.ID, ConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GamePayload.ID, GamePayload.CODEC);
    }
}
