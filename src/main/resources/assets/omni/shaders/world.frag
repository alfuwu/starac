#version 330 core

in vec2 vTexCoord;
in vec4 vColor;
in float vFogDistance;

uniform sampler2D uTexture;
uniform int uUseTexture;
uniform vec4 uColor;
uniform int uUseVertexColor;
uniform float uAlphaTest;

// Fog
uniform int uUseFog;
uniform float uFogStart;
uniform float uFogEnd;
uniform vec3 uFogColor;

out vec4 fragColor;

void main() {
    vec4 color;

    if (uUseVertexColor != 0) {
        color = vColor;
    } else {
        color = uColor;
    }

    if (uUseTexture != 0) {
        vec4 texColor = texture(uTexture, vTexCoord);
        color *= texColor;
    }

    if (color.a < uAlphaTest) {
        discard;
    }

    // Apply fog (linear)
    if (uUseFog != 0 && uFogEnd > uFogStart) {
        float fogFactor = clamp((uFogEnd - vFogDistance) / (uFogEnd - uFogStart), 0.0, 1.0);
        color.rgb = mix(uFogColor, color.rgb, fogFactor);
    }

    fragColor = color;
}
