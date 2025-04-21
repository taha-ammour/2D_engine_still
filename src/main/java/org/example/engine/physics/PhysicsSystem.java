package org.example.engine.physics;

import org.example.engine.core.GameObject;
import org.example.engine.core.Transform;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.*;

/**
 * Physics system that handles collision detection and response.
 * Uses a spatial partitioning system for efficient collision checks.
 */
public class PhysicsSystem {
    private static PhysicsSystem instance;

    // All physics objects in the system
    private final List<Collider> colliders = new ArrayList<>();

    // Spatial partitioning for efficient collision detection
    private final SpatialGrid spatialGrid;

    // Cache of collision pairs to avoid duplicate checks
    private final Map<Long, CollisionInfo> collisionCache = new HashMap<>();

    // Physics simulation properties
    private Vector2f gravity = new Vector2f(0, 9.8f);
    private float fixedTimeStep = 1.0f / 60.0f;
    private float accumulator = 0.0f;
    private boolean continuousCollisionDetection = false;

    // Layer-based collision matrix
    private final boolean[][] collisionMatrix = new boolean[32][32];

    // Debugging
    private boolean debugDraw = false;

    /**
     * Singleton instance accessor
     */
    public static PhysicsSystem getInstance() {
        if (instance == null) {
            instance = new PhysicsSystem();
        }
        return instance;
    }

