// src/main/java/org/example/mario/states/MarioState.java
package org.example.mario.states;

import org.example.ecs.components.RigidBody;
import org.example.mario.MarioController;

/**
 * State Pattern: Interface for Mario's states
 */
public interface MarioState {
    void jump();
    void update(double dt);
    default void onEnter() {}
    default void onExit() {}
}

