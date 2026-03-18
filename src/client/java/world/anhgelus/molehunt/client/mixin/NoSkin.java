package world.anhgelus.molehunt.client.mixin;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import world.anhgelus.molehunt.Molehunt;
import world.anhgelus.molehunt.client.MolehuntClient;

@Mixin(AbstractClientPlayerEntity.class)
public class NoSkin {
	@Inject(at = @At("HEAD"), method = "getSkin", cancellable = true)
	public void getSkin(CallbackInfoReturnable<SkinTextures> cir) {
		if (MolehuntClient.showSkins() || !MolehuntClient.gameStarted()) return;
		cir.setReturnValue(SkinTextures.create(
			new AssetInfo.TextureAssetInfo(Identifier.of(Molehunt.MOD_ID, "skin")),
			null,
			null,
			PlayerSkinType.WIDE
		));
	}
}
