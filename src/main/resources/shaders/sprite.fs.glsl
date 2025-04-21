#version 330 core

in vec2 TexCoord;
in vec3 FragPos;
in vec3 Normal;

out vec4 FragColor;

struct Light {
    vec3 position;
    vec3 color;
    vec3 direction;
    float intensity;
    float constant;
    float linear;
    float quadratic;
    float cutoff;
    float outerCutoff;
    int type;
};

#define MAX_LIGHTS 10
uniform Light lights[MAX_LIGHTS];
uniform int lightCount;

uniform sampler2D u_Texture;
uniform vec3 u_Palette[4];
uniform vec4 u_Color;

uniform vec3 u_AmbientColor;
uniform vec3 u_Specular;
uniform float u_Shininess;
uniform vec3 u_ViewPos;

void main()
{
    // Sample the texture
    vec4 texColor = texture(u_Texture, TexCoord);
    if (texColor.a < 0.0001)
    discard;

    // Determine which palette color to use based on the texture's red channel
    uint i;
    if(texColor.r >= (0xA0 / 255.0))
    i = 3u;
    else if(texColor.r >= (0x70 / 255.0))
    i = 2u;
    else if(texColor.r >= (0x40 / 255.0))
    i = 1u;
    else
    i = 0u;

    vec3 baseColor = u_Palette[int(i)];

    // Start with ambient light only
    vec3 lighting = u_AmbientColor;

    // For 2D sprites, we need to ensure normal is properly oriented
    // As a fallback for sprites without normal data, default to facing camera
    vec3 norm = normalize(Normal.xyz);
    if(length(norm) < 0.5) { // Check if normal is too short (not set properly)
        norm = vec3(0.0, 0.0, 1.0); // Default normal pointing toward camera
    }

    // Process all available lights
    for (int idx = 0; idx < min(lightCount, MAX_LIGHTS); idx++) {
        Light light = lights[idx];
        vec3 lightDir;
        float attenuation = 1.0;

        if (light.type == 0) {
            // Directional light - light comes from a specific direction, no attenuation
            lightDir = normalize(-light.direction);
        } else {
            // Point or spotlight - calculate direction from fragment to light
            lightDir = normalize(light.position - FragPos);

            // Calculate distance-based attenuation
            float distance = length(light.position - FragPos);
            attenuation = 1.0 / (light.constant + light.linear * distance +
            light.quadratic * (distance * distance));
        }

        // Diffuse component calculation
        float diff = max(dot(norm, lightDir), 0.0);
        vec3 diffuse = light.color * diff * light.intensity;

        // Specular component calculation (using Blinn-Phong)
        vec3 viewDir = normalize(u_ViewPos - FragPos);
        vec3 halfwayDir = normalize(lightDir + viewDir);
        float spec = pow(max(dot(norm, halfwayDir), 0.0), u_Shininess);
        vec3 specular = u_Specular * spec * light.color * light.intensity;

        // If this is a spotlight, apply the cone effect
        if (light.type == 2) {
            float theta = dot(lightDir, normalize(-light.direction));
            float epsilon = light.cutoff - light.outerCutoff;
            float intensity = clamp((theta - light.outerCutoff) / epsilon, 0.0, 1.0);
            diffuse *= intensity;
            specular *= intensity;
        }

        // Add this light's contribution (with attenuation)
        lighting += (diffuse + specular) * attenuation;
    }

    // Make sure lighting doesn't exceed reasonable values
    lighting = clamp(lighting, 0.0, 1.5);

    // Apply the lighting to the base color
    vec3 finalColor = baseColor * lighting;

    // Output the final color
    FragColor = vec4(finalColor, texColor.a) * u_Color;
}