package org.example.game;

import org.example.engine.SpriteManager;
import org.example.engine.core.Component;
import org.example.engine.input.InputSystem;
import org.example.engine.physics.Rigidbody;
import org.example.engine.rendering.Sprite;
import org.example.engine.rendering.Texture;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class PlayerController extends Component {
    private float moveSpeed = 200.0f;
    private Rigidbody rigidbody;
    private Sprite sprite;
    private boolean facingRight = true;
    private SpriteManager spriteManager;

    @Override
    protected void onInit() {
        rigidbody = getComponent(Rigidbody.class);
        sprite = getComponent(Sprite.class);
        if (rigidbody == null) {
            rigidbody = addComponent(new Rigidbody());
        }
        spriteManager = SpriteManager.getInstance();
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

        // Horizontal input (left/right)
        if (input.isKeyDown(GLFW.GLFW_KEY_A) || input.isKeyDown(GLFW.GLFW_KEY_LEFT)) {
            horizontalInput -= 1f;
        }
        if (input.isKeyDown(GLFW.GLFW_KEY_D) || input.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
            horizontalInput += 1f;
        }

        // Vertical input (up/down)
        if (input.isKeyDown(GLFW.GLFW_KEY_W) || input.isKeyDown(GLFW.GLFW_KEY_UP)) {
            verticalInput -= 1f;  // Negative Y is up
        }
        if (input.isKeyDown(GLFW.GLFW_KEY_S) || input.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
            verticalInput += 1f;  // Positive Y is down
        }

        // Create movement vector
        Vector2f velocity = new Vector2f(horizontalInput, verticalInput);

        // Normalize diagonal movement to maintain consistent speed
        if (velocity.length() > 0.01f) {
            // If moving diagonally, normalize and multiply by speed
            if (Math.abs(horizontalInput) > 0.01f && Math.abs(verticalInput) > 0.01f) {
                velocity.normalize();
            }

            // Apply movement speed
            velocity.mul(moveSpeed);

            // Emit particles when moving

        } else {
            // Stop particles when not moving

        }

        // Apply velocity to rigidbody
        rigidbody.setVelocity(velocity);

        // Update sprite direction based on movement
        updateSpriteDirection(horizontalInput, verticalInput);
    }

    private void updateSpriteDirection(float horizontalInput, float verticalInput) {
        if (sprite == null) return;

        // Handle horizontal flipping
        if (horizontalInput != 0) {
            facingRight = horizontalInput > 0;
            sprite.setFlipX(!facingRight);
        }

        Texture leftSP = spriteManager.createSprite("player_sprite_r").getTexture();
        Texture upTexture = spriteManager.createSprite("player_sprite_u").getTexture();
        Texture downTexture = spriteManager.createSprite("player_sprite_d").getTexture();



        // Change sprite based on direction (if you have different sprites for directions)
        if (Math.abs(horizontalInput) > Math.abs(verticalInput)) {
            // Horizontal movement is primary
            if (horizontalInput < 0) {
                // Left movement - could change sprite here if you have left/right sprites
                sprite.setFlipX(false);
                sprite.setTexture(leftSP);
            } else if (horizontalInput > 0) {
                // Right movement
                sprite.setFlipX(true);
                sprite.setTexture(leftSP);
            }
        } else if (Math.abs(verticalInput) > 0.01f) {
            // Vertical movement is primary
            if (verticalInput < 0) {
                // Up movement
                 sprite.setTexture(upTexture);
            } else {
                // Down movement
                 sprite.setTexture(downTexture);
            }
        }
    }
}