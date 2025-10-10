// src/main/java/org/example/physics/Collision.java
package org.example.physics;

import org.example.ecs.GameObject;
import org.joml.Vector2f;

/**
 * Collision data passed to collision callbacks
 * Contains all info about a collision between two objects
 */
public final class Collision {
    public final GameObject self;
    public final GameObject other;
    public final BoxCollider selfCollider;
    public final BoxCollider otherCollider;

    // Collision normal (direction to resolve collision)
    public final Vector2f normal = new Vector2f();

    // Penetration depth
    public float penetration;

    // Contact point
    public final Vector2f contactPoint = new Vector2f();

    public Collision(GameObject self, GameObject other,
                     BoxCollider selfCollider, BoxCollider otherCollider) {
        this.self = self;
        this.other = other;
        this.selfCollider = selfCollider;
        this.otherCollider = otherCollider;
        calculateCollisionData();
    }

    private void calculateCollisionData() {
        // Calculate penetration on both axes
        float penX = selfCollider.getPenetrationX(otherCollider);
        float penY = selfCollider.getPenetrationY(otherCollider);

        // Resolve on the axis with least penetration
        if (penX < penY) {
            // Resolve on X axis
            penetration = penX;
            if (selfCollider.getCenter().x < otherCollider.getCenter().x) {
                normal.set(-1, 0);  // Push left
            } else {
                normal.set(1, 0);   // Push right
            }
        } else {
            // Resolve on Y axis
            penetration = penY;
            if (selfCollider.getCenter().y < otherCollider.getCenter().y) {
                normal.set(0, -1);  // Push down
            } else {
                normal.set(0, 1);   // Push up
            }
        }

        // Calculate contact point (midpoint of overlap)
        contactPoint.set(
                (Math.max(selfCollider.getMinX(), otherCollider.getMinX()) +
                        Math.min(selfCollider.getMaxX(), otherCollider.getMaxX())) * 0.5f,
                (Math.max(selfCollider.getMinY(), otherCollider.getMinY()) +
                        Math.min(selfCollider.getMaxY(), otherCollider.getMaxY())) * 0.5f
        );
    }

    /**
     * Check if collision is from above (standing on platform)
     */
    public boolean isFromAbove() {
        return normal.y > 0.5f;
    }

    /**
     * Check if collision is from below (hitting ceiling)
     */
    public boolean isFromBelow() {
        return normal.y < -0.5f;
    }

    /**
     * Check if collision is from left
     */
    public boolean isFromLeft() {
        return normal.x > 0.5f;
    }

    /**
     * Check if collision is from right
     */
    public boolean isFromRight() {
        return normal.x < -0.5f;
    }
}