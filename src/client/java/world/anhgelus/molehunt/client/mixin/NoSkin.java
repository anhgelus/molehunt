package world.anhgelus.molehunt.client.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import world.anhgelus.molehunt.Molehunt;
import world.anhgelus.molehunt.client.MolehuntClient;

@Mixin(AbstractClientPlayer.class)
public class NoSkin {
    @Inject(at = @At("HEAD"), method = "getSkin", cancellable = true)
    public void getSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        if (MolehuntClient.showSkins() || !MolehuntClient.gameStarted()) return;
        cir.setReturnValue(new PlayerSkin(
                new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath(Molehunt.MOD_ID, "skin")),
                null,
                null,
                PlayerModelType.WIDE,
                true
        ));
    }
}
