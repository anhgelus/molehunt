package world.anhgelus.molehunt.client.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.anhgelus.molehunt.client.MolehuntClient;

@Mixin(GameOptions.class)
public abstract class NoCustomizableSkinOverlay {
    @Shadow
    private void setPlayerModelPart(PlayerModelPart part, boolean enabled) {}

    @Inject(at = @At("HEAD"), method = "togglePlayerModelPart", cancellable = true)
    public void togglePlayerModelPart(PlayerModelPart part, boolean enabled, CallbackInfo ci) {
        if (MolehuntClient.showSkins() && MolehuntClient.gameStarted()) {
            setPlayerModelPart(part, true);
            ((GameOptions) (Object) this).sendClientSettings();

            ci.cancel();
        }
    }
}
