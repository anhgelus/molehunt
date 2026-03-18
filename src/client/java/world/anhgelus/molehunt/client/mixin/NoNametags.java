package world.anhgelus.molehunt.client.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import world.anhgelus.molehunt.client.MolehuntClient;

@Mixin(EntityRenderer.class)
public class NoNametags<T extends Entity, S extends EntityRenderState> {
	@Inject(at = @At("HEAD"), method = "shouldShowName", cancellable = true)
	private void renderLabelOrNot(T entity, double distanceToCameraSq, CallbackInfoReturnable<Boolean> cir) {
		if (!(entity instanceof Player) || MolehuntClient.showNameTags() || !MolehuntClient.gameStarted()) return;
		cir.setReturnValue(false);
	}
}