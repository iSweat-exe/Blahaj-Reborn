#version 330
#extension GL_ARB_separate_shader_objects : require

// Explosion shockwave: a ring leaves the screen centre,
// distorts the image and splits the colours as it passes.
// Progress (0 to 1) comes from the post_effect file (one file per step).

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform VoidShockConfig {
    float Progress;
};

layout(location = 0) out vec4 fragColor;

void main() {
    float aspect = OutSize.x / OutSize.y;
    vec2 c = texCoord - 0.5;
    c.x *= aspect;
    float d = length(c);
    vec2 dir = c / max(d, 1e-4);

    float radius = Progress * 1.3;
    float width = 0.06 + 0.10 * Progress;
    float fade = 1.0 - Progress;
    float ring = exp(-pow((d - radius) / width, 2.0)) * fade;

    // Radial push along the ring plus a global zoom that relaxes.
    vec2 offset = dir * ring * 0.09;
    offset += c * 0.12 * fade * fade;
    offset.x /= aspect;
    vec2 uv = texCoord - offset;

    float ca = ring * 0.025;
    vec2 caDir = vec2(dir.x / aspect, dir.y);
    vec3 col;
    col.r = texture(InSampler, clamp(uv + caDir * ca, 0.001, 0.999)).r;
    col.g = texture(InSampler, clamp(uv, 0.001, 0.999)).g;
    col.b = texture(InSampler, clamp(uv - caDir * ca, 0.001, 0.999)).b;

    // The ring itself glows slightly (white-purple).
    col += vec3(0.9, 0.7, 1.0) * ring * 0.35;

    fragColor = vec4(clamp(col, 0.0, 1.0), 1.0);
}
