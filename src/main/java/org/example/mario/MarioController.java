// src/main/java/org/example/mario/MarioController.java
package org.example.mario;

import org.example.ecs.Component;
import org.example.ecs.GameObject;
import org.example.ecs.components.*;
import org.example.mario.states.*;
import org.example.physics.Collision;
import org.example.physics.CollisionLayer;
import org.example.physics.CollisionSystem;
import org.example.physics.ICollisionListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Mario Controller with ROBUST ground detection
 * Combines collision callbacks with active ground checking
 */
public final class MarioController extends Component implements ICollisionListener {
    private MarioState currentState;
    private MarioState smallState;
    private MarioState bigState;

    // Movement tuning
    private float moveSpeed = 220f;
    private float jumpForce = 480f;
    private float jumpCutMultiplier = 0.45f;

    // Input state
    private boolean jumpRequested = false;
    private boolean jumpHeld = false;
    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean dashRequested = false;

    // Jump feel
    private float jumpBufferTime = 0.15f;
    private float jumpBufferCounter = 0f;
    private float coyoteTime = 0.15f;
    private float coyoteCounter = 0f;
    private boolean isJumping = false;

    // Ground detection - hybrid approach
    private final Set<GameObject> groundContacts = new HashSet<>();
    private boolean wasGroundedLastFrame = false;
    private float timeSinceGrounded = 0f;

    // Collision system reference for ground checks
    private CollisionSystem collisionSystem;

    // Dash mechanic
    private boolean canDash = true;
    private boolean isDashing = false;
    private float dashSpeed = 450f;
    private float dashDuration = 0.15f;
    private float dashTimer = 0f;
    private float dashCooldown = 0.2f;
    private float dashCooldownTimer = 0f;
    private float dashDirX = 0f;
    private float dashDirY = 0f;

    public void setCollisionSystem(CollisionSystem collisionSystem) {
        this.collisionSystem = collisionSystem;
    }

    @Override
    protected void onAttach() {
        smallState = new SmallMarioState(this);
        bigState = new BigMarioState(this);
        currentState = smallState;
    }

    @Override
    public void update(double dt) {
        RigidBody rb = owner.getComponent(RigidBody.class);
        Transform transform = owner.getComponent(Transform.class);
        if (rb == null || transform == null) return;

        // Update time since grounded
        boolean isGrounded = checkGrounded();
        if (isGrounded) {
            timeSinceGrounded = 0f;
        } else {
            timeSinceGrounded += (float)dt;
        }

        // Detect landing
        if (isGrounded && !wasGroundedLastFrame) {
            onLanded();
        }

        wasGroundedLastFrame = isGrounded;

        // Update dash cooldown
        if (dashCooldownTimer > 0) {
            dashCooldownTimer -= (float)dt;
        }

        // Dash logic
        if (isDashing) {
            dashTimer -= (float)dt;

            rb.velocity.x = dashDirX * dashSpeed;
            rb.velocity.y = dashDirY * dashSpeed;
            rb.useGravity = false;

            if (dashTimer <= 0) {
                isDashing = false;
                rb.useGravity = true;
                dashCooldownTimer = dashCooldown;

                rb.velocity.x *= 0.7f;
                rb.velocity.y *= 0.7f;
            }

            return;
        }

        // Start dash
        if (dashRequested && canDash && dashCooldownTimer <= 0) {
            startDash();
            dashRequested = false;
            return;
        }

        // Coyote time
        if (isGrounded) {
            coyoteCounter = coyoteTime;
            canDash = true;
        } else {
            coyoteCounter -= (float)dt;
        }

        // Jump buffer
        if (jumpRequested) {
            jumpBufferCounter = jumpBufferTime;
        } else {
            jumpBufferCounter -= (float)dt;
        }

        // Horizontal movement
        if (moveRight) {
            rb.velocity.x = moveSpeed;
            updateAnimation("run");
        } else if (moveLeft) {
            rb.velocity.x = -moveSpeed;
            updateAnimation("run");
        } else {
            rb.velocity.x = 0;
            updateAnimation("idle");
        }

        // Jump logic
        if (jumpBufferCounter > 0 && coyoteCounter > 0 && !isJumping) {
            currentState.jump();
            updateAnimation("jump");
            jumpBufferCounter = 0;
            coyoteCounter = 0;
            isJumping = true;
        }

        // Variable jump height
        if (isJumping && !jumpHeld && rb.velocity.y > 0 && jumpCutMultiplier < 1.0f) {
            rb.velocity.y *= jumpCutMultiplier;
        }

        // Update state-specific logic
        currentState.update(dt);

        // Reset input flags
        moveLeft = false;
        moveRight = false;
        jumpRequested = false;
        dashRequested = false;
    }

