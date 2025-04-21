package org.example.engine.physics;

import org.example.engine.core.Transform;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Box-shaped collider component for 2D physics.
 */
public class BoxCollider extends Collider {
    private Vector2f size = new Vector2f(1.0f, 1.0f);

    /**
     * Create a box collider with default size (1x1)
     */
    public BoxCollider() {
        // Default size
    }

    /**
     * Create a box collider with the specified size
     */
    public BoxCollider(float width, float height) {
        this.size.set(width, height);
    }

    /**
     * Get the size of this box
     */
    public Vector2f getSize() {
        return new Vector2f(size);
    }

    /**
     * Set the size of this box
     */
    public void setSize(Vector2f size) {
        this.size.set(size);
        boundsDirty = true;
    }

    /**
     * Set the size of this box
     */
    public void setSize(float width, float height) {
        this.size.set(width, height);
        boundsDirty = true;
    }

    @Override
    protected void updateBounds() {
        Transform transform = getGameObject().getTransform();
        Vector3f position = transform.getPosition();
        Vector3f scale = transform.getScale();

        // Calculate half extents
        float halfWidth = (size.x * scale.x) * 0.5f;
        float halfHeight = (size.y * scale.y) * 0.5f;

        // Calculate center position with offset
        float centerX = position.x + getOffset().x;
        float centerY = position.y + getOffset().y;

        // Set bounds
        min.set(centerX - halfWidth, centerY - halfHeight);
        max.set(centerX + halfWidth, centerY + halfHeight);

        boundsDirty = false;
    }

    @Override
    public boolean containsPoint(Vector2f point) {
        if (boundsDirty) {
            updateBounds();
        }

        return point.x >= min.x && point.x <= max.x &&
                point.y >= min.y && point.y <= max.y;
    }

    @Override
    public Vector2f getClosestPoint(Vector2f point) {
        if (boundsDirty) {
            updateBounds();
        }

        // Clamp point to the box bounds
        return new Vector2f(
                Math.max(min.x, Math.min(point.x, max.x)),
                Math.max(min.y, Math.min(point.y, max.y))
        );
    }

    @Override
    public RaycastHit raycast(Vector2f origin, Vector2f direction, float maxDistance) {
        if (boundsDirty) {
            updateBounds();
        }

        // Optimization: quick AABB test with infinite ray
        if (!rayIntersectsAABB(origin, direction)) {
            return null;
        }

        // Compute intersection with each face of the box
        float tMin = Float.NEGATIVE_INFINITY;
        float tMax = Float.POSITIVE_INFINITY;
        Vector2f normal = new Vector2f();

        // X axis slab intersection
        if (Math.abs(direction.x) < 0.0001f) {
            // Ray is parallel to slab. No hit if origin is outside slab
            if (origin.x < min.x || origin.x > max.x) {
                return null;
            }
        } else {
            // Compute intersection t value of ray with near and far plane of slab
            float invDirX = 1.0f / direction.x;
            float t1 = (min.x - origin.x) * invDirX;
            float t2 = (max.x - origin.x) * invDirX;

            // Swap if necessary
            if (t1 > t2) {
                float temp = t1;
                t1 = t2;
                t2 = temp;
            }

            // Update tMin and tMax
            if (t1 > tMin) {
                tMin = t1;
                normal.set(-1, 0);
                if (direction.x > 0) normal.x = 1;
            }
            tMax = Math.min(tMax, t2);

            // Exit if no intersection found
            if (tMin > tMax) {
                return null;
            }
        }

        // Y axis slab intersection
        if (Math.abs(direction.y) < 0.0001f) {
            // Ray is parallel to slab. No hit if origin is outside slab
            if (origin.y < min.y || origin.y > max.y) {
                return null;
            }
        } else {
            // Compute intersection t value of ray with near and far plane of slab
            float invDirY = 1.0f / direction.y;
            float t1 = (min.y - origin.y) * invDirY;
            float t2 = (max.y - origin.y) * invDirY;

            // Swap if necessary
            if (t1 > t2) {
                float temp = t1;
                t1 = t2;
                t2 = temp;
            }

            // Update tMin and tMax
            if (t1 > tMin) {
                tMin = t1;
                normal.set(0, -1);
                if (direction.y > 0) normal.y = 1;
            }
            tMax = Math.min(tMax, t2);

            // Exit if no intersection found
            if (tMin > tMax) {
                return null;
            }
        }

        // Check if intersection is within distance limit
        if (tMin < 0 || tMin > maxDistance) {
            return null;
        }

        // Compute intersection point
        Vector2f point = new Vector2f(
                origin.x + direction.x * tMin,
                origin.y + direction.y * tMin
        );

        return new RaycastHit(point, normal, tMin);
    }

    /**
     * Helper method to check if a ray intersects with this box's AABB
     */
    private boolean rayIntersectsAABB(Vector2f origin, Vector2f direction) {
        float tMin = Float.NEGATIVE_INFINITY;
        float tMax = Float.POSITIVE_INFINITY;

        // X axis slab
        if (Math.abs(direction.x) < 0.0001f) {
            if (origin.x < min.x || origin.x > max.x) {
                return false;
            }
        } else {
            float invDirX = 1.0f / direction.x;
            float t1 = (min.x - origin.x) * invDirX;
            float t2 = (max.x - origin.x) * invDirX;

            if (t1 > t2) {
                float temp = t1;
                t1 = t2;
                t2 = temp;
            }

            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);

            if (tMin > tMax) {
                return false;
            }
        }

        // Y axis slab
        if (Math.abs(direction.y) < 0.0001f) {
            if (origin.y < min.y || origin.y > max.y) {
                return false;
            }
        } else {
            float invDirY = 1.0f / direction.y;
            float t1 = (min.y - origin.y) * invDirY;
            float t2 = (max.y - origin.y) * invDirY;

            if (t1 > t2) {
                float temp = t1;
                t1 = t2;
                t2 = temp;
            }

            tMin = Math.max(tMin, t1);
            tMax = Math.min(tMax, t2);

            if (tMin > tMax) {
                return false;
            }
        }

        return true;
    }
}