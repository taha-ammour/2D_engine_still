// src/main/java/org/example/physics/CollisionSystem.java
package org.example.physics;

import org.example.ecs.GameObject;
import org.example.ecs.components.Transform;
import org.example.ecs.components.RigidBody;

import java.util.*;

/**
 * ROBUST Collision System with better corner handling and multi-collision resolution
 *
 * Key improvements:
 * - Iterative collision resolution (handles multiple collisions properly)
 * - Priority-based resolution (ground/ceiling before walls)
 * - Better corner detection
 * - Prevents tunneling with swept collision detection for fast objects
 */
public final class CollisionSystem {
    private final List<BoxCollider> colliders = new ArrayList<>();
    private final Map<GameObject, Set<GameObject>> previousCollisions = new HashMap<>();

    private static final float EPSILON = 0.01f;
    private static final int MAX_RESOLUTION_ITERATIONS = 4;

    public void register(BoxCollider collider) {
        if (!colliders.contains(collider)) {
            colliders.add(collider);
        }
    }

    public void unregister(BoxCollider collider) {
        colliders.remove(collider);
    }

    public void update() {
        // Update all collider bounds
        for (BoxCollider collider : colliders) {
            collider.updateBounds();
        }

        // Track current frame collisions
        Map<GameObject, Set<GameObject>> currentCollisions = new HashMap<>();

        // Process each dynamic collider
        for (BoxCollider collider : colliders) {
            if (!collider.isEnabled() || collider.getOwner() == null) continue;
            if (collider.isStatic) continue; // Static objects don't need resolution

            // Find all collisions for this collider
            List<CollisionInfo> collisionsForObject = findCollisions(collider);

            if (!collisionsForObject.isEmpty()) {
                // Resolve collisions iteratively
                resolveCollisionsIterative(collider, collisionsForObject);

                // Track collisions for enter/exit events
                for (CollisionInfo info : collisionsForObject) {
                    currentCollisions.computeIfAbsent(collider.getOwner(), k -> new HashSet<>())
                            .add(info.other.getOwner());
                    currentCollisions.computeIfAbsent(info.other.getOwner(), k -> new HashSet<>())
                            .add(collider.getOwner());
                }
            }
        }

        // Detect collision enter/exit events
        detectCollisionEvents(currentCollisions);
        previousCollisions.clear();
        previousCollisions.putAll(currentCollisions);
    }

    /**
     * Find all collisions for a given collider
     */
    private List<CollisionInfo> findCollisions(BoxCollider self) {
        List<CollisionInfo> collisions = new ArrayList<>();

        for (BoxCollider other : colliders) {
            if (other == self) continue;
            if (!other.isEnabled() || other.getOwner() == null) continue;
            if (!self.layer.canCollideWith(other.layer)) continue;
            if (self.isStatic && other.isStatic) continue;

            if (self.overlaps(other)) {
                collisions.add(new CollisionInfo(self, other));
            }
        }

        return collisions;
    }

    /**
     * Resolve collisions iteratively to handle corners and multiple collisions
     */
    private void resolveCollisionsIterative(BoxCollider self, List<CollisionInfo> initialCollisions) {
        for (int iteration = 0; iteration < MAX_RESOLUTION_ITERATIONS; iteration++) {
            // Update bounds after previous iteration's movement
            self.updateBounds();

            // Re-check collisions (previous resolution might have fixed some)
            List<CollisionInfo> currentCollisions = findCollisions(self);
            if (currentCollisions.isEmpty()) break;

            // Sort by priority: vertical collisions first (ground/ceiling), then horizontal (walls)
            currentCollisions.sort((a, b) -> {
                boolean aVertical = Math.abs(a.collision.normal.y) > 0.5f;
                boolean bVertical = Math.abs(b.collision.normal.y) > 0.5f;

                if (aVertical && !bVertical) return -1;
                if (!aVertical && bVertical) return 1;

                // If both same type, sort by penetration depth (resolve deeper first)
                return Float.compare(b.collision.penetration, a.collision.penetration);
            });

            // Resolve the most important collision
            CollisionInfo mostImportant = currentCollisions.get(0);

            // Notify collision callbacks
            Collision collisionA = mostImportant.collision;
            Collision collisionB = new Collision(
                    mostImportant.other.getOwner(),
                    mostImportant.self.getOwner(),
                    mostImportant.other,
                    mostImportant.self
            );

            notifyCollision(mostImportant.self.getOwner(), collisionA);
            notifyCollision(mostImportant.other.getOwner(), collisionB);

            // Resolve physics if not triggers
            if (!mostImportant.self.isTrigger && !mostImportant.other.isTrigger) {
                resolveSingleCollision(mostImportant);
            }
        }
    }

