package org.example.engine.resource;

import org.example.engine.animation.Animation;
import org.example.engine.audio.AudioSystem;
import org.example.engine.audio.Sound;
import org.example.engine.rendering.ShaderManager;
import org.example.engine.rendering.Texture;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized resource management system.
 * Handles loading, caching, and unloading of assets like textures, sounds, and animations.
 */
public class ResourceManager {
    private static ResourceManager instance;

    // Resource caches
    private final Map<String, Texture> textures = new HashMap<>();
    private final Map<String, Sound> sounds = new HashMap<>();
    private final Map<String, Animation> animations = new HashMap<>();

    // Reference counting for automatic resource management
    private final Map<String, Integer> referenceCount = new HashMap<>();

    /**
     * Get the singleton instance
     */
    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    /**
     * Private constructor for singleton pattern
     */
    private ResourceManager() {
    }

    /**
     * Load a texture
     * @param name Resource identifier
     * @param path File path
     * @return Loaded texture
     */
    public Texture loadTexture(String name, String path) {
        // Return cached texture if exists
        if (textures.containsKey(name)) {
            incrementReferenceCount(name);
            return textures.get(name);
        }

        try {
            // Load the texture (implementation depends on your Texture class)
            Texture texture = new Texture(path);
            textures.put(name, texture);
            referenceCount.put(name, 1);
            return texture;
        } catch (Exception e) {
            System.err.println("Failed to load texture: " + path);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a loaded texture by name
     */
    public Texture getTexture(String name) {
        Texture texture = textures.get(name);
        if (texture != null) {
            incrementReferenceCount(name);
        }
        return texture;
    }

    /**
     * Load a sound
     * @param name Resource identifier
     * @param path File path
     * @return Loaded sound
     */
    public Sound loadSound(String name, String path) {
        // Return cached sound if exists
        if (sounds.containsKey(name)) {
            incrementReferenceCount(name);
            return sounds.get(name);
        }

        // Load the sound
        Sound sound = AudioSystem.getInstance().loadSound(name, path);
        if (sound != null) {
            sounds.put(name, sound);
            referenceCount.put(name, 1);
        }
        return sound;
    }

    /**
     * Get a loaded sound by name
     */
    public Sound getSound(String name) {
        Sound sound = sounds.get(name);
        if (sound != null) {
            incrementReferenceCount(name);
        }
        return sound;
    }

    /**
     * Load a shader
     * @param name Resource identifier
     * @param vertexPath Vertex shader path
     * @param fragmentPath Fragment shader path
     * @return Loaded shader
     */
    public ShaderManager.Shader loadShader(String name, String vertexPath, String fragmentPath) {
        // Load through the shader manager (it handles caching)
        ShaderManager.Shader shader = ShaderManager.getInstance().loadShader(name, vertexPath, fragmentPath);
        if (shader != null) {
            referenceCount.put("shader:" + name, 1);
        }
        return shader;
    }

    /**
     * Register an animation
     * @param name Resource identifier
     * @param animation Animation to register
     */
    public void registerAnimation(String name, Animation animation) {
        animations.put(name, animation);
        referenceCount.put("animation:" + name, 1);
    }

    /**
     * Get a registered animation
     */
    public Animation getAnimation(String name) {
        Animation animation = animations.get(name);
        if (animation != null) {
            incrementReferenceCount("animation:" + name);
        }
        return animation;
    }

    /**
     * Increment reference count for a resource
     */
    private void incrementReferenceCount(String name) {
        if (referenceCount.containsKey(name)) {
            referenceCount.put(name, referenceCount.get(name) + 1);
        } else {
            referenceCount.put(name, 1);
        }
    }

    /**
     * Release a resource (decrement reference count)
     * @param name Resource identifier
     */
    public void releaseResource(String name) {
        if (!referenceCount.containsKey(name)) {
            return;
        }

        int count = referenceCount.get(name) - 1;
        referenceCount.put(name, count);

        // If reference count is zero, unload the resource
        if (count <= 0) {
            unloadResource(name);
        }
    }

    /**
     * Unload a specific resource
     */
    private void unloadResource(String name) {
        // Handle different resource types
        if (textures.containsKey(name)) {
            Texture texture = textures.remove(name);
            texture.dispose(); // Assuming Texture has a dispose method
            referenceCount.remove(name);
        } else if (sounds.containsKey(name)) {
            Sound sound = sounds.remove(name);
            sound.cleanup();
            referenceCount.remove(name);
        } else if (name.startsWith("shader:")) {
            String shaderName = name.substring(7);
            // Shader manager handles the cleanup
            referenceCount.remove(name);
        } else if (name.startsWith("animation:")) {
            String animName = name.substring(10);
            animations.remove(animName);
            referenceCount.remove(name);
        }
    }

    /**
     * Unload all resources
     */
    public void unloadAll() {
        // Clean up textures
        for (Texture texture : textures.values()) {
            texture.dispose();
        }
        textures.clear();

        // Clean up sounds
        for (Sound sound : sounds.values()) {
            sound.cleanup();
        }
        sounds.clear();

        // Clean up animations
        animations.clear();

        // Clean up reference counts
        referenceCount.clear();
    }

    /**
     * Check if hot-reloading is supported for a resource type
     */
    public boolean supportsHotReload(String resourceType) {
        return "shader".equals(resourceType); // Only shaders support hot-reloading for now
    }

    /**
     * Reload all resources of a specific type
     */
    public void reloadResourceType(String resourceType) {
        if ("shader".equals(resourceType)) {
            ShaderManager.getInstance().reloadAll();
        }
    }
}