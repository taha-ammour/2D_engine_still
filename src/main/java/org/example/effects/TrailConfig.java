// src/main/java/org/example/effects/TrailConfig.java
package org.example.effects;

import org.joml.Vector4f;

/**
 * Configuration for trail effects
 */
public final class TrailConfig {
    public Vector4f color = new Vector4f(1, 1, 1, 0.5f);
    public float width = 16f;
    public float height = 16f;
    public float lifetime = 0.3f;
    public float emissionInterval = 0.02f;  // How often to spawn trail segments
    public int maxSegments = 30;

    // ===== PRESET TRAIL TYPES =====

    /**
     * Fast dash trail - blue/white blur
     */
    public static TrailConfig dash() {
        TrailConfig config = new TrailConfig();
        config.color.set(0.5f, 0.8f, 1f, 0.6f);  // Light blue
        config.width = 20f;
        config.height = 32f;
        config.lifetime = 0.25f;
        config.emissionInterval = 0.015f;
        config.maxSegments = 25;
        return config;
    }

    /**
     * Speed trail - motion blur effect
     */
    public static TrailConfig speed() {
        TrailConfig config = new TrailConfig();
        config.color.set(1f, 1f, 1f, 0.4f);  // White blur
        config.width = 24f;
        config.height = 36f;
        config.lifetime = 0.2f;
        config.emissionInterval = 0.01f;
        config.maxSegments = 30;
        return config;
    }

    /**
     * Fire trail - burning path
     */
    public static TrailConfig fire() {
        TrailConfig config = new TrailConfig();
        config.color.set(1f, 0.5f, 0f, 0.7f);  // Orange
        config.width = 18f;
        config.height = 18f;
        config.lifetime = 0.5f;
        config.emissionInterval = 0.03f;
        config.maxSegments = 20;
        return config;
    }

    /**
     * Magic trail - glowing mystical
     */
    public static TrailConfig magic() {
        TrailConfig config = new TrailConfig();
        config.color.set(0.8f, 0.3f, 1f, 0.8f);  // Purple
        config.width = 16f;
        config.height = 16f;
        config.lifetime = 0.6f;
        config.emissionInterval = 0.025f;
        config.maxSegments = 35;
        return config;
    }

    /**
     * Projectile trail - weapon/bullet path
     */
    public static TrailConfig projectile() {
        TrailConfig config = new TrailConfig();
        config.color.set(1f, 1f, 0.3f, 0.7f);  // Yellow
        config.width = 8f;
        config.height = 8f;
        config.lifetime = 0.3f;
        config.emissionInterval = 0.02f;
        config.maxSegments = 20;
        return config;
    }

    /**
     * Ghost trail - afterimage effect
     */
    public static TrailConfig ghost() {
        TrailConfig config = new TrailConfig();
        config.color.set(0.8f, 0.8f, 1f, 0.3f);  // Pale blue
        config.width = 32f;
        config.height = 48f;
        config.lifetime = 0.4f;
        config.emissionInterval = 0.05f;
        config.maxSegments = 15;
        return config;
    }

    /**
     * Toxic trail - poison/acid path
     */
    public static TrailConfig toxic() {
        TrailConfig config = new TrailConfig();
        config.color.set(0.3f, 1f, 0.3f, 0.6f);  // Green
        config.width = 14f;
        config.height = 14f;
        config.lifetime = 0.8f;
        config.emissionInterval = 0.04f;
        config.maxSegments = 25;
        return config;
    }

    /**
     * Lightning trail - electric path
     */
    public static TrailConfig lightning() {
        TrailConfig config = new TrailConfig();
        config.color.set(0.8f, 1f, 1f, 0.9f);  // Cyan/white
        config.width = 12f;
        config.height = 12f;
        config.lifetime = 0.15f;
        config.emissionInterval = 0.01f;
        config.maxSegments = 20;
        return config;
    }
}