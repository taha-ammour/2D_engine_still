// src/main/java/org/example/gfx/ShaderManager.java
package org.example.gfx;

import java.util.HashMap;
import java.util.Map;

/**
 * Flyweight Pattern: Manages and caches shader programs
 * Single Responsibility: Handles shader lifecycle
 */
public final class ShaderManager implements AutoCloseable {
    private final Map<String, Shader> shaders = new HashMap<>();
    private boolean initialized = false;

    public enum ShaderType {
        SPRITE,
        EFFECTS,
        PARTICLE,
        POSTPROCESS_BLUR,
        POSTPROCESS_CRT,
        POSTPROCESS_CHROMATIC
    }

    public ShaderManager() {
        // Don't initialize shaders here - no OpenGL context yet!
    }

    /**
     * MUST be called after OpenGL context is created
     */
    public void init() {
        if (initialized) return;
        initializeDefaultShaders();
        initialized = true;
    }

    private void initializeDefaultShaders() {
        // Basic sprite shader (fast, for most sprites)
        shaders.put("sprite", new Shader(
                Shaders.SPRITE_VERT,
                Shaders.SPRITE_FRAG,
                false
        ));

        // Effects shader (for special effects)
        shaders.put("effects", new Shader(
                Shaders.EFFECTS_VERT,
                Shaders.EFFECTS_FRAG,
                false
        ));

        // Particle shader
        shaders.put("particle", new Shader(
                Shaders.PARTICLE_VERT,
                Shaders.PARTICLE_FRAG,
                false
        ));

        // Post-process shaders
        shaders.put("blur", new Shader(
                Shaders.POSTPROCESS_VERT,
                Shaders.BLUR_FRAG,
                false
        ));

        shaders.put("crt", new Shader(
                Shaders.POSTPROCESS_VERT,
                Shaders.CRT_FRAG,
                false
        ));

        shaders.put("chromatic", new Shader(
                Shaders.POSTPROCESS_VERT,
                Shaders.CHROMATIC_ABERRATION_FRAG,
                false
        ));
        shaders.put("bloom_extract", new Shader(
                Shaders.POSTPROCESS_VERT,
                Shaders.BLOOM_EXTRACT_FRAG,
                false
        ));

        shaders.put("bloom_combine", new Shader(
                Shaders.POSTPROCESS_VERT,
                Shaders.BLOOM_COMBINE_FRAG,
                false
        ));
    }

    public Shader get(String name) {
        Shader shader = shaders.get(name);
        if (shader == null) {
            throw new IllegalArgumentException("Shader not found: " + name);
        }
        return shader;
    }

    public Shader getDefault() {
        return get("sprite");
    }

    public void loadCustomShader(String name, String vertPath, String fragPath) {
        if (shaders.containsKey(name)) {
            System.err.println("Warning: Overwriting shader: " + name);
            shaders.get(name).close();
        }
        shaders.put(name, new Shader(vertPath, fragPath, true));
    }

    @Override
    public void close() {
        for (Shader shader : shaders.values()) {
            shader.close();
        }
        shaders.clear();
    }
}