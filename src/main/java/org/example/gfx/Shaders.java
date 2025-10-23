package org.example.gfx;

public final class Shaders {

    private Shaders() {}

    // ============================================
    // BASIC SPRITE SHADER (Most Common)
    // ============================================

    public static final String SPRITE_VERT = """
    #version 330 core

    layout(std140) uniform Camera {
        mat4 uProj;
        mat4 uView;
    };

    layout (location = 0) in vec2 aPos;
    layout (location = 1) in vec2 aUV;

    uniform mat4 uModel;
    uniform vec4 uUVTransform; // xy=scale, zw=offset

    out vec2 vUV;

    void main() {
        vUV = aPos * uUVTransform.xy + uUVTransform.zw;
        gl_Position = uProj * uView * uModel * vec4(aPos, 0.0, 1.0);
    }
    """;

    public static final String SPRITE_FRAG = """
    #version 330 core
    in vec2 vUV;
    out vec4 FragColor;

    uniform sampler2D uTex;
    uniform vec4 uTint;
    uniform bool uUseTex;

    void main() {
        vec4 texColor = uUseTex ? texture(uTex, vUV) : vec4(1.0);
        FragColor = texColor * uTint;
    }
    """;

    // ============================================
    // EFFECTS SHADER (For Special Effects)
    // ============================================

    public static final String EFFECTS_VERT = """
    #version 330 core

    layout(std140) uniform Camera {
        mat4 uProj;
        mat4 uView;
    };

    layout (location = 0) in vec2 aPos;
    layout (location = 1) in vec2 aUV;

    uniform mat4 uModel;
    uniform vec4 uUVTransform;

    out vec2 vUV;
    out vec2 vScreenPos;

    void main() {
        vUV = aPos * uUVTransform.xy + uUVTransform.zw;
        vec4 worldPos = uView * uModel * vec4(aPos, 0.0, 1.0);
        vScreenPos = worldPos.xy;
        gl_Position = uProj * worldPos;
    }
    """;

    public static final String EFFECTS_FRAG = """
    #version 330 core
    in vec2 vUV;
    in vec2 vScreenPos;
    out vec4 FragColor;

    uniform sampler2D uTex;
    uniform vec4  uTint;
    uniform vec2  uTexelSize;
    uniform float uTime;
    uniform int   uEffectFlags;
    uniform bool  uUseTex;

    // Effect bits
    const int GRAYSCALE = 1;
    const int PULSATE   = 2;
    const int WOBBLE    = 4;
    const int OUTLINE   = 8;
    const int FLASH     = 16;
    const int DISSOLVE  = 32;

    // ======= HELPER FUNCTIONS =======
    
    vec4 sampleTex(vec2 uv) {
        return texture(uTex, uv);
    }

    float luminance(vec3 color) {
        return dot(color, vec3(0.299, 0.587, 0.114));
    }

    // ======= EFFECT FUNCTIONS =======

    vec3 applyGrayscale(vec3 color) {
        float gray = luminance(color);
        return vec3(gray);
    }

    vec3 applyPulsate(vec3 color) {
        float pulse = 0.7 + 0.3 * sin(uTime * 6.28318);
        return color * pulse;
    }

    vec2 applyWobble(vec2 uv) {
        float wave = sin(uTime * 8.0 + vScreenPos.y * 0.1) * 0.01;
        return uv + vec2(wave, 0.0);
    }

    // IMPROVED: 8-directional outline sampling
    vec4 applyOutline(vec2 uv, vec4 baseColor) {
        if (baseColor.a > 0.5) {
            return baseColor; // Already solid, no outline needed
        }

        float maxAlpha = 0.0;
        vec2 offsets[8] = vec2[](
            vec2(-1, -1), vec2(0, -1), vec2(1, -1),
            vec2(-1,  0),              vec2(1,  0),
            vec2(-1,  1), vec2(0,  1), vec2(1,  1)
        );

        for (int i = 0; i < 8; i++) {
            vec2 sampleUV = uv + offsets[i] * uTexelSize;
            maxAlpha = max(maxAlpha, sampleTex(sampleUV).a);
        }

        if (maxAlpha > 0.5) {
            return vec4(1.0, 1.0, 1.0, 1.0); // White outline
        }

        return baseColor;
    }

    vec3 applyFlash(vec3 color) {
        float flash = sin(uTime * 20.0) * 0.5 + 0.5;
        return mix(color, vec3(1.0), flash * 0.5);
    }

    vec4 applyDissolve(vec4 color, vec2 uv) {
        float noise = fract(sin(dot(uv * 100.0, vec2(12.9898, 78.233))) * 43758.5453);
        float dissolve = sin(uTime * 2.0) * 0.5 + 0.5;
        if (noise > dissolve) {
            discard;
        }
        return color;
    }

    // ======= MAIN =======

    void main() {
        vec2 uv = vUV;
        
        // Apply UV effects first (changes sampling position)
        if ((uEffectFlags & WOBBLE) != 0) {
            uv = applyWobble(uv);
        }

        // Sample base color
        vec4 color = uUseTex ? sampleTex(uv) : vec4(1.0);

        // Apply outline (needs original UV and base color)
        if ((uEffectFlags & OUTLINE) != 0) {
            color = applyOutline(vUV, color);
        }

        // Apply color effects
        if ((uEffectFlags & GRAYSCALE) != 0) {
            color.rgb = applyGrayscale(color.rgb);
        }

        if ((uEffectFlags & PULSATE) != 0) {
            color.rgb = applyPulsate(color.rgb);
        }

        if ((uEffectFlags & FLASH) != 0) {
            color.rgb = applyFlash(color.rgb);
        }

        // Apply tint
        color *= uTint;

        // Apply alpha effects last
        if ((uEffectFlags & DISSOLVE) != 0) {
            color = applyDissolve(color, vUV);
        }

        FragColor = color;
    }
    """;

