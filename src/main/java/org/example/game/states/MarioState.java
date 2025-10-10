// src/main/java/org/example/mario/states/MarioState.java
package org.example.game.states;

/**
 * State Pattern: Interface for Mario's states
 */
public interface MarioState {
    void jump();
    void update(double dt);
    default void onEnter() {}
    default void onExit() {}
}

