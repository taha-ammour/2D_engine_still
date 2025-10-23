// src/main/java/org/example/effects/FluidConfig.java
package org.example.effects;

import org.joml.Vector4f;

/**
 * Configuration for different fluid types
 * Provides presets for water, lava, slime, blood, etc.
 */
public final class FluidConfig {
    public float minSpeed = 50f;
    public float maxSpeed = 150f;
    public float spreadRadius = 10f;
    public Vector4f color = new Vector4f(0.3f, 0.5f, 1f, 0.8f);  // Blue water
    public float viscosity = 0.98f;
    public float lifetime = 3f;
    public float mass = 1f;
    public float size = 6f;

    // ===== PRESET FLUID TYPES =====

    /**
     * Water - splashes, drips, rain
     */
    public static FluidConfig water() {
        FluidConfig config = new FluidConfig();
        config.color.set(0.3f, 0.5f, 1f, 0.7f);  // Blue
        config.minSpeed = 80f;
        config.maxSpeed = 200f;
        config.viscosity = 0.98f;
        config.lifetime = 2f;
        config.size = 5f;
        return config;
    }

    /**
     * Lava - slow, thick, glowing
     */
    public static FluidConfig lava() {
        FluidConfig config = new FluidConfig();
        config.color.set(1f, 0.3f, 0f, 1f);  // Orange-red
        config.minSpeed = 20f;
        config.maxSpeed = 60f;
        config.viscosity = 0.85f;  // Very viscous
        config.lifetime = 5f;
        config.size = 8f;
        config.mass = 2f;
        return config;
    }

    /**
     * Slime - bouncy, sticky, colorful
     */
    public static FluidConfig slime() {
        FluidConfig config = new FluidConfig();
        config.color.set(0.3f, 1f, 0.3f, 0.9f);  // Green
        config.minSpeed = 40f;
        config.maxSpeed = 100f;
        config.viscosity = 0.90f;  // Sticky
        config.lifetime = 3f;
        config.size = 7f;
        config.mass = 1.2f;
        return config;
    }

    /**
     * Blood - thick, dark red
     */
    public static FluidConfig blood() {
        FluidConfig config = new FluidConfig();
        config.color.set(0.6f, 0f, 0f, 0.9f);  // Dark red
        config.minSpeed = 60f;
        config.maxSpeed = 120f;
        config.viscosity = 0.92f;
        config.lifetime = 4f;
        config.size = 4f;
        return config;
    }

    /**
     * Poison - toxic green, medium viscosity
     */
    public static FluidConfig poison() {
        FluidConfig config = new FluidConfig();
        config.color.set(0.5f, 1f, 0.2f, 0.8f);  // Toxic green
        config.minSpeed = 50f;
        config.maxSpeed = 130f;
        config.viscosity = 0.95f;
        config.lifetime = 3.5f;
        config.size = 5f;
        return config;
    }

    /**
     * Oil - dark, slow, very viscous
     */
    public static FluidConfig oil() {
        FluidConfig config = new FluidConfig();
        config.color.set(0.1f, 0.1f, 0.15f, 0.95f);  // Almost black
        config.minSpeed = 30f;
        config.maxSpeed = 70f;
        config.viscosity = 0.85f;
        config.lifetime = 6f;
        config.size = 6f;
        config.mass = 1.1f;
        return config;
    }

    /**
     * Magic/Energy - bright, fast, floaty
     */
    public static FluidConfig magic() {
        FluidConfig config = new FluidConfig();
        config.color.set(0.8f, 0.3f, 1f, 1f);  // Purple/pink
        config.minSpeed = 100f;
        config.maxSpeed = 250f;
        config.viscosity = 0.99f;
        config.lifetime = 1.5f;
        config.size = 4f;
        config.mass = 0.5f;
        return config;
    }

    /**
     * Acid - bright green, bubbling effect
     */
    public static FluidConfig acid() {
        FluidConfig config = new FluidConfig();
        config.color.set(0.3f, 1f, 0.3f, 0.9f);  // Bright green
        config.minSpeed = 60f;
        config.maxSpeed = 140f;
        config.viscosity = 0.96f;
        config.lifetime = 2.5f;
        config.size = 5f;
        return config;
    }

    /**
     * Sparks - fast, small, bright particles
     */
    public static FluidConfig sparks() {
        FluidConfig config = new FluidConfig();
        config.color.set(1f, 0.9f, 0.3f, 1f);  // Yellow-orange
        config.minSpeed = 150f;
        config.maxSpeed = 300f;
        config.viscosity = 0.99f;
        config.lifetime = 0.8f;
        config.size = 2f;
        config.mass = 0.3f;
        return config;
    }

    /**
     * Rain - fast falling droplets
     */
    public static FluidConfig rain() {
        FluidConfig config = new FluidConfig();
        config.color.set(0.7f, 0.8f, 1f, 0.6f);  // Light blue
        config.minSpeed = 200f;
        config.maxSpeed = 300f;
        config.viscosity = 0.99f;
        config.lifetime = 1f;
        config.size = 3f;
        config.mass = 0.8f;
        config.spreadRadius = 5f;
        return config;
    }
}