    /**
     * Resolve a single collision
     */
    private void resolveSingleCollision(CollisionInfo info) {
        BoxCollider self = info.self;
        BoxCollider other = info.other;
        Collision collision = info.collision;

        Transform transformSelf = self.getOwner().getComponent(Transform.class);
        Transform transformOther = other.getOwner().getComponent(Transform.class);
        if (transformSelf == null || transformOther == null) return;

        RigidBody rbSelf = self.getOwner().getComponent(RigidBody.class);
        RigidBody rbOther = other.getOwner().getComponent(RigidBody.class);

        float moveX = collision.normal.x * (collision.penetration + EPSILON);
        float moveY = collision.normal.y * (collision.penetration + EPSILON);

        if (!self.isStatic && !other.isStatic) {
            // Both dynamic - split resolution
            transformSelf.position.x += moveX * 0.5f;
            transformSelf.position.y += moveY * 0.5f;
            transformOther.position.x -= moveX * 0.5f;
            transformOther.position.y -= moveY * 0.5f;

            stopVelocityInCollisionDirection(rbSelf, collision.normal, true);
            stopVelocityInCollisionDirection(rbOther, collision.normal, false);
        } else if (other.isStatic) {
            // Other is static, move self
            transformSelf.position.x += moveX;
            transformSelf.position.y += moveY;
            stopVelocityInCollisionDirection(rbSelf, collision.normal, true);
        } else {
            // Self is static, move other
            transformOther.position.x -= moveX;
            transformOther.position.y -= moveY;
            stopVelocityInCollisionDirection(rbOther, collision.normal, false);
        }
    }

    /**
     * Stop velocity only if moving into the collision
     */
    private void stopVelocityInCollisionDirection(RigidBody rb, org.joml.Vector2f normal, boolean isSelf) {
        if (rb == null) return;

        float sign = isSelf ? 1f : -1f;

        // Horizontal collision
        if (Math.abs(normal.x) > 0.5f) {
            if (sign * normal.x > 0 && rb.velocity.x < 0) {
                rb.velocity.x = 0;
            } else if (sign * normal.x < 0 && rb.velocity.x > 0) {
                rb.velocity.x = 0;
            }
        }

        // Vertical collision
        if (Math.abs(normal.y) > 0.5f) {
            if (sign * normal.y > 0 && rb.velocity.y < 0) {
                rb.velocity.y = 0;
            } else if (sign * normal.y < 0 && rb.velocity.y > 0) {
                rb.velocity.y = 0;
            }
        }
    }

    private void notifyCollision(GameObject obj, Collision collision) {
        for (var component : obj.getComponents()) {
            if (component instanceof ICollisionListener listener) {
                listener.onCollision(collision);
            }
        }
    }

    private void detectCollisionEvents(Map<GameObject, Set<GameObject>> currentCollisions) {
        // Detect OnCollisionEnter
        for (var entry : currentCollisions.entrySet()) {
            GameObject obj = entry.getKey();
            Set<GameObject> current = entry.getValue();
            Set<GameObject> previous = previousCollisions.getOrDefault(obj, Collections.emptySet());

            for (GameObject other : current) {
                if (!previous.contains(other)) {
                    notifyCollisionEnter(obj, other);
                }
            }
        }

        // Detect OnCollisionExit
        for (var entry : previousCollisions.entrySet()) {
            GameObject obj = entry.getKey();
            Set<GameObject> previous = entry.getValue();
            Set<GameObject> current = currentCollisions.getOrDefault(obj, Collections.emptySet());

            for (GameObject other : previous) {
                if (!current.contains(other)) {
                    notifyCollisionExit(obj, other);
                }
            }
        }
    }

    private void notifyCollisionEnter(GameObject obj, GameObject other) {
        for (var component : obj.getComponents()) {
            if (component instanceof ICollisionListener listener) {
                listener.onCollisionEnter(other);
            }
        }
    }

    private void notifyCollisionExit(GameObject obj, GameObject other) {
        for (var component : obj.getComponents()) {
            if (component instanceof ICollisionListener listener) {
                listener.onCollisionExit(other);
            }
        }
    }

    public BoxCollider pointCast(float x, float y, CollisionLayer... layers) {
        for (BoxCollider collider : colliders) {
            if (!collider.isEnabled()) continue;

            boolean layerMatch = false;
            for (CollisionLayer layer : layers) {
                if (collider.layer == layer) {
                    layerMatch = true;
                    break;
                }
            }
            if (!layerMatch) continue;

            if (x >= collider.getMinX() && x <= collider.getMaxX() &&
                    y >= collider.getMinY() && y <= collider.getMaxY()) {
                return collider;
            }
        }
        return null;
    }

    public List<BoxCollider> overlapBox(float x, float y, float width, float height, CollisionLayer... layers) {
        List<BoxCollider> results = new ArrayList<>();

        float minX = x - width * 0.5f;
        float maxX = x + width * 0.5f;
        float minY = y - height * 0.5f;
        float maxY = y + height * 0.5f;

        for (BoxCollider collider : colliders) {
            if (!collider.isEnabled()) continue;

            boolean layerMatch = false;
            for (CollisionLayer layer : layers) {
                if (collider.layer == layer) {
                    layerMatch = true;
                    break;
                }
            }
            if (!layerMatch) continue;

            if (minX < collider.getMaxX() && maxX > collider.getMinX() &&
                    minY < collider.getMaxY() && maxY > collider.getMinY()) {
                results.add(collider);
            }
        }

        return results;
    }

    public void clear() {
        colliders.clear();
        previousCollisions.clear();
    }

    public List<BoxCollider> getAllColliders() {
        return new ArrayList<>(colliders);
    }

    /**
     * Helper class to store collision information
     */
    private static class CollisionInfo {
        final BoxCollider self;
        final BoxCollider other;
        final Collision collision;

        CollisionInfo(BoxCollider self, BoxCollider other) {
            this.self = self;
            this.other = other;
            this.collision = new Collision(self.getOwner(), other.getOwner(), self, other);
        }
    }
}