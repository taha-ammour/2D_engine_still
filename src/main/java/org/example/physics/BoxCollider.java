// src/main/java/org/example/physics/BoxCollider.java
package org.example.physics;

import org.example.ecs.Component;
import org.example.ecs.components.Transform;
import org.joml.Vector2f;

/**
 * AABB (Axis-Aligned Bounding Box) Collider Component
 * Handles rectangular collision detection
 *
 * ✅ FIXED: Removed redundant updateBounds() call in update()
 */
public final class BoxCollider extends Component {
    // Size of the collider
    public float width;
    public float height;

    // Offset from transform position (for fine-tuning hitboxes)
    public final Vector2f offset = new Vector2f();

    // Collision properties
    public CollisionLayer layer = CollisionLayer.DEFAULT;
    public boolean isTrigger = false;  // Triggers don't resolve physics, just detect overlap
    public boolean isStatic = false;   // Static colliders don't move (ground, walls)

    // Cached bounds (updated each frame by CollisionSystem)
    private float minX, maxX, minY, maxY;

    public BoxCollider(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public BoxCollider(float width, float height, CollisionLayer layer) {
        this.width = width;
        this.height = height;
        this.layer = layer;
    }

    @Override
    public void update(double dt) {
        // ✅ FIXED: Don't update bounds here - CollisionSystem handles it
        // This prevents double-updates and keeps timing consistent
    }

    /**
     * Update cached bounds based on transform position
     * Called by CollisionSystem before collision detection
     */
    public void updateBounds() {
        Transform transform = owner.getComponent(Transform.class);
        if (transform == null) return;

        float centerX = transform.position.x + offset.x + width * 0.5f;
        float centerY = transform.position.y + offset.y + height * 0.5f;

        minX = centerX - width * 0.5f;
        maxX = centerX + width * 0.5f;
        minY = centerY - height * 0.5f;
        maxY = centerY + height * 0.5f;
    }

    /**
     * AABB overlap test with another collider
     */
    public boolean overlaps(BoxCollider other) {
        return minX < other.maxX && maxX > other.minX &&
                minY < other.maxY && maxY > other.minY;
    }

    /**
     * Get the center position of this collider
     */
    public Vector2f getCenter() {
        return new Vector2f((minX + maxX) * 0.5f, (minY + maxY) * 0.5f);
    }

    // Getters for bounds
    public float getMinX() { return minX; }
    public float getMaxX() { return maxX; }
    public float getMinY() { return minY; }
    public float getMaxY() { return maxY; }

    /**
     * Get penetration depth on X axis
     */
    public float getPenetrationX(BoxCollider other) {
        float overlapLeft = maxX - other.minX;
        float overlapRight = other.maxX - minX;
        return Math.min(overlapLeft, overlapRight);
    }

    /**
     * Get penetration depth on Y axis
     */
    public float getPenetrationY(BoxCollider other) {
        float overlapBottom = maxY - other.minY;
        float overlapTop = other.maxY - minY;
        return Math.min(overlapBottom, overlapTop);
    }
}