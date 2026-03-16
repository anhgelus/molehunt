package world.anhgelus.molehunt.client.mixin;

import net.minecraft.client.Options;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.anhgelus.molehunt.client.MolehuntClient;

@Mixin(Options.class)
public abstract class NoCustomizableSkinOverlay {
    @Shadow
    public void setModelPart(PlayerModelPart part, boolean enabled) {}

    @Inject(at = @At("HEAD"), method = "setModelPart", cancellable = true)
    public void togglePlayerModelPart(PlayerModelPart part, boolean enabled, CallbackInfo ci) {
        if (MolehuntClient.showSkins()) return;
        setModelPart(part, true);
        ((Options) (Object) this).broadcastOptions();
        ci.cancel();
    }
}
