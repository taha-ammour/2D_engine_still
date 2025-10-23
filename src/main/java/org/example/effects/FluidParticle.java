// src/main/java/org/example/effects/FluidParticle.java
package org.example.effects;

import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * Fluid simulation particle with pressure, viscosity, and surface tension
 * Creates water, lava, and other fluid-like effects
 */
public final class FluidParticle {
    public final Vector2f position = new Vector2f();
    public final Vector2f velocity = new Vector2f();
    public final Vector2f force = new Vector2f();
    public final Vector4f color = new Vector4f();

    public float density = 0f;
    public float pressure = 0f;
    public float mass = 1f;
    public float size = 6f;

    // Fluid properties
    public float viscosity = 0.98f;  // How "thick" the fluid is
    public float lifetime = 5f;
    public float age = 0f;
    public boolean active = true;

    public void update(float dt) {
        if (!active) return;

        age += dt;
        if (age >= lifetime) {
            active = false;
            return;
        }

        // Apply forces
        velocity.add(force.x * dt, force.y * dt);
        force.set(0, 0);

        // Apply viscosity (damping)
        velocity.mul(viscosity);

        // Update position
        position.add(velocity.x * dt, velocity.y * dt);
    }

    public void reset() {
        position.set(0, 0);
        velocity.set(0, 0);
        force.set(0, 0);
        density = 0f;
        pressure = 0f;
        age = 0f;
        active = true;
    }

    public boolean isDead() {
        return !active || age >= lifetime;
    }
}