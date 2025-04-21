#version 330 core

in vec4 Color;
in vec2 TexCoord;

out vec4 FragColor;

uniform sampler2D u_Texture;

void main() {
    vec4 texColor = texture(u_Texture, TexCoord);
    FragColor = texColor * Color;
}