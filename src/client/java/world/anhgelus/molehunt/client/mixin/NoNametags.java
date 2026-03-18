package world.anhgelus.molehunt.client.mixin;

import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import world.anhgelus.molehunt.client.MolehuntClient;

@Mixin(EntityRenderer.class)
public class NoNametags<T extends Entity, S extends EntityRenderState> {
	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	private void renderLabelOrNot(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState, CallbackInfo ci) {
		if (EntityType.PLAYER != state.entityType || MolehuntClient.showNameTags() || !MolehuntClient.gameStarted())
			return;
		ci.cancel();
	}
}