    /**
     * ROBUST ground check - uses both collision tracking AND active overlap check
     * This prevents bugs where collision callbacks get out of sync
     */
    private boolean checkGrounded() {
        // First check: collision tracking
        if (!groundContacts.isEmpty()) {
            return true;
        }

        // Second check: active ground detection (small overlap box below player)
        // This catches cases where collision callbacks might be missed
        if (collisionSystem != null) {
            Transform transform = owner.getComponent(Transform.class);
            if (transform != null) {
                // Check a small area just below the player's feet
                float groundCheckDistance = 2f;
                float checkX = transform.position.x + 14; // Center of 28-wide collider
                float checkY = transform.position.y - groundCheckDistance;

                var groundColliders = collisionSystem.overlapBox(
                        checkX,
                        checkY,
                        20f,  // Slightly narrower than player
                        4f,   // Small height
                        CollisionLayer.GROUND,
                        CollisionLayer.PLATFORM
                );

                return !groundColliders.isEmpty();
            }
        }

        return false;
    }

    /**
     * Called when Mario lands on ground
     */
    private void onLanded() {
        isJumping = false;
        canDash = true;
    }

    // ✅ COLLISION CALLBACKS
    @Override
    public void onCollision(Collision collision) {
        // Track ground contacts
        if (collision.isFromAbove()) {
            groundContacts.add(collision.other);
        }

        // Hit ceiling
        if (collision.isFromBelow()) {
            RigidBody rb = owner.getComponent(RigidBody.class);
            if (rb != null && rb.velocity.y > 0) {
                rb.velocity.y = 0;
            }
        }
    }

    @Override
    public void onCollisionEnter(GameObject other) {
        // Additional enter logic (pickups, enemies, etc.)
    }

    @Override
    public void onCollisionExit(GameObject other) {
        // Remove from ground contacts
        groundContacts.remove(other);
    }

    private void startDash() {
        dashDirX = 0;
        dashDirY = 0;

        if (moveRight) dashDirX = 1;
        else if (moveLeft) dashDirX = -1;

        if (jumpHeld) dashDirY = 1;

        if (dashDirX == 0 && dashDirY == 0) {
            dashDirX = 1;
        }

        if (dashDirX != 0 && dashDirY != 0) {
            float length = (float)Math.sqrt(dashDirX * dashDirX + dashDirY * dashDirY);
            dashDirX /= length;
            dashDirY /= length;
        }

        isDashing = true;
        dashTimer = dashDuration;
        canDash = false;
        isJumping = false;
    }

    private void updateAnimation(String anim) {
        Animator animator = owner.getComponent(Animator.class);
        if (animator != null) {
            if (isDashing) {
                animator.play("run");
            } else {
                animator.play(anim);
            }
        }
    }

    // Input methods
    public void moveLeft() { this.moveLeft = true; }
    public void moveRight() { this.moveRight = true; }
    public void jump() {
        this.jumpRequested = true;
        this.jumpHeld = true;
    }
    public void releaseJump() {
        this.jumpHeld = false;
    }
    public void dash() {
        this.dashRequested = true;
    }

    // State transitions
    public void powerUp() {
        currentState = bigState;
        currentState.onEnter();
    }

    public void takeDamage() {
        if (currentState == bigState) {
            currentState = smallState;
            currentState.onEnter();
        }
    }

    public boolean isOnGround() {
        return checkGrounded();
    }

    public float getJumpForce() { return jumpForce; }
    public GameObject getMario() { return owner; }
    public boolean isDashing() { return isDashing; }
}