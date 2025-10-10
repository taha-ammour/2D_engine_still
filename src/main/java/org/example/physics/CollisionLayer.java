// src/main/java/org/example/physics/CollisionLayer.java
package org.example.physics;

/**
 * Collision layers for filtering what can collide with what
 * Uses bit flags for efficient collision matrix
 */
public enum CollisionLayer {
    DEFAULT(1),
    PLAYER(2),
    ENEMY(4),
    GROUND(8),
    PLATFORM(16),
    PROJECTILE(32),
    TRIGGER(64),
    ITEM(128);

    public final int mask;

    CollisionLayer(int mask) {
        this.mask = mask;
    }

    /**
     * Check if this layer can collide with another layer
     * Customize collision rules here
     */
    public boolean canCollideWith(CollisionLayer other) {
        // Define collision rules
        return switch (this) {
            case PLAYER -> other == GROUND || other == PLATFORM ||
                    other == ENEMY || other == ITEM || other == TRIGGER;
            case ENEMY -> other == PLAYER || other == GROUND ||
                    other == PLATFORM || other == PROJECTILE;
            case PROJECTILE -> other == ENEMY || other == GROUND;
            case GROUND, PLATFORM -> other == PLAYER || other == ENEMY || other == PROJECTILE;
            case TRIGGER -> other == PLAYER;
            case ITEM -> other == PLAYER;
            default -> true;
        };
    }
}