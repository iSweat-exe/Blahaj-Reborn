package com.blahaj.client;

import com.blahaj.BlahajMod;
import com.blahaj.voidhaj.BlackHoleEntity;
import com.blahaj.voidhaj.RisingVoidBlahaj;
import com.blahaj.voidhaj.VoidContent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

/**
 * Screen effects for the player: camera shake, glitches, purple vignette,
 * corrupted text, explosion flash, glitch sounds and tinnitus.
 */
public final class VoidClientFx {
    /** Sky and horizon colours when the void takes over. */
    public static final Vector3f VOID_SKY = new Vector3f(0.06F, 0.0F, 0.11F);
    public static final Vector3f VOID_FOG = new Vector3f(0.10F, 0.02F, 0.16F);

    private static float intensity;  // 0 to 1, smoothed
    private static float target;  // recomputed every tick by nearby entities
    private static float flash;  // explosion flash
    private static float shock;  // shockwave shake
    private static float darkness, darknessTarget;  // sky darkening
    private static float squeeze, squeezeTarget;  // FOV squeeze
    private static float prevFov = 1F, fov = 1F;  // FOV multiplier (interpolated between ticks)
    private static float fovKick;  // FOV kick on explosion
    private static final Map<BlackHoleEntity, Boolean> LOOPING = new WeakHashMap<>();
    private static final Random RNG = new Random();

    private VoidClientFx() {}

    // Entity hooks

    public static void onRisingTick(RisingVoidBlahaj e) {
        double hover = Mth.clamp((e.getAge() - RisingVoidBlahaj.RISE_TICKS) / (double) RisingVoidBlahaj.HOVER_TICKS, 0, 1);
        push(e, 0.08F + 0.3F * (float) hover, 48);
        float w = weight(e, 160);
        darknessTarget = Math.max(darknessTarget, 0.25F * (float) hover * w);
    }

    private static float weight(Entity source, double range) {
        Entity player = Minecraft.getInstance().player;
        if (player == null) return 0;
        return (float) Mth.clamp(1 - player.distanceTo(source) / range, 0, 1);
    }

    public static void onBlackHoleTick(BlackHoleEntity e) {
        if (!LOOPING.containsKey(e)) {
            LOOPING.put(e, Boolean.TRUE);
            Minecraft.getInstance().getSoundManager().play(new VoidLoopSound(e));
        }
        float size = e.getRadius() / BlackHoleEntity.MAX_RADIUS;
        float level = 0.25F + 0.5F * size;
        if (e.isCollapsing()) level = 0.85F + 0.15F * RNG.nextFloat();
        push(e, level, 96);

        float w = weight(e, 160);
        float near = 0.4F + 0.6F * w;
        darknessTarget = Math.max(darknessTarget, (e.isCollapsing() ? 1F : 0.35F + 0.6F * size) * near);
        // FOV narrows a little while growing and a lot while collapsing.
        squeezeTarget = Math.max(squeezeTarget, (e.isCollapsing() ? 0.25F + 0.75F * (1 - size) : 0.2F * size) * near);
    }

