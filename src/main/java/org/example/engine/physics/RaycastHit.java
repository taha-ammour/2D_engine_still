package org.example.engine.physics;

import org.joml.Vector2f;

/**
 * Contains information about a raycast hit.
 */
public class RaycastHit {
    // The point where the ray hit the collider
    public final Vector2f point;

    // The normal vector at the hit point
    public final Vector2f normal;

    // The distance from the ray origin to the hit point
    public final float distance;

    /**
     * Create a new raycast hit object
     */
    public RaycastHit(Vector2f point, Vector2f normal, float distance) {
        this.point = new Vector2f(point);
        this.normal = new Vector2f(normal);
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "RaycastHit[" +
                "point=" + point +
                ", normal=" + normal +
                ", distance=" + distance +
                "]";
    }
}