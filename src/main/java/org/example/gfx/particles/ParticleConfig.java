// src/main/java/org/example/gfx/particles/ParticleConfig.java
package org.example.gfx.particles;

import org.joml.Vector4f;

/**
 * Configuration for particle appearance and behavior
 */
public class ParticleConfig {
    // Velocity
    public float minSpeed = 50f;
    public float maxSpeed = 150f;
    public float angleMin = 0f;
    public float angleMax = 360f;

    // Lifetime
    public float minLifetime = 0.5f;
    public float maxLifetime = 1.5f;

    // Size
    public float minSize = 2f;
    public float maxSize = 8f;

    // Color
    public Vector4f startColor = new Vector4f(1, 1, 1, 1);
    public Vector4f endColor = new Vector4f(1, 1, 1, 0);
    public boolean useColorGradient = true;

    // Physics
    public float gravity = -200f;
    public float damping = 0.98f;
    public float minRotationSpeed = -5f;
    public float maxRotationSpeed = 5f;

    // Visual
    public boolean fadeOut = true;
    public boolean shrink = false;

    /**
     * Apply this config to a particle
     */
    public void apply(Particle p) {
        // Random velocity
        float angle = (float) Math.toRadians(random(angleMin, angleMax));
        float speed = random(minSpeed, maxSpeed);
        p.velocity.set(
                (float) Math.cos(angle) * speed,
                (float) Math.sin(angle) * speed
        );

        // Random lifetime
        p.lifetime = random(minLifetime, maxLifetime);

        // Random size
        p.size = random(minSize, maxSize);
        p.initialSize = p.size;

        // Color
        p.color.set(startColor);

        // Physics
        p.gravity = gravity;
        p.damping = damping;
        p.rotationSpeed = random(minRotationSpeed, maxRotationSpeed);

        // Visual effects
        p.fadeOut = fadeOut;
        p.shrink = shrink;
    }

    private float random(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }

    // ===== PRESET CONFIGURATIONS =====

    public static ParticleConfig jumpDust() {
        ParticleConfig config = new ParticleConfig();
        config.minSpeed = 30f;
        config.maxSpeed = 80f;
        config.angleMin = 45f;
        config.angleMax = 135f;
        config.minLifetime = 0.3f;
        config.maxLifetime = 0.6f;
        config.minSize = 3f;
        config.maxSize = 6f;
        config.startColor.set(0.9f, 0.9f, 0.95f, 1f);
        config.gravity = -100f;
        config.damping = 0.95f;
        config.fadeOut = true;
        config.shrink = true;
        return config;
    }

    public static ParticleConfig landDust() {
        ParticleConfig config = new ParticleConfig();
        config.minSpeed = 50f;
        config.maxSpeed = 120f;
        config.angleMin = 30f;
        config.angleMax = 150f;
        config.minLifetime = 0.4f;
        config.maxLifetime = 0.8f;
        config.minSize = 4f;
        config.maxSize = 8f;
        config.startColor.set(0.8f, 0.8f, 0.9f, 1f);
        config.gravity = -150f;
        config.damping = 0.92f;
        config.fadeOut = true;
        config.shrink = true;
        return config;
    }

    public static ParticleConfig dashTrail() {
        ParticleConfig config = new ParticleConfig();
        config.minSpeed = 0f;
        config.maxSpeed = 20f;
        config.angleMin = 0f;
        config.angleMax = 360f;
        config.minLifetime = 0.2f;
        config.maxLifetime = 0.4f;
        config.minSize = 6f;
        config.maxSize = 10f;
        config.startColor.set(0.3f, 0.7f, 1f, 0.8f);
        config.gravity = 0f;
        config.damping = 0.95f;
        config.fadeOut = true;
        config.shrink = true;
        return config;
    }

    public static ParticleConfig wallSlide() {
        ParticleConfig config = new ParticleConfig();
        config.minSpeed = 20f;
        config.maxSpeed = 60f;
        config.angleMin = 80f;
        config.angleMax = 100f;
        config.minLifetime = 0.3f;
        config.maxLifetime = 0.7f;
        config.minSize = 2f;
        config.maxSize = 5f;
        config.startColor.set(1f, 0.8f, 0.4f, 1f);
        config.gravity = -80f;
        config.damping = 0.96f;
        config.fadeOut = true;
        return config;
    }

    public static ParticleConfig coinCollect() {
        ParticleConfig config = new ParticleConfig();
        config.minSpeed = 80f;
        config.maxSpeed = 160f;
        config.angleMin = 0f;
        config.angleMax = 360f;
        config.minLifetime = 0.5f;
        config.maxLifetime = 1f;
        config.minSize = 3f;
        config.maxSize = 7f;
        config.startColor.set(1f, 0.9f, 0.2f, 1f);
        config.endColor.set(1f, 0.5f, 0f, 0f);
        config.gravity = -100f;
        config.damping = 0.94f;
        config.fadeOut = true;
        config.shrink = true;
        return config;
    }

    public static ParticleConfig blockHit() {
        ParticleConfig config = new ParticleConfig();
        config.minSpeed = 60f;
        config.maxSpeed = 140f;
        config.angleMin = -45f;
        config.angleMax = 45f;
        config.minLifetime = 0.4f;
        config.maxLifetime = 0.9f;
        config.minSize = 4f;
        config.maxSize = 8f;
        config.startColor.set(1f, 0.8f, 0.3f, 1f);
        config.gravity = -200f;
        config.damping = 0.93f;
        config.fadeOut = true;
        return config;
    }

    public static ParticleConfig powerUpGlow() {
        ParticleConfig config = new ParticleConfig();
        config.minSpeed = 30f;
        config.maxSpeed = 70f;
        config.angleMin = 60f;
        config.angleMax = 120f;
        config.minLifetime = 0.8f;
        config.maxLifetime = 1.5f;
        config.minSize = 4f;
        config.maxSize = 10f;
        config.startColor.set(1f, 1f, 0.3f, 0.9f);
        config.endColor.set(1f, 0.5f, 1f, 0f);
        config.gravity = 50f; // Float upward!
        config.damping = 0.97f;
        config.fadeOut = true;
        config.shrink = true;
        return config;
    }
}