    public static void onExplosion(BlackHoleEntity e) {
        Entity player = Minecraft.getInstance().player;
        if (player == null) return;
        double d = player.distanceTo(e);
        float w = (float) Mth.clamp(1 - d / 120, 0, 1);
        flash = Math.max(flash, 0.3F + 0.7F * w);
        shock = Math.max(shock, w);
        fovKick = Math.max(fovKick, 0.25F + 0.35F * w);
        squeeze = 0;
        if (w > 0) shockStep = 0;
        if (d < 64) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(VoidContent.RING, 1.0F, 0.9F * w + 0.1F));
        }
    }

    private static void push(Entity source, float level, double range) {
        Entity player = Minecraft.getInstance().player;
        if (player == null) return;
        double d = player.distanceTo(source);
        float w = (float) Mth.clamp(1 - d / range, 0, 1);
        target = Math.max(target, level * (0.35F + 0.65F * w) * (d < range ? 1 : 0));
    }

    // Client tick

    public static void tick(Minecraft mc) {
        intensity += (target - intensity) * 0.15F;
        target = 0;
        flash *= 0.92F;
        shock *= 0.94F;
        if (flash < 0.003F) flash = 0;
        if (shock < 0.003F) shock = 0;

        // The sky darkens slowly and brightens even more slowly.
        darkness += (darknessTarget - darkness) * (darknessTarget > darkness ? 0.06F : 0.025F);
        darknessTarget = 0;
        if (darkness < 0.002F) darkness = 0;

        squeeze += (squeezeTarget - squeeze) * 0.12F;
        squeezeTarget = 0;
        fovKick *= 0.88F;
        if (fovKick < 0.002F) fovKick = 0;
        prevFov = fov;
        fov = 1F - 0.4F * squeeze + fovKick;

        if (mc.player != null && intensity > 0.18F && RNG.nextFloat() < intensity * 0.07F) {
            float pitch = 0.6F + RNG.nextFloat() * 1.0F;
            mc.getSoundManager().play(SimpleSoundInstance.forUI(VoidContent.GLITCH, pitch, 0.15F + intensity * 0.45F));
        }

        if (shockStep >= 0 && ++shockStep >= SHOCK_STEPS) shockStep = -1;
        updatePostEffects(mc);
    }

    // Fullscreen shaders

    private static final int GLITCH_LEVELS = 16;
    private static final int SHOCK_STEPS = 16;
    private static int shockStep = -1;

    /**
     * Adds our post effects to the player's active ones: the glitch shader at the current intensity step
     * (assets/blahaj/post_effect/void_glitch_N.json) and the shockwave after the explosion.
     */
    private static void updatePostEffects(Minecraft mc) {
        if (mc.player == null) return;
        List<Identifier> effects = mc.player.getActivePostEffects();
        effects.removeIf(id -> id.getNamespace().equals(BlahajMod.MOD_ID) && id.getPath().startsWith("void_"));

        int level = Math.round(Mth.clamp(intensity * 1.1F, 0F, 1F) * GLITCH_LEVELS);
        if (level > 0) effects.add(BlahajMod.id("void_glitch_" + level));
        if (shockStep >= 0) effects.add(BlahajMod.id("void_shock_" + (shockStep + 1)));
    }

    // Sky / FOV (read by mixins)

    public static float skyDarkness() {
        return darkness;
    }

    public static float skyFlash() {
        return Math.min(1F, flash * 0.8F);
    }

    public static float fovMultiplier(float partialTick) {
        return Mth.lerp(partialTick, prevFov, fov);
    }

    // Camera shake (read by CameraMixin)

    private static float shakeAmplitude() {
        return intensity * intensity * 2.2F + shock * 9F + flash * 2F;
    }

    public static float shakeYaw() {
        float a = shakeAmplitude();
        if (a <= 0) return 0;
        double t = System.nanoTime() / 1.0e9;
        return a * (float) (Math.sin(t * 31.0) * 0.5 + Math.sin(t * 17.3 + 1.7) * 0.35 + Math.sin(t * 53.1) * 0.15);
    }

    public static float shakePitch() {
        float a = shakeAmplitude();
        if (a <= 0) return 0;
        double t = System.nanoTime() / 1.0e9;
        return a * 0.8F * (float) (Math.sin(t * 27.0 + 0.6) * 0.5 + Math.sin(t * 13.7 + 2.9) * 0.35 + Math.sin(t * 47.9) * 0.15);
    }

    // HUD
    // The shader handles distortion, tearing and noise; the HUD adds what a shader
    // cannot: corrupted text and the explosion flash.

    public static void renderHud(GuiGraphicsExtractor g, DeltaTracker delta) {
        float i = intensity;
        if (i < 0.01F && flash <= 0) return;

        int w = g.guiWidth();
        int h = g.guiHeight();
        long now = System.currentTimeMillis();
        Random r = new Random(now / 50);  // changes 20 times per second
        Random slow = new Random(now / 450);  // stays for ~0.5 s
        Font font = Minecraft.getInstance().font;

        if (i > 0.01F) {
            // Light dark veil (the vignette is in the shader).
            g.fill(0, 0, w, h, argb(i * 0.18F, 8, 0, 20));
            // Shader packs own the sky, so darken the screen instead, mostly at the top.
            if (darkness > 0 && ShaderPackCompat.isShaderPackActive()) {
                g.fillGradient(0, 0, w, h, argb(darkness * 0.6F, 10, 0, 25), argb(darkness * 0.25F, 10, 0, 25));
            }

            // Corrupted text with RGB split and shifted slices.
            if (i > 0.25F) {
                int lines = r.nextFloat() < i ? 1 + r.nextInt(1 + (int) (i * 3)) : 0;
                for (int l = 0; l < lines; l++) {
                    Component text = cryptic(r);
                    int x = r.nextInt(Math.max(1, w - font.width(text)));
                    int y = r.nextInt(Math.max(1, h - 10));
                    glitchText(g, font, text, x, y, r, 0.9F);
                }
            }

            // While collapsing: large shaking message in the centre.
            if (i > 0.8F && slow.nextFloat() < 0.6F) {
                Component big = cryptic(slow);
                float scale = 2.5F + slow.nextFloat();
                g.pose().pushMatrix();
                g.pose().translate(w / 2F + (r.nextFloat() - 0.5F) * 6, h / 2F - 30 + (r.nextFloat() - 0.5F) * 6);
                g.pose().scale(scale, scale);
                glitchText(g, font, big, -font.width(big) / 2, 0, r, 0.85F);
                g.pose().popMatrix();
            }
        }

        // Explosion flash: white, fading to purple.
        if (flash > 0) {
            g.fill(0, 0, w, h, argb(Math.min(1, flash * 1.1F), 255, 255, 255));
            if (flash < 0.6F) g.fill(0, 0, w, h, argb(flash * 0.5F, 140, 0, 255));
        }
    }

    /** Three offset text layers (cyan, magenta, white), sometimes cut into slices. */
    private static void glitchText(GuiGraphicsExtractor g, Font font, Component text, int x, int y, Random r, float alpha) {
        int dx = 1 + r.nextInt(3);
        g.text(font, text, x - dx, y, argb(alpha * 0.8F, 0, 255, 255), false);
        g.text(font, text, x + dx, y + (r.nextBoolean() ? 1 : 0), argb(alpha * 0.8F, 255, 0, 200), false);
        g.text(font, text, x, y, argb(alpha, 235, 215, 255), false);
        if (r.nextFloat() < 0.4F) {
            int sliceY = y + 2 + r.nextInt(5);
            int shift = (r.nextInt(9) - 4) * 2;
            g.fill(x + shift, sliceY, x + shift + font.width(text), sliceY + 2, argb(alpha * 0.9F, 200, 40, 255));
        }
    }

    /** Symbols that never form a word: blocks, math signs, assorted glyphs. */
    private static final String GIBBERISH = "█▓▒░∅∆∑∞∫≈≠≡ΩΨΣЖЯЙאשᚠᚱᛉ❀❖▬◢◣☰☲";

    /** Unreadable gibberish: random glyphs, fully obfuscated. */
    private static Component cryptic(Random r) {
        int len = 4 + r.nextInt(14);
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < len; k++) {
            sb.append(GIBBERISH.charAt(r.nextInt(GIBBERISH.length())));
        }
        return Component.literal(sb.toString()).withStyle(ChatFormatting.OBFUSCATED);
    }

    private static int argb(float alpha, int red, int green, int blue) {
        int a = Mth.clamp((int) (alpha * 255), 0, 255);
        return a << 24 | red << 16 | green << 8 | blue;
    }
}
