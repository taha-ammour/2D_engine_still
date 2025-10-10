// src/main/java/org/example/ecs/components/RigidBody.java
package org.example.ecs.components;

import org.example.ecs.Component;
import org.joml.Vector2f;
import org.example.ecs.components.Transform;

/**
 * Physics Component: Tuned for HK + Celeste hybrid movement
 * Fast falls, terminal velocity, dash support
 */
public final class RigidBody extends Component {
    public final Vector2f velocity = new Vector2f();
    public float mass = 1f;
    public float drag = 0.0f;  // No drag for instant response
    public boolean useGravity = true;
    private static final float GRAVITY = -1400f;  // Even faster than HK, like Celeste
    public float maxFallSpeed = -700f;  // Higher terminal velocity

    @Override
    public void update(double dt) {
        Transform transform = owner.getComponent(Transform.class);
        if (transform == null) return;

        // Apply gravity (can be disabled during dash)
        if (useGravity) {
            velocity.y += GRAVITY * dt;
            // Cap fall speed
            if (velocity.y < maxFallSpeed) {
                velocity.y = maxFallSpeed;
            }
        }

        // Apply velocity
        transform.position.x += velocity.x * dt;
        transform.position.y += velocity.y * dt;

        // Apply drag only to horizontal velocity (if drag > 0)
        if (drag > 0) {
            velocity.x *= drag;
        }
    }

    public void addForce(float fx, float fy) {
        velocity.x += fx / mass;
        velocity.y += fy / mass;
    }

    public void setVelocity(float vx, float vy) {
        velocity.set(vx, vy);
    }
}