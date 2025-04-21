#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec4 aColor;
layout (location = 2) in vec2 aTexCoord;

uniform mat4 u_MVP;

out vec4 Color;
out vec2 TexCoord;

void main() {
    gl_Position = u_MVP * vec4(aPos, 1.0);
    Color = aColor;
    TexCoord = aTexCoord;
}