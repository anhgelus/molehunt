package world.anhgelus.molehunt;

import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

import static net.minecraft.server.command.CommandManager.literal;


public class Molehunt implements ModInitializer {

    public static final String MOD_ID = "molehunt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
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
//        command.then(literal("time").executes(context -> {
//            context.getSource().sendFeedback(() -> game.getRemainingText(), false);
//            return Command.SINGLE_SUCCESS;
//        }));
        command.then(literal("timer").requires(ServerCommandSource::isExecutedByPlayer).then(
                literal("show").executes(context -> {
                    timerVisibility.put(context.getSource().getPlayer(), true);
                    context.getSource().sendFeedback(() -> Text.of("Showing molehunt timer"), false);

                    var player = context.getSource().getPlayer();
                    assert player != null;

                    if (game == null || !game.hasStarted()) {
                        player.networkHandler.sendPacket(new OverlayMessageS2CPacket(Text.of("§cGame has not started yet")));
                    } else {
                        player.networkHandler.sendPacket(new OverlayMessageS2CPacket(Text.of(game.getShortRemainingText())));
                    }

                    return Command.SINGLE_SUCCESS;
                })
        ).then(
                literal("hide").executes(context -> {
                    timerVisibility.put(context.getSource().getPlayer(), false);
                    context.getSource().sendFeedback(() -> Text.of("Hiding molehunt timer"), false);
                    return Command.SINGLE_SUCCESS;
                })
        ));
        command.then(literal("moles").requires(source -> (game != null) && game.hasStarted() && game.isAMole(source.getPlayer())).executes(context -> {
            context.getSource().sendFeedback(() -> Text.literal("List of moles: " + game.getMolesAsString()),false);
            return Command.SINGLE_SUCCESS;
        }));
        command.then(literal("stop").requires(source -> source.hasPermissionLevel(1)).executes(context -> {
            if (game == null || !game.hasStarted()) {
                context.getSource().sendError(Text.of("Game has not started yet"));
            }

            game.stop();

            return Command.SINGLE_SUCCESS;
        }));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(command));

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> false);

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity instanceof ServerPlayerEntity) || game == null) return;
            if (game.gameWonByMoles()) game.end();
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (game == null) return;
            if (!game.hasStarted()) return;
            if (game.getMoles().contains(oldPlayer)) game.updateMole(oldPlayer, newPlayer);
            newPlayer.changeGameMode(GameMode.SPECTATOR);
        });
    }
}
