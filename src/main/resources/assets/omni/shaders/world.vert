#version 330 core

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec4 aColor;

uniform mat4 uMVP;

out vec2 vTexCoord;
out vec4 vColor;
out float vFogDistance;

void main() {
    vec4 pos = uMVP * vec4(aPosition, 1.0);
    gl_Position = pos;
    vTexCoord = aTexCoord;
    vColor = aColor;
    // Fog distance: use eye-space Z (linear depth from camera)
    vFogDistance = length(pos.xyz);
}
