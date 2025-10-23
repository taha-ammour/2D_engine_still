// src/main/java/org/example/game/MarioController.java
package org.example.game;

import org.example.ecs.Component;
import org.example.ecs.GameObject;
import org.example.ecs.components.*;
import org.example.effects.FluidSystem;
import org.example.effects.ScreenShake;
import org.example.effects.TrailRenderer;
import org.example.game.states.*;
import org.example.gfx.particles.ParticleConfig;
import org.example.gfx.particles.ParticleSystem;
import org.example.physics.Collision;
import org.example.physics.CollisionLayer;
import org.example.physics.CollisionSystem;
import org.example.physics.ICollisionListener;
import org.example.physics.BoxCollider;

import java.util.HashSet;
import java.util.Set;

/**
 * Mario Controller with ROBUST ground detection
 *
 * KEY FIXES:
 * 1. Better ground detection with proper offset calculation
 * 2. Improved collision tracking
 * 3. More reliable coyote time
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

    // Collision system reference
    private CollisionSystem collisionSystem;

    // ✨ PARTICLE SYSTEM ✨
    private ParticleSystem particleSystem;
    private float dashTrailTimer = 0f;
    private float wallSlideTimer = 0f;


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

    public MarioController(ParticleSystem particleSystem, FluidSystem fluidSystem, ScreenShake screenShake, TrailRenderer trailRenderer) {
        super();
        this.particleSystem = particleSystem;

    }

    public void setCollisionSystem(CollisionSystem collisionSystem) {
        this.collisionSystem = collisionSystem;
    }

    @Override
    protected void onAttach() {
        smallState = new SmallMarioState(this);
        bigState = new BigMarioState(this);
        currentState = smallState;
    }

    // ✨ NEW: Set particle system
    public void setParticleSystem(ParticleSystem particleSystem) {
        this.particleSystem = particleSystem;
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
            onLanded(transform);
        }

        wasGroundedLastFrame = isGrounded;

        // Update dash cooldown
        if (dashCooldownTimer > 0) {
            dashCooldownTimer -= (float)dt;
        }

        // ✨ Dash trail particles
        if (isDashing && particleSystem != null) {
            dashTrailTimer += (float)dt;
            if (dashTrailTimer > 0.03f) { // Emit every 30ms
                emitDashTrail(transform);
                dashTrailTimer = 0f;
            }
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
            startDash(transform);
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

            emitJumpDust(transform);

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

    private void emitJumpDust(Transform transform) {
        if (particleSystem != null) {
            particleSystem.emitBurst(
                    transform.position.x + 16,
                    transform.position.y,
                    8,
                    ParticleConfig.jumpDust()
            );
        }
    }

    private void emitDashTrail(Transform transform) {
        if (particleSystem != null) {
            particleSystem.emitBurst(
                    transform.position.x + 16,
                    transform.position.y + 24,
                    3,
                    ParticleConfig.dashTrail()
            );
        }
    }

    /**
     * ROBUST ground check - uses both collision tracking AND active overlap check
     *
     * ✅ FIXED: Better offset calculation based on collider dimensions
     */
    private boolean checkGrounded() {
        // First check: collision tracking
        if (!groundContacts.isEmpty()) {
            return true;
        }

        // Second check: active ground detection
        if (collisionSystem != null) {
            Transform transform = owner.getComponent(Transform.class);
            BoxCollider selfCollider = owner.getComponent(BoxCollider.class);

            if (transform != null && selfCollider != null) {
                // Calculate check position at bottom of collider
                float groundCheckDistance = 3f;

                // Center X of collider (accounting for offset)
                float checkX = transform.position.x + selfCollider.offset.x + selfCollider.width * 0.5f;

                // Bottom Y of collider minus check distance
                float checkY = transform.position.y + selfCollider.offset.y - groundCheckDistance;

                var groundColliders = collisionSystem.overlapBox(
                        checkX,
                        checkY,
                        selfCollider.width * 0.8f,  // Slightly narrower
                        6f,                          // Small height
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
    private void onLanded(Transform transform) {
        isJumping = false;
        canDash = true;
        if (particleSystem != null) {
            // Landing dust cloud
            particleSystem.emitBurst(
                    transform.position.x + 16,
                    transform.position.y,
                    15,
                    ParticleConfig.landDust()
            );
        }
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
        // ✨ Wall slide particles
        if ((collision.isFromLeft() || collision.isFromRight()) && particleSystem != null) {
            Transform transform = owner.getComponent(Transform.class);
            if (transform != null) {
                wallSlideTimer += 0.016f; // Approximate frame time
                if (wallSlideTimer > 0.1f) {
                    particleSystem.emitBurst(
                            transform.position.x + (collision.isFromLeft() ? 0 : 32),
                            transform.position.y + 24,
                            2,
                            ParticleConfig.wallSlide()
                    );
                    wallSlideTimer = 0f;
                }
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

    private void startDash(Transform transform) {
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
        if (particleSystem != null) {
            particleSystem.emitBurst(
                    transform.position.x + 16,
                    transform.position.y + 24,
                    12,
                    ParticleConfig.dashTrail()
            );
        }
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
        // ✨ Power-up particles!
        if (particleSystem != null) {
            Transform transform = owner.getComponent(Transform.class);
            if (transform != null) {
                particleSystem.emitBurst(
                        transform.position.x + 16,
                        transform.position.y + 24,
                        20,
                        ParticleConfig.powerUpGlow()
                );
            }
        }
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