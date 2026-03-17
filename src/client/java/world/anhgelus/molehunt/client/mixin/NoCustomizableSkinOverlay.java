package world.anhgelus.molehunt.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import world.anhgelus.molehunt.client.MolehuntClient;

@Mixin(Options.class)
public abstract class NoCustomizableSkinOverlay {
    @Unique
    private static int fullParts;

    static {
        for (PlayerModelPart part : PlayerModelPart.values()) {
            fullParts |= part.getMask();
        }
    }
    
    @Shadow
    protected Minecraft minecraft;

    @Inject(at = @At("HEAD"), method = "setModelPart", cancellable = true)
    public void togglePlayerModelPart(PlayerModelPart part, boolean enabled, CallbackInfo ci) {
        if (MolehuntClient.showSkins()) return;
        ci.cancel();
    }

    @Inject(at = @At("RETURN"), method = "buildPlayerInformation", cancellable = true)
    public void buildPlayerInformation(CallbackInfoReturnable<ClientInformation> cir) {
        if (MolehuntClient.showSkins()) return;
        final var opts = (Options) (Object) this;

        cir.setReturnValue(
                new ClientInformation(
                        opts.languageCode,
                        opts.renderDistance().get(),
                        opts.chatVisibility().get(),
                        opts.chatColors().get(),
                        fullParts,
                        opts.mainHand().get(),
                        this.minecraft.isTextFilteringEnabled(),
                        opts.allowServerListing().get(),
                        opts.particles().get()
                )
        );
    }
}
