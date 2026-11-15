package dev.nico.tropiezo.mixin.client;

import dev.nico.tropiezo.TripState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mientras estás en el piso no podés moverte ni saltar. */
@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void tropiezo$lockInput(boolean slowDown, float f, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && TripState.isTripped(client.player)) {
            Input input = (Input) (Object) this;
            input.pressingForward = false;
            input.pressingBack = false;
            input.pressingLeft = false;
            input.pressingRight = false;
            input.movementForward = 0.0F;
            input.movementSideways = 0.0F;
            input.jumping = false;
            input.sneaking = false;
        }
    }
}
