package world.anhgelus.molehunt.client.mixin;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.anhgelus.molehunt.client.MolehuntClient;

@Mixin(PlayerTabOverlay.class)
public class NoPlayerListHud {
    @Inject(at = @At("HEAD"), method = "setVisible", cancellable = true)
    public void render(CallbackInfo ci) {
        if (MolehuntClient.showTab() || !MolehuntClient.gameStarted()) return;
        ci.cancel();
    }
}
