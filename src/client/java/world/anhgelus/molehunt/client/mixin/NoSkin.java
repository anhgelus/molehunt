package world.anhgelus.molehunt.client.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import world.anhgelus.molehunt.client.MolehuntClient;

@Mixin(AbstractClientPlayerEntity.class)
public class NoSkin {
    @Inject(at = @At("HEAD"), method = "getSkinTextures", cancellable = true)
    public void getSkin(CallbackInfoReturnable<SkinTextures> cir) {
        cir.setReturnValue(new SkinTextures(
                Identifier.of(MolehuntClient.MOD_ID, "textures/skin.png"),
                null,
                null,
                null,
                SkinTextures.Model.WIDE, true)
        );
    }
}
