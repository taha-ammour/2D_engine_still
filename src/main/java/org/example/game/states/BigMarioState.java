
// src/main/java/org/example/mario/states/BigMarioState.java
package org.example.game.states;

import org.example.ecs.components.RigidBody;
import org.example.game.MarioController;

public final class BigMarioState implements MarioState {
    private final MarioController controller;

    public BigMarioState(MarioController controller) {
        this.controller = controller;
    }

    @Override
    public void jump() {
        if (!controller.isOnGround()) return;

        RigidBody rb = controller.getMario().getComponent(RigidBody.class);
        if (rb != null) {
            // Big Mario jumps higher
            rb.velocity.y = controller.getJumpForce() * 1.2f;
        }
    }

    @Override
    public void update(double dt) {
        // Big Mario specific logic
    }

    @Override
    public void onEnter() {
        // Resize sprite, play grow animation, etc.
    }
}