package org.example.game;

import org.example.engine.core.Component;
import org.example.engine.input.InputSystem;
import org.example.engine.physics.Rigidbody;
import org.example.engine.rendering.Sprite;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class PlayerController extends Component {
    private float moveSpeed = 200.0f;
    private Rigidbody rigidbody;
    private Sprite sprite;
    private boolean facingRight = true;

    @Override
    protected void onInit() {
        rigidbody = getComponent(Rigidbody.class);
        sprite = getComponent(Sprite.class);
        if (rigidbody == null) {
            rigidbody = addComponent(new Rigidbody());
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        handleMovement(deltaTime);
    }

    private void handleMovement(float deltaTime) {
        InputSystem input = InputSystem.getInstance();

        // Get horizontal and vertical input
        float horizontalInput = 0f;
        float verticalInput = 0f;

        if (input.isKeyDown(GLFW.GLFW_KEY_A) || input.isKeyDown(GLFW.GLFW_KEY_LEFT)) {
            horizontalInput -= 1f;
        }
        if (input.isKeyDown(GLFW.GLFW_KEY_D) || input.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
            horizontalInput += 1f;
        }
        if (input.isKeyDown(GLFW.GLFW_KEY_W) || input.isKeyDown(GLFW.GLFW_KEY_UP)) {
            verticalInput -= 1f;
        }
        if (input.isKeyDown(GLFW.GLFW_KEY_S) || input.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
            verticalInput += 1f;
        }

        // Apply movement
        Vector2f velocity = new Vector2f(horizontalInput, verticalInput);
        if (velocity.length() > 0) {
            velocity.normalize().mul(moveSpeed * deltaTime);
        }

        rigidbody.setVelocity(velocity.x, velocity.y);

        // Update sprite direction
        if (horizontalInput != 0 && sprite != null) {
            boolean newFacingRight = horizontalInput > 0;
            if (facingRight != newFacingRight) {
                facingRight = newFacingRight;
                sprite.setFlipX(!facingRight);
            }
        }
    }
}