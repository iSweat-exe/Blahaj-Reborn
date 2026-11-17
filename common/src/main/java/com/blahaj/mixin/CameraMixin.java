package com.blahaj.mixin;

import com.blahaj.client.VoidClientFx;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Camera shake: offsets the rendered orientation, not the player's real look direction. */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float blahaj$shakeYaw(float yRot) {
        return yRot + VoidClientFx.shakeYaw();
    }

    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float blahaj$shakePitch(float xRot) {
        return xRot + VoidClientFx.shakePitch();
    }

    /** FOV narrows during the collapse, then kicks wide on explosion. */
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void blahaj$voidFov(float partialTicks, CallbackInfoReturnable<Float> cir) {
        float mul = VoidClientFx.fovMultiplier(partialTicks);
        if (mul != 1F) cir.setReturnValue(cir.getReturnValue() * mul);
    }
}
