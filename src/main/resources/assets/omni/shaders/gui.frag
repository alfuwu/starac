#version 330 core

in vec2 vTexCoord;
in vec4 vColor;

uniform sampler2D uTexture;
uniform int uUseTexture;
uniform vec4 uColor;
uniform int uUseVertexColor;
uniform float uAlphaTest;

out vec4 fragColor;

void main() {
    vec4 color;

    if (uUseVertexColor != 0) {
        color = vColor;
    } else {
        color = uColor;
    }

    if (uUseTexture != 0) {
        color *= texture(uTexture, vTexCoord);
    }

    if (color.a < uAlphaTest) {
        discard;
    }

    fragColor = color;
}
