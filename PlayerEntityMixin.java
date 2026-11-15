package dev.nico.tropiezo.mixin;

import dev.nico.tropiezo.TripState;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mientras estás "tropezado", el jugador queda en pose de gateo (acostado boca abajo). */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "updatePose", at = @At("HEAD"), cancellable = true)
    private void tropiezo$forceProne(CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (TripState.isTripped(self)) {
            self.setPose(EntityPose.SWIMMING);
            ci.cancel();
        }
    }
}
