// src/main/java/org/example/physics/ICollisionListener.java
package org.example.physics;

import org.example.ecs.GameObject;

/**
 * Interface for components that want to receive collision events
 * Implement this in any component that needs collision callbacks
 */
public interface ICollisionListener {
    /**
     * Called every frame while colliding
     */
    default void onCollision(Collision collision) {}

    /**
     * Called when collision starts
     */
    default void onCollisionEnter(GameObject other) {}

    /**
     * Called when collision ends
     */
    default void onCollisionExit(GameObject other) {}
}