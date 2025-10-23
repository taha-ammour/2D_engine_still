// src/main/java/org/example/gfx/particles/Particle.java
package org.example.gfx.particles;

import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * Individual particle with physics and lifetime
 */
public final class Particle {
    public final Vector2f position = new Vector2f();
    public final Vector2f velocity = new Vector2f();
    public final Vector4f color = new Vector4f(1, 1, 1, 1);

    public float size = 4f;
    public float lifetime = 1f;
    public float age = 0f;
    public float rotation = 0f;
    public float rotationSpeed = 0f;
    public float gravity = -200f;
    public float damping = 0.98f;

    // Visual effects
    public boolean fadeOut = true;
    public boolean shrink = false;
    public float initialSize;

    public Particle(float x, float y) {
        position.set(x, y);
        initialSize = size;
    }

    public void update(float dt) {
        age += dt;

        // Apply velocity
        position.x += velocity.x * dt;
        position.y += velocity.y * dt;

        // Apply gravity
        velocity.y += gravity * dt;

        // Apply damping
        velocity.mul(damping);

        // Apply rotation
        rotation += rotationSpeed * dt;

        // Fade out
        if (fadeOut) {
            float lifePercent = age / lifetime;
            color.w = 1f - lifePercent;
        }

        // Shrink
        if (shrink) {
            float lifePercent = age / lifetime;
            size = initialSize * (1f - lifePercent);
        }
    }

    public boolean isDead() {
        return age >= lifetime;
    }

    public float getLifePercent() {
        return Math.min(1f, age / lifetime);
    }
}