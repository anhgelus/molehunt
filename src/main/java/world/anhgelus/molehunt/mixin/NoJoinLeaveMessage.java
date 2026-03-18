package world.anhgelus.molehunt.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
public class NoJoinLeaveMessage {
	@Inject(at = @At("HEAD"), method = "broadcastSystemMessage*", cancellable = true)
	public void broadcastNoJoinLeaveMessage(final Component message, final boolean overlay, CallbackInfo ci) {
		final var content = message.getContents().toString();
		if (content.startsWith("translation{key='multiplayer.player.joined") ||
			content.startsWith("translation{key='multiplayer.player.left")) ci.cancel();
	}
}
