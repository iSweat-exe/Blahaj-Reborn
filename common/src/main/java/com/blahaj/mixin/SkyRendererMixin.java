package com.blahaj.mixin;

import com.blahaj.client.VoidClientFx;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Black hole sky: purple-black sky, hidden sun, stars in daylight. */
@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void blahaj$darkenSky(ClientLevel level, float partialTicks, Camera camera, SkyRenderState state, CallbackInfo ci) {
        float dark = VoidClientFx.skyDarkness();
        float flash = VoidClientFx.skyFlash();
        if ((dark <= 0 && flash <= 0) || state.skyColor == null) return;

        Vector3f sky = new Vector3f(state.skyColor).lerp(VoidClientFx.VOID_SKY, dark).lerp(new Vector3f(1, 0.9F, 1), flash);
        state.skyColor = sky;
        state.starBrightness = Math.max(state.starBrightness, dark);
        state.rainBrightness *= 1 - dark;
        if (state.sunriseAndSunsetColor != null) {
            state.sunriseAndSunsetColor = new Vector4f(state.sunriseAndSunsetColor).mul(1, 1, 1, 1 - dark);
        }
    }
}
