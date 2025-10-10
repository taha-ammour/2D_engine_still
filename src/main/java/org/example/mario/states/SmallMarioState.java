
// src/main/java/org/example/mario/states/SmallMarioState.java
package org.example.mario.states;

import org.example.ecs.components.RigidBody;
import org.example.mario.MarioController;

public final class SmallMarioState implements MarioState {
    private final MarioController controller;

    public SmallMarioState(MarioController controller) {
        this.controller = controller;
    }

    @Override
    public void jump() {
        if (!controller.isOnGround()) return;

        RigidBody rb = controller.getMario().getComponent(RigidBody.class);
        if (rb != null) {
            rb.velocity.y = controller.getJumpForce();
        }
    }

    @Override
    public void update(double dt) {
        // Small Mario specific logic (lower jump, faster fall, etc.)
    }

    @Override
    public void onEnter() {
        // Resize sprite, play shrink animation, etc.
    }
}
