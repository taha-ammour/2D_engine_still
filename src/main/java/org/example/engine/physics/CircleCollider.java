package org.example.engine.physics;

import org.example.engine.core.Transform;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Circle-shaped collider component for 2D physics.
 */
public class CircleCollider extends Collider {
    private float radius = 0.5f;

    /**
     * Create a circle collider with default radius (0.5)
     */
    public CircleCollider() {
        // Default radius
    }

    /**
     * Create a circle collider with the specified radius
     */
    public CircleCollider(float radius) {
        this.radius = Math.max(0.01f, radius);
    }

    /**
     * Get the radius of this circle
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Set the radius of this circle
     */
    public void setRadius(float radius) {
        this.radius = Math.max(0.01f, radius);
        boundsDirty = true;
    }

    @Override
    protected void updateBounds() {
        Transform transform = getGameObject().getTransform();
        Vector3f position = transform.getPosition();
        Vector3f scale = transform.getScale();

        // Calculate scaled radius
        float scaledRadius = radius * Math.max(scale.x, scale.y);

        // Calculate center position with offset
        float centerX = position.x + getOffset().x;
        float centerY = position.y + getOffset().y;

        // Set bounds to enclosing AABB
        min.set(centerX - scaledRadius, centerY - scaledRadius);
        max.set(centerX + scaledRadius, centerY + scaledRadius);

        boundsDirty = false;
    }

    @Override
    public boolean containsPoint(Vector2f point) {
        if (boundsDirty) {
            updateBounds();
        }

        Transform transform = getGameObject().getTransform();
        Vector3f position = transform.getPosition();
        Vector3f scale = transform.getScale();

        // Calculate scaled radius
        float scaledRadius = radius * Math.max(scale.x, scale.y);

        // Calculate center position with offset
        float centerX = position.x + getOffset().x;
        float centerY = position.y + getOffset().y;

        // Calculate distance squared to center
        float dx = point.x - centerX;
        float dy = point.y - centerY;
        float distanceSquared = dx * dx + dy * dy;

        // Compare with radius squared
        return distanceSquared <= scaledRadius * scaledRadius;
    }

    @Override
    public Vector2f getClosestPoint(Vector2f point) {
        if (boundsDirty) {
            updateBounds();
        }

        Transform transform = getGameObject().getTransform();
        Vector3f position = transform.getPosition();
        Vector3f scale = transform.getScale();

        // Calculate scaled radius
        float scaledRadius = radius * Math.max(scale.x, scale.y);

        // Calculate center position with offset
        float centerX = position.x + getOffset().x;
        float centerY = position.y + getOffset().y;

        // Calculate direction from center to point
        float dx = point.x - centerX;
        float dy = point.y - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance <= scaledRadius) {
            // Point is inside circle, return the point
            return new Vector2f(point);
        } else {
            // Point is outside circle, return closest point on circumference
            float nx = dx / distance;
            float ny = dy / distance;

            return new Vector2f(
                    centerX + nx * scaledRadius,
                    centerY + ny * scaledRadius
            );
        }
    }

    @Override
    public RaycastHit raycast(Vector2f origin, Vector2f direction, float maxDistance) {
        if (boundsDirty) {
            updateBounds();
        }

        Transform transform = getGameObject().getTransform();
        Vector3f position = transform.getPosition();
        Vector3f scale = transform.getScale();

        // Calculate scaled radius
        float scaledRadius = radius * Math.max(scale.x, scale.y);

        // Calculate center position with offset
        float centerX = position.x + getOffset().x;
        float centerY = position.y + getOffset().y;

        // Calculate vector from ray origin to circle center
        float dx = centerX - origin.x;
        float dy = centerY - origin.y;

        // Project this vector onto the ray direction
        float projectionLength = dx * direction.x + dy * direction.y;

        // Calculate the closest point on the ray to the circle center
        Vector2f closestPoint = new Vector2f(
                origin.x + projectionLength * direction.x,
                origin.y + projectionLength * direction.y
        );

        // Calculate distance from the closest point to the circle center
        float closestDistX = closestPoint.x - centerX;
        float closestDistY = closestPoint.y - centerY;
        float closestDistSquared = closestDistX * closestDistX + closestDistY * closestDistY;

        // Check if ray misses the circle
        if (closestDistSquared > scaledRadius * scaledRadius) {
            return null;
        }

        // Calculate the half-chord distance from the closest point to the intersection points
        float halfChordDistance = (float) Math.sqrt(scaledRadius * scaledRadius - closestDistSquared);

        // Calculate the distance to the first intersection point
        float intersectionDist = projectionLength - halfChordDistance;

        // If we're looking from inside the circle, use the far intersection
        if (intersectionDist < 0) {
            intersectionDist = projectionLength + halfChordDistance;
        }

        // Check if intersection is within distance limit
        if (intersectionDist < 0 || intersectionDist > maxDistance) {
            return null;
        }

        // Calculate intersection point
        Vector2f point = new Vector2f(
                origin.x + direction.x * intersectionDist,
                origin.y + direction.y * intersectionDist
        );

        // Calculate normal at intersection point (from center to intersection point)
        Vector2f normal = new Vector2f(
                point.x - centerX,
                point.y - centerY
        ).normalize();

        return new RaycastHit(point, normal, intersectionDist);
    }
}