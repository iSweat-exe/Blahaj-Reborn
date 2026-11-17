#version 330
#extension GL_ARB_separate_shader_objects : require

// Void Blåhaj glitch / distortion.
// Intensity (0 to 1) comes from the post_effect file (one file per intensity step).
// GameTime (Globals) is the clock: GameTime * 1200 = seconds, interpolated between ticks.

// Same block as minecraft:globals.glsl (#moj_import is not available in post shaders).
layout(std140) uniform Globals {
    ivec3 CameraBlockPos;
    float GlintAlpha;
    vec3 CameraOffset;
    float GameTime;
    vec2 ScreenSize;
    int MenuBlurRadius;
    int UseRgss;
};

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform VoidGlitchConfig {
    float Intensity;
};

layout(location = 0) out vec4 fragColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float hash1(float x) {
    return hash(vec2(x, x * 0.37 + 11.7));
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1, 0)), f.x), mix(hash(i + vec2(0, 1)), hash(i + vec2(1, 1)), f.x), f.y);
}

vec3 sampleRGB(vec2 uv, vec2 split) {
    float r = texture(InSampler, clamp(uv + split, 0.001, 0.999)).r;
    float g = texture(InSampler, clamp(uv, 0.001, 0.999)).g;
    float b = texture(InSampler, clamp(uv - split, 0.001, 0.999)).b;
    return vec3(r, g, b);
}

void main() {
    float I = Intensity;
    float t = GameTime * 1200.0;
    vec2 uv = texCoord;
    float aspect = OutSize.x / OutSize.y;

    // Stepped clocks: the glitch changes in jumps, not continuously.
    float stepFast = floor(t * 14.0);
    float stepSlow = floor(t * 4.0);
    float burst = step(1.0 - I * 0.55, hash1(stepSlow * 1.7));  // bursts of intense glitching

    // 1. Gravitational lens: the image sinks toward the centre and breathes.
    vec2 c = uv - 0.5;
    c.x *= aspect;
    float r2 = dot(c, c);
    float breathe = 0.6 + 0.4 * sin(t * 2.3) + 0.3 * sin(t * 5.1);
    float lens = -0.22 * I * breathe;
    vec2 cl = c * (1.0 + lens * r2) ;
    // Slight spiral twist around the centre.
    float swirl = I * 0.35 * exp(-r2 * 3.0) * sin(t * 0.9);
    float cs = cos(swirl), sn = sin(swirl);
    cl = mat2(cs, -sn, sn, cs) * cl;
    cl.x /= aspect;
    uv = cl + 0.5;

    // 2. Heat wobble (smooth noise).
    vec2 wob = vec2(noise(vec2(uv.y * 9.0, t * 1.7)), noise(vec2(uv.x * 7.0, t * 1.3 + 4.0))) - 0.5;
    uv += wob * 0.012 * I;

    // 3. Horizontal tearing: randomly offset bands.
    float bands = 18.0 + floor(hash1(stepFast) * 40.0);
    float band = floor(uv.y * bands);
    float bandRnd = hash(vec2(band, stepFast));
    float tear = step(1.0 - I * (0.25 + 0.45 * burst), bandRnd);
    uv.x += tear * (hash(vec2(band + 3.1, stepFast)) - 0.5) * (0.06 + 0.18 * burst) * I;

    // 4. Corrupted macro blocks: image rectangles are copied elsewhere.
    vec2 grid = vec2(16.0, 9.0) * (1.0 + floor(hash1(stepSlow + 9.0) * 3.0));
    vec2 cell = floor(uv * grid);
    float cellRnd = hash(cell + stepFast * 0.13);
    if (cellRnd < I * I * 0.12 * (0.4 + burst)) {
        uv = fract(uv + vec2(hash(cell + 1.3), hash(cell + 7.9)) * 0.3 - 0.15);
    }

    // 5. Burst pixelation (mosaic on some bands).
    float mosaicBand = step(1.0 - I * 0.3 * burst, hash(vec2(floor(uv.y * 6.0), stepFast + 2.0)));
    if (mosaicBand > 0.5) {
        float px = mix(64.0, 16.0, I);
        uv = (floor(uv * OutSize / px) + 0.5) * px / OutSize;
    }

    // 6. Chromatic aberration: radial plus horizontal jitter.
    vec2 dir = normalize(uv - 0.5 + 1e-5);
    float ca = (0.002 + 0.010 * I + 0.02 * I * burst) * (0.4 + length(uv - 0.5) * 1.6);
    vec2 split = dir * ca + vec2((hash1(stepFast + 5.0) - 0.5) * 0.02 * I * burst, 0.0);
    vec3 col = sampleRGB(uv, split);

    // 7. Offset ghost image.
    vec3 ghost = texture(InSampler, clamp(uv + vec2(0.012, 0.004) * sin(t * 3.0) * I, 0.001, 0.999)).rgb;
    col = mix(col, max(col, ghost * vec3(0.9, 0.4, 1.1)), 0.25 * I);

    // 8. Void colour grade: desaturated, purple shadows, cyan highlights.
    float lum = dot(col, vec3(0.299, 0.587, 0.114));
    vec3 voidTone = mix(vec3(0.12, 0.0, 0.22), vec3(0.75, 0.95, 1.0), smoothstep(0.1, 0.9, lum));
    col = mix(col, voidTone * (0.5 + lum), 0.32 * I);

    // 9. Scanlines and a rolling bright band.
    float scan = 0.5 + 0.5 * sin(gl_FragCoord.y * 3.14159);
    col *= 1.0 - 0.10 * I * scan;
    float roll = fract(t * 0.35);
    col += vec3(0.6, 0.3, 1.0) * 0.08 * I * smoothstep(0.03, 0.0, abs(texCoord.y - roll));

    // 10. Grain and static.
    float grain = hash(gl_FragCoord.xy + fract(t * 7.0) * 100.0);
    col += (grain - 0.5) * 0.14 * I;
    if (grain > 1.0 - 0.004 * I * (1.0 + 4.0 * burst)) col = vec3(1.0, 0.9, 1.0);

    // 11. Very brief colour inversion flashes during bursts.
    float invert = step(1.0 - I * 0.05, hash1(stepFast * 3.3)) * burst;
    col = mix(col, vec3(1.0) - col, invert);

    // 12. Purple vignette.
    vec2 v = texCoord - 0.5;
    float vig = smoothstep(0.25, 0.85, length(v * vec2(aspect, 1.0)) / aspect * 1.4);
    col = mix(col, vec3(0.06, 0.0, 0.12), vig * 0.65 * I);

    fragColor = vec4(clamp(col, 0.0, 1.0), 1.0);
}
