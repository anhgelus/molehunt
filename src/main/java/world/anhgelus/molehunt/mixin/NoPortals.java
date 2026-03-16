package world.anhgelus.molehunt.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PortalProcessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import world.anhgelus.molehunt.Molehunt;

@Mixin(PortalProcessor.class)
public class NoPortals {
    @Inject(at = @At("HEAD"), method = "processPortalTeleportation", cancellable = true)
    public void disableTick(ServerLevel world, Entity entity, boolean canUsePortals, CallbackInfoReturnable<Boolean> cir) {
        if (Molehunt.CONFIG == null || Molehunt.CONFIG.portalsEnabled()) return;
        cir.setReturnValue(false);
    }
}
