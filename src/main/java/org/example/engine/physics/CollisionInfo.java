package org.example.engine.physics;

import org.joml.Vector2f;

/**
 * Contains detailed information about a collision between two colliders.
 */
public class CollisionInfo {
    // The colliders involved in the collision
    public final Collider colliderA;
    public final Collider colliderB;

    // The normal vector of the collision (points from A to B)
    public final Vector2f normal;

    // The point where the collision occurred
    public final Vector2f contactPoint;

    // The penetration depth of the collision
    public final float depth;

    /**
     * Create a new collision info object
     */
    public CollisionInfo(Collider colliderA, Collider colliderB, Vector2f normal, Vector2f contactPoint, float depth) {
        this.colliderA = colliderA;
        this.colliderB = colliderB;
        this.normal = new Vector2f(normal);
        this.contactPoint = new Vector2f(contactPoint);
        this.depth = depth;
    }

    /**
     * Create a reversed version of this collision (swap A and B, negate normal)
     */
    public CollisionInfo reversed() {
        return new CollisionInfo(
                colliderB,
                colliderA,
                new Vector2f(-normal.x, -normal.y),
                new Vector2f(contactPoint),
                depth
        );
    }

    @Override
    public String toString() {
        return "CollisionInfo[" +
                "colliderA=" + (colliderA != null ? colliderA.getGameObject().getName() : "null") +
                ", colliderB=" + (colliderB != null ? colliderB.getGameObject().getName() : "null") +
                ", normal=" + normal +
                ", contactPoint=" + contactPoint +
                ", depth=" + depth +
                "]";
    }
}