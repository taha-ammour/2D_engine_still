#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec3 aNormal;

uniform mat4 u_MVP;
uniform mat4 u_Model;
uniform bool u_flipX;
uniform bool u_flipY;
uniform vec4 u_texCoords; // (u0, v0, u1, v1)

out vec2 TexCoord;
out vec3 FragPos;
out vec3 Normal;

void main() {
    vec4 worldPos = u_Model * vec4(aPos, 1.0);
    FragPos = worldPos.xyz;
    Normal = mat3(transpose(inverse(u_Model))) * aNormal;

    // Get the original texture coordinate
    vec2 texCoord = aTexCoord;

    // Extract texture region coordinates
    float u0 = u_texCoords.x;
    float v0 = u_texCoords.y;
    float u1 = u_texCoords.z;
    float v1 = u_texCoords.w;

    // Flip within the sprite's own texture region
    if(u_flipX) {
        // Map from local space (0-1) to atlas space
        float uNorm = (texCoord.x - u0) / (u1 - u0);
        // Flip the normalized coordinate
        uNorm = 1.0 - uNorm;
        // Map back to atlas space
        texCoord.x = u0 + uNorm * (u1 - u0);
    }

    if(u_flipY) {
        // Map from local space (0-1) to atlas space
        float vNorm = (texCoord.y - v0) / (v1 - v0);
        // Flip the normalized coordinate
        vNorm = 1.0 - vNorm;
        // Map back to atlas space
        texCoord.y = v0 + vNorm * (v1 - v0);
    }

    TexCoord = texCoord;
    gl_Position = u_MVP * vec4(aPos, 1.0);
}