    // ============================================
    // POST-PROCESS SHADERS (Full-screen effects)
    // ============================================

    public static final String POSTPROCESS_VERT = """
    #version 330 core
    
    layout (location = 0) in vec2 aPos;
    layout (location = 1) in vec2 aUV;
    
    out vec2 vUV;
    
    void main() {
        vUV = aUV;
        gl_Position = vec4(aPos, 0.0, 1.0);
    }
    """;

    public static final String BLUR_FRAG = """
    #version 330 core
    in vec2 vUV;
    out vec4 FragColor;
    
    uniform sampler2D uTexture;
    uniform vec2 uTexelSize;
    uniform float uBlurAmount;
    
    void main() {
        vec4 color = vec4(0.0);
        float weights[5] = float[](0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);
        
        color += texture(uTexture, vUV) * weights[0];
        
        for(int i = 1; i < 5; i++) {
            vec2 offset = vec2(uTexelSize.x * i * uBlurAmount, 0.0);
            color += texture(uTexture, vUV + offset) * weights[i];
            color += texture(uTexture, vUV - offset) * weights[i];
        }
        
        FragColor = color;
    }
    """;

    public static final String CRT_FRAG = """
    #version 330 core
    in vec2 vUV;
    out vec4 FragColor;
    
    uniform sampler2D uTexture;
    uniform float uTime;
    
    void main() {
        vec2 uv = vUV;
        
        // Scanlines
        float scanline = sin(uv.y * 800.0) * 0.04;
        
        // Vignette
        vec2 center = uv - 0.5;
        float vignette = 1.0 - dot(center, center) * 0.5;
        
        // Screen curve
        uv = uv * 2.0 - 1.0;
        uv *= 1.0 + 0.05 * dot(uv, uv);
        uv = (uv + 1.0) * 0.5;
        
        vec4 color = texture(uTexture, uv);
        color.rgb -= scanline;
        color.rgb *= vignette;
        
        // Flicker
        color.rgb *= 0.95 + 0.05 * sin(uTime * 100.0);
        
        FragColor = color;
    }
    """;

    public static final String CHROMATIC_ABERRATION_FRAG = """
    #version 330 core
    in vec2 vUV;
    out vec4 FragColor;
    
    uniform sampler2D uTexture;
    uniform float uStrength;
    
    void main() {
        vec2 offset = (vUV - 0.5) * uStrength;
        
        float r = texture(uTexture, vUV + offset).r;
        float g = texture(uTexture, vUV).g;
        float b = texture(uTexture, vUV - offset).b;
        
        FragColor = vec4(r, g, b, 1.0);
    }
    """;

    // ============================================
    // PARTICLE SHADER (For effects)
    // ============================================

    public static final String PARTICLE_VERT = """
    #version 330 core

    layout(std140) uniform Camera {
        mat4 uProj;
        mat4 uView;
    };

    layout (location = 0) in vec2 aPos;
    layout (location = 1) in vec2 aUV;
    layout (location = 2) in vec4 aColor;
    layout (location = 3) in float aSize;

    out vec2 vUV;
    out vec4 vColor;

    void main() {
        vUV = aUV;
        vColor = aColor;
        
        vec4 worldPos = uView * vec4(aPos, 0.0, 1.0);
        gl_Position = uProj * worldPos;
        gl_PointSize = aSize;
    }
    """;

    public static final String PARTICLE_FRAG = """
    #version 330 core
    in vec2 vUV;
    in vec4 vColor;
    out vec4 FragColor;

    uniform sampler2D uTex;
    uniform bool uUseTex;

    void main() {
        vec4 color = vColor;
        
        if (uUseTex) {
            color *= texture(uTex, vUV);
        } else {
            // Circular particles
            vec2 center = gl_PointCoord - 0.5;
            float dist = length(center);
            if (dist > 0.5) discard;
            color.a *= 1.0 - (dist * 2.0);
        }
        
        FragColor = color;
    }
    """;

    public static final String BLOOM_EXTRACT_FRAG = """
    #version 330 core
    in vec2 vUV;
    out vec4 FragColor;
    
    uniform sampler2D uTexture;
    uniform float uThreshold;
    
    void main() {
        vec4 color = texture(uTexture, vUV);
        
        // Calculate brightness
        float brightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
        
        // Only keep colors above threshold (bright parts)
        if (brightness > uThreshold) {
            FragColor = color;
        } else {
            FragColor = vec4(0.0);
        }
    }
    """;

    public static final String BLOOM_COMBINE_FRAG = """
    #version 330 core
    in vec2 vUV;
    out vec4 FragColor;
    
    uniform sampler2D uScene;
    uniform sampler2D uBloom;
    uniform float uBloomIntensity;
    
    void main() {
        vec3 scene = texture(uScene, vUV).rgb;
        vec3 bloom = texture(uBloom, vUV).rgb;
        
        // Combine original scene with blurred bright parts
        vec3 result = scene + bloom * uBloomIntensity;
        
        FragColor = vec4(result, 1.0);
    }
    """;
}