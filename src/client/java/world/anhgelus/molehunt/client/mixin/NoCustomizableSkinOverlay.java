package world.anhgelus.molehunt.client.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.anhgelus.molehunt.client.MolehuntClient;

@Mixin(GameOptions.class)
public abstract class NoCustomizableSkinOverlay {
	@Inject(at = @At("HEAD"), method = "setPlayerModelPart", cancellable = true)
	public void togglePlayerModelPart(PlayerModelPart part, boolean enabled, CallbackInfo ci) {
		if (MolehuntClient.showSkins()) return;
		ci.cancel();
	}
}