    /**
     * Private constructor for singleton
     */
    private PhysicsSystem() {
        // Create spatial grid with default cell size
        spatialGrid = new SpatialGrid(64.0f);

        // Enable all collisions by default
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                collisionMatrix[i][j] = true;
            }
        }
    }

    /**
     * Add a collider to the physics system
     */
    public void addCollider(Collider collider) {
        if (collider == null || colliders.contains(collider)) return;

        colliders.add(collider);
        spatialGrid.addObject(collider);
    }

    /**
     * Remove a collider from the physics system
     */
    public void removeCollider(Collider collider) {
        if (collider == null) return;

        colliders.remove(collider);
        spatialGrid.removeObject(collider);
    }

    /**
     * Set whether two layers should collide
     */
    public void setLayerCollision(int layer1, int layer2, boolean shouldCollide) {
        if (layer1 < 0 || layer1 >= 32 || layer2 < 0 || layer2 >= 32) return;

        collisionMatrix[layer1][layer2] = shouldCollide;
        collisionMatrix[layer2][layer1] = shouldCollide;
    }

    /**
     * Check if two layers should collide
     */
    public boolean shouldLayersCollide(int layer1, int layer2) {
        if (layer1 < 0 || layer1 >= 32 || layer2 < 0 || layer2 >= 32) return false;

        return collisionMatrix[layer1][layer2];
    }

    /**
     * Update the physics system with the current deltaTime
     */
    public void update(float deltaTime) {
        // Update spatial grid
        updateSpatialGrid();

        // Add time to accumulator
        accumulator += deltaTime;

        // Run fixed timestep simulation
        while (accumulator >= fixedTimeStep) {
            fixedUpdate(fixedTimeStep);
            accumulator -= fixedTimeStep;
        }
    }

    /**
     * Fixed timestep update for physics
     */
    private void fixedUpdate(float timeStep) {
        // Update rigidbodies
        for (Collider collider : colliders) {
            Rigidbody rigidbody = collider.getGameObject().getComponent(Rigidbody.class);
            if (rigidbody != null && rigidbody.isActive()) {
                updateRigidbody(rigidbody, timeStep);
            }
        }

        // Detect and resolve collisions
        detectCollisions();
        resolveCollisions();
    }

    /**
     * Update a rigidbody's position and velocity
     */
    private void updateRigidbody(Rigidbody rigidbody, float timeStep) {
        if (!rigidbody.isKinematic()) {
            // Apply gravity
            if (rigidbody.useGravity()) {
                rigidbody.addForce(gravity.x * rigidbody.getMass(), gravity.y * rigidbody.getMass());
            }

            // Update velocity based on forces
            Vector2f acceleration = new Vector2f(
                    rigidbody.getForce().x / rigidbody.getMass(),
                    rigidbody.getForce().y / rigidbody.getMass()
            );

            rigidbody.getVelocity().add(
                    acceleration.x * timeStep,
                    acceleration.y * timeStep
            );

            // Apply drag
            float drag = rigidbody.getDrag();
            if (drag > 0.0f) {
                rigidbody.getVelocity().mul(1.0f - (drag * timeStep));
            }

            // Clear accumulated forces
            rigidbody.clearForces();
        }

        // Update position based on velocity
        Transform transform = rigidbody.getGameObject().getTransform();
        Vector3f position = transform.getPosition();

        position.add(
                rigidbody.getVelocity().x * timeStep,
                rigidbody.getVelocity().y * timeStep,
                0
        );

        transform.setPosition(position);
    }

    /**
     * Update the spatial grid with current object positions
     */
    private void updateSpatialGrid() {
        spatialGrid.clear();

        for (Collider collider : colliders) {
            if (collider.isActive()) {
                spatialGrid.addObject(collider);
            }
        }
    }

    /**
     * Detect collisions between objects
     */
    private void detectCollisions() {
        // Clear previous collision info
        collisionCache.clear();

        // Check for collisions
        for (Collider collider : colliders) {
            if (!collider.isActive()) continue;

            // Get potential collision candidates from spatial grid
            List<Collider> candidates = spatialGrid.getPotentialCollisions(collider);

            for (Collider other : candidates) {
                if (!other.isActive() || collider == other) continue;

                // Check if layers should collide
                if (!shouldLayersCollide(collider.getLayer(), other.getLayer())) continue;

                // Create a unique ID for this collision pair
                long pairId = getPairId(collider.getId(), other.getId());

                // Skip if we've already checked this pair
                if (collisionCache.containsKey(pairId)) continue;

                // Check for collision
                CollisionInfo collision = checkCollision(collider, other);

                if (collision != null) {
                    // Store collision information
                    collisionCache.put(pairId, collision);

                    // Trigger collision events
                    collider.onCollisionEnter(collision);
                    other.onCollisionEnter(collision.reversed());
                }
            }
        }
    }

    /**
     * Resolve detected collisions
     */
    private void resolveCollisions() {
        for (CollisionInfo collision : collisionCache.values()) {
            // Get rigidbodies
            Rigidbody rbA = collision.colliderA.getGameObject().getComponent(Rigidbody.class);
            Rigidbody rbB = collision.colliderB.getGameObject().getComponent(Rigidbody.class);

            // Skip collision resolution if both objects are static
            if ((rbA == null || rbA.isKinematic()) && (rbB == null || rbB.isKinematic())) continue;

            // Resolve the collision
            resolveCollision(collision, rbA, rbB);
        }
    }

    /**
     * Resolve a specific collision
     */
    private void resolveCollision(CollisionInfo collision, Rigidbody rbA, Rigidbody rbB) {
        // Calculate masses
        float massA = rbA != null ? rbA.getMass() : Float.POSITIVE_INFINITY;
        float massB = rbB != null ? rbB.getMass() : Float.POSITIVE_INFINITY;

        // Calculate mass ratio for position correction
        float totalMass = massA + massB;
        float ratioA = massB / totalMass;
        float ratioB = massA / totalMass;

        // Handle infinite mass cases
        if (Float.isInfinite(massA)) {
            ratioA = 0.0f;
            ratioB = 1.0f;
        } else if (Float.isInfinite(massB)) {
            ratioA = 1.0f;
            ratioB = 0.0f;
        }

        // Positional correction to prevent sinking
        Vector2f correction = new Vector2f(collision.normal).mul(collision.depth * 0.8f); // 80% correction

        Transform transformA = collision.colliderA.getGameObject().getTransform();
        Transform transformB = collision.colliderB.getGameObject().getTransform();

        if (rbA == null || !rbA.isKinematic()) {
            Vector3f posA = transformA.getPosition();
            posA.sub(correction.x * ratioA, correction.y * ratioA, 0);
            transformA.setPosition(posA);
        }

        if (rbB == null || !rbB.isKinematic()) {
            Vector3f posB = transformB.getPosition();
            posB.add(correction.x * ratioB, correction.y * ratioB, 0);
            transformB.setPosition(posB);
        }

        // Skip velocity resolution if one object is a trigger
        if (collision.colliderA.isTrigger() || collision.colliderB.isTrigger()) return;

        // Velocity resolution
        if (rbA != null && rbB != null) {
            // Calculate relative velocity
            Vector2f relativeVel = new Vector2f(rbB.getVelocity()).sub(rbA.getVelocity());

            // Calculate velocity along normal
            float normalVel = relativeVel.dot(collision.normal);

            // Skip resolution if objects are moving away from each other
            if (normalVel > 0) return;

            // Calculate restitution (bounciness)
            float restitution = Math.min(rbA.getRestitution(), rbB.getRestitution());

            // Calculate impulse scalar
            float impulseMag = -(1.0f + restitution) * normalVel;
            impulseMag /= (1.0f / massA) + (1.0f / massB);

            // Apply impulse
            Vector2f impulse = new Vector2f(collision.normal).mul(impulseMag);

            if (!rbA.isKinematic()) {
                rbA.getVelocity().sub(new Vector2f(impulse).div(massA));
            }

            if (!rbB.isKinematic()) {
                rbB.getVelocity().add(new Vector2f(impulse).div(massB));
            }

            // Apply friction
            float friction = (rbA.getFriction() + rbB.getFriction()) * 0.5f;

            if (friction > 0.0f) {
                // Calculate tangent vector
                Vector2f tangent = new Vector2f(relativeVel);
                tangent.sub(new Vector2f(collision.normal).mul(normalVel));

                if (tangent.length() > 0.0001f) {
                    tangent.normalize();

                    // Calculate tangent impulse magnitude
                    float tangentImpulseMag = -relativeVel.dot(tangent);
                    tangentImpulseMag /= (1.0f / massA) + (1.0f / massB);

                    // Clamp tangent impulse by friction
                    float maxTangentImpulse = impulseMag * friction;
                    tangentImpulseMag = Math.max(-maxTangentImpulse, Math.min(tangentImpulseMag, maxTangentImpulse));

                    // Apply tangent impulse
                    Vector2f tangentImpulse = new Vector2f(tangent).mul(tangentImpulseMag);

                    if (!rbA.isKinematic()) {
                        rbA.getVelocity().sub(new Vector2f(tangentImpulse).div(massA));
                    }

                    if (!rbB.isKinematic()) {
                        rbB.getVelocity().add(new Vector2f(tangentImpulse).div(massB));
                    }
                }
            }
        }
    }

    /**
     * Check for collision between two colliders
     */
    private CollisionInfo checkCollision(Collider a, Collider b) {
        // Dispatch to appropriate collision detection algorithm based on collider types
        if (a instanceof BoxCollider && b instanceof BoxCollider) {
            return checkBoxBoxCollision((BoxCollider) a, (BoxCollider) b);
        } else if (a instanceof CircleCollider && b instanceof CircleCollider) {
            return checkCircleCircleCollision((CircleCollider) a, (CircleCollider) b);
        } else if (a instanceof BoxCollider && b instanceof CircleCollider) {
            return checkBoxCircleCollision((BoxCollider) a, (CircleCollider) b);
        } else if (a instanceof CircleCollider && b instanceof BoxCollider) {
            CollisionInfo collision = checkBoxCircleCollision((BoxCollider) b, (CircleCollider) a);
            return collision != null ? collision.reversed() : null;
        }

        // Fallback to AABB overlap test
        return checkAABBOverlap(a, b);
    }

    /**
     * Check for collision between two box colliders
     */
    private CollisionInfo checkBoxBoxCollision(BoxCollider a, BoxCollider b) {
        // Get positions and sizes in world space
        Transform transformA = a.getGameObject().getTransform();
        Transform transformB = b.getGameObject().getTransform();

        Vector3f posA = transformA.getPosition();
        Vector3f posB = transformB.getPosition();

        Vector2f sizeA = a.getSize();
        Vector2f sizeB = b.getSize();

        // Calculate half sizes
        float halfWidthA = sizeA.x * 0.5f * transformA.getScale().x;
        float halfHeightA = sizeA.y * 0.5f * transformA.getScale().y;

        float halfWidthB = sizeB.x * 0.5f * transformB.getScale().x;
        float halfHeightB = sizeB.y * 0.5f * transformB.getScale().y;

        // Calculate center points
        float centerAX = posA.x + a.getOffset().x;
        float centerAY = posA.y + a.getOffset().y;

        float centerBX = posB.x + b.getOffset().x;
        float centerBY = posB.y + b.getOffset().y;

        // Calculate overlap on each axis
        float overlapX = halfWidthA + halfWidthB - Math.abs(centerAX - centerBX);
        float overlapY = halfHeightA + halfHeightB - Math.abs(centerAY - centerBY);

        // Check if there is an overlap
        if (overlapX > 0 && overlapY > 0) {
            // Determine collision normal and depth
            Vector2f normal = new Vector2f();
            float depth;

            // Use the smaller overlap as the penetration depth
            if (overlapX < overlapY) {
                depth = overlapX;
                normal.x = centerAX < centerBX ? -1.0f : 1.0f;
                normal.y = 0.0f;
            } else {
                depth = overlapY;
                normal.x = 0.0f;
                normal.y = centerAY < centerBY ? -1.0f : 1.0f;
            }

            // Create contact point (approximate)
            Vector2f contactPoint = new Vector2f(
                    centerBX - normal.x * halfWidthB,
                    centerBY - normal.y * halfHeightB
            );

            return new CollisionInfo(a, b, normal, contactPoint, depth);
        }

        return null; // No collision
    }

    /**
     * Check for collision between two circle colliders
     */
    private CollisionInfo checkCircleCircleCollision(CircleCollider a, CircleCollider b) {
        // Get positions and radii in world space
        Transform transformA = a.getGameObject().getTransform();
        Transform transformB = b.getGameObject().getTransform();

        Vector3f posA = transformA.getPosition();
        Vector3f posB = transformB.getPosition();

        float radiusA = a.getRadius() * Math.max(transformA.getScale().x, transformA.getScale().y);
        float radiusB = b.getRadius() * Math.max(transformB.getScale().x, transformB.getScale().y);

        // Calculate center points
        float centerAX = posA.x + a.getOffset().x;
        float centerAY = posA.y + a.getOffset().y;

        float centerBX = posB.x + b.getOffset().x;
        float centerBY = posB.y + b.getOffset().y;

        // Calculate distance between centers
        float deltaX = centerBX - centerAX;
        float deltaY = centerBY - centerAY;
        float distanceSquared = deltaX * deltaX + deltaY * deltaY;

        // Check if circles are overlapping
        float radiusSum = radiusA + radiusB;
        if (distanceSquared < radiusSum * radiusSum) {
            float distance = (float) Math.sqrt(distanceSquared);

            // Avoid division by zero
            Vector2f normal;
            if (distance > 0.0001f) {
                normal = new Vector2f(deltaX / distance, deltaY / distance);
            } else {
                // If circles are at the same position, use default direction
                normal = new Vector2f(0.0f, 1.0f);
            }

            float depth = radiusSum - distance;

            // Calculate contact point
            Vector2f contactPoint = new Vector2f(
                    centerAX + normal.x * radiusA,
                    centerAY + normal.y * radiusA
            );

            return new CollisionInfo(a, b, normal, contactPoint, depth);
        }

        return null; // No collision
    }

    /**
     * Check for collision between a box and a circle collider
     */
    private CollisionInfo checkBoxCircleCollision(BoxCollider box, CircleCollider circle) {
        // Get positions and sizes in world space
        Transform boxTransform = box.getGameObject().getTransform();
        Transform circleTransform = circle.getGameObject().getTransform();

        Vector3f boxPos = boxTransform.getPosition();
        Vector3f circlePos = circleTransform.getPosition();

        Vector2f boxSize = box.getSize();
        float circleRadius = circle.getRadius() * Math.max(circleTransform.getScale().x, circleTransform.getScale().y);

        // Calculate box half sizes
        float halfWidth = boxSize.x * 0.5f * boxTransform.getScale().x;
        float halfHeight = boxSize.y * 0.5f * boxTransform.getScale().y;

        // Calculate center points
        float boxCenterX = boxPos.x + box.getOffset().x;
        float boxCenterY = boxPos.y + box.getOffset().y;

        float circleCenterX = circlePos.x + circle.getOffset().x;
        float circleCenterY = circlePos.y + circle.getOffset().y;

        // Find the closest point on the box to the circle center
        float closestX = Math.max(boxCenterX - halfWidth, Math.min(circleCenterX, boxCenterX + halfWidth));
        float closestY = Math.max(boxCenterY - halfHeight, Math.min(circleCenterY, boxCenterY + halfHeight));

        // Calculate distance between closest point and circle center
        float deltaX = closestX - circleCenterX;
        float deltaY = closestY - circleCenterY;
        float distanceSquared = deltaX * deltaX + deltaY * deltaY;

        // Check if circle is overlapping with box
        if (distanceSquared < circleRadius * circleRadius) {
            float distance = (float) Math.sqrt(distanceSquared);

            // Calculate normal
            Vector2f normal;
            if (distance > 0.0001f) {
                normal = new Vector2f(-deltaX / distance, -deltaY / distance);
            } else {
                // Default direction if point is at center
                normal = new Vector2f(0.0f, -1.0f);
            }

            float depth = circleRadius - distance;

            // If the closest point is inside the box, adjust normal to point to nearest edge
            if (closestX > boxCenterX - halfWidth + 0.0001f &&
                    closestX < boxCenterX + halfWidth - 0.0001f &&
                    closestY > boxCenterY - halfHeight + 0.0001f &&
                    closestY < boxCenterY + halfHeight - 0.0001f) {

                // Find closest edge
                float edgeDistX = Math.min(Math.abs(closestX - (boxCenterX - halfWidth)), Math.abs(closestX - (boxCenterX + halfWidth)));
                float edgeDistY = Math.min(Math.abs(closestY - (boxCenterY - halfHeight)), Math.abs(closestY - (boxCenterY + halfHeight)));

                if (edgeDistX < edgeDistY) {
                    normal.x = closestX < boxCenterX ? -1.0f : 1.0f;
                    normal.y = 0.0f;
                    depth = edgeDistX + circleRadius;
                } else {
                    normal.x = 0.0f;
                    normal.y = closestY < boxCenterY ? -1.0f : 1.0f;
                    depth = edgeDistY + circleRadius;
                }
            }

            // Contact point is on circle's surface in direction of normal
            Vector2f contactPoint = new Vector2f(
                    circleCenterX - normal.x * circleRadius,
                    circleCenterY - normal.y * circleRadius
            );

            return new CollisionInfo(box, circle, normal, contactPoint, depth);
        }

        return null; // No collision
    }

    /**
     * Fallback collision test using AABB (Axis-Aligned Bounding Box)
     */
    private CollisionInfo checkAABBOverlap(Collider a, Collider b) {
        // Get AABB bounds
        Vector2f minA = a.getMin();
        Vector2f maxA = a.getMax();
        Vector2f minB = b.getMin();
        Vector2f maxB = b.getMax();

        // Check for overlap
        if (maxA.x > minB.x && minA.x < maxB.x && maxA.y > minB.y && minA.y < maxB.y) {
            // Calculate overlap on each axis
            float overlapX = Math.min(maxA.x, maxB.x) - Math.max(minA.x, minB.x);
            float overlapY = Math.min(maxA.y, maxB.y) - Math.max(minA.y, minB.y);

            // Calculate centers
            Vector2f centerA = new Vector2f((minA.x + maxA.x) * 0.5f, (minA.y + maxA.y) * 0.5f);
            Vector2f centerB = new Vector2f((minB.x + maxB.x) * 0.5f, (minB.y + maxB.y) * 0.5f);

            // Determine normal based on smaller overlap
            Vector2f normal = new Vector2f();
            float depth;

            if (overlapX < overlapY) {
                depth = overlapX;
                normal.x = centerA.x < centerB.x ? -1.0f : 1.0f;
                normal.y = 0.0f;
            } else {
                depth = overlapY;
                normal.x = 0.0f;
                normal.y = centerA.y < centerB.y ? -1.0f : 1.0f;
            }

            // Approximate contact point
            Vector2f contactPoint = new Vector2f(
                    centerB.x - normal.x * (maxB.x - minB.x) * 0.5f,
                    centerB.y - normal.y * (maxB.y - minB.y) * 0.5f
            );

            return new CollisionInfo(a, b, normal, contactPoint, depth);
        }

        return null; // No collision
    }

    /**
     * Generate a unique ID for a collision pair
     */
    private long getPairId(UUID idA, UUID idB) {
        // Use hashCode as a simple way to get a long from UUID
        long hashA = idA.hashCode();
        long hashB = idB.hashCode();

        // Ensure commutative property (A,B same as B,A)
        return hashA < hashB ? hashA | (hashB << 32) : hashB | (hashA << 32);
    }

    /**
     * Check if a point intersects with any collider
     */
    public CollisionInfo raycast(Vector2f point, int layerMask) {
        for (Collider collider : colliders) {
            if (!collider.isActive()) continue;

            // Skip if layer is not in mask
            if ((layerMask & (1 << collider.getLayer())) == 0) continue;

            if (collider.containsPoint(point)) {
                // Create a dummy collision info for point intersection
                Vector2f normal = new Vector2f(0, -1); // Default normal
                return new CollisionInfo(null, collider, normal, point, 0.0f);
            }
        }

        return null;
    }

    /**
     * Cast a ray and find the first collision
     */
    public CollisionInfo raycast(Vector2f origin, Vector2f direction, float maxDistance, int layerMask) {
        direction = new Vector2f(direction).normalize();
        CollisionInfo closestHit = null;
        float closestDistance = maxDistance;

        for (Collider collider : colliders) {
            if (!collider.isActive()) continue;

            // Skip if layer is not in mask
            if ((layerMask & (1 << collider.getLayer())) == 0) continue;

            // Raycast against collider
            RaycastHit hit = collider.raycast(origin, direction, maxDistance);

            if (hit != null && hit.distance < closestDistance) {
                closestDistance = hit.distance;

                // Create collision info from raycast hit
                closestHit = new CollisionInfo(
                        null,
                        collider,
                        hit.normal,
                        hit.point,
                        0.0f
                );
            }
        }

        return closestHit;
    }

    /**
     * Get all colliders intersecting an area
     */
    public List<Collider> overlapArea(Vector2f min, Vector2f max, int layerMask) {
        List<Collider> results = new ArrayList<>();

        for (Collider collider : colliders) {
            if (!collider.isActive()) continue;

            // Skip if layer is not in mask
            if ((layerMask & (1 << collider.getLayer())) == 0) continue;

            // Check AABB overlap
            Vector2f colliderMin = collider.getMin();
            Vector2f colliderMax = collider.getMax();

            if (max.x > colliderMin.x && min.x < colliderMax.x &&
                    max.y > colliderMin.y && min.y < colliderMax.y) {
                results.add(collider);
            }
        }

        return results;
    }

    /**
     * Get all colliders intersecting a circle
     */
    public List<Collider> overlapCircle(Vector2f center, float radius, int layerMask) {
        List<Collider> results = new ArrayList<>();
        float radiusSquared = radius * radius;

        for (Collider collider : colliders) {
            if (!collider.isActive()) continue;

            // Skip if layer is not in mask
            if ((layerMask & (1 << collider.getLayer())) == 0) continue;

            // Check distance to closest point on collider
            Vector2f closestPoint = collider.getClosestPoint(center);

            float deltaX = closestPoint.x - center.x;
            float deltaY = closestPoint.y - center.y;
            float distanceSquared = deltaX * deltaX + deltaY * deltaY;

            if (distanceSquared <= radiusSquared) {
                results.add(collider);
            }
        }

        return results;
    }
}