package com.blahaj.mixin;

import com.blahaj.client.VoidClientFx;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fog follows the darkened sky colour, otherwise the horizon would stay light blue. */
@Mixin(AtmosphericFogEnvironment.class)
public abstract class AtmosphericFogMixin {
    @Inject(method = "getBaseColor", at = @At("RETURN"), cancellable = true)
    private void blahaj$darkenFog(ClientLevel level, Camera camera, int renderDistance, float partialTicks, CallbackInfoReturnable<Vector3fc> cir) {
        float dark = VoidClientFx.skyDarkness();
        float flash = VoidClientFx.skyFlash();
        if (dark <= 0 && flash <= 0) return;
        cir.setReturnValue(new Vector3f(cir.getReturnValue()).lerp(VoidClientFx.VOID_FOG, dark).lerp(new Vector3f(1, 0.9F, 1), flash));
    }
}
