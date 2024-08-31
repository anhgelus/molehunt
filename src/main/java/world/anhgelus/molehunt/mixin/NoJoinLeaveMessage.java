package world.anhgelus.molehunt.mixin;

import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class NoJoinLeaveMessage {
    @Inject(at = @At("HEAD"), method = "broadcast*", cancellable = true)
    public void broadcastNoJoinLeaveMessage(Text message, boolean overlay, CallbackInfo ci) {
        final var content = message.getContent().toString();
        if (content.startsWith("translation{key='multiplayer.player.joined")) ci.cancel();
        else if (content.startsWith("translation{key='multiplayer.player.left")) ci.cancel();
    }
}
