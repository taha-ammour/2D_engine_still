// ============================================
// BLOCK SYSTEM - CORE COMPONENTS
// ============================================

// ===== 1. BASE BLOCK COMPONENT =====
// src/main/java/org/example/blocks/Block.java
package org.example.game.blocks;

import org.example.ecs.Component;
import org.example.ecs.GameObject;
import org.example.ecs.components.SpriteRenderer;
import org.example.gfx.*;
import org.example.physics.*;

/**
 * Base Block Component
 * Strategy Pattern: Different block types use different strategies
 * Open/Closed: Open for extension (new block types), closed for modification
 */
public abstract class Block extends Component implements ICollisionListener {
    protected BlockState state = BlockState.IDLE;
    protected boolean isActive = true;
    protected float animationTimer = 0f;

    // Visual properties
    protected Texture texture;
    protected TextureAtlas atlas;
    protected Material material;

    // Block properties
    protected boolean isBreakable = false;
    protected boolean canBeHit = true;
    protected int hitCount = 0;
    protected int maxHits = 1;
    protected Renderer2D renderer;


    public Block() {
        // Default material
        this.material = Material.builder()
                .shader("sprite")
                .tint(1, 1, 1, 1)
                .build();
    }

    @Override
    public void onAttach() {
        setupVisuals();
        setupCollider();
    }

    public void completeSetup() {
        if (renderer != null) {
            // Remove any incomplete SpriteRenderer from failed setup
            SpriteRenderer oldSprite = owner.getComponent(SpriteRenderer.class);
            if (oldSprite != null) {
                owner.removeComponent(SpriteRenderer.class);
            }

            setupVisuals();
            setupCollider();
        } else {
            System.err.println("⚠️ Block.completeSetup() called but renderer is null!");
        }
    }

    public void setOwner(GameObject owner) {
        this.owner = owner;
    }

    public void setRenderer(Renderer2D renderer) {
        this.renderer = renderer;
    }

    /**
     * Template Method: Subclasses override to customize
     */
    public abstract void setupVisuals();

    protected void setupCollider() {
        BoxCollider collider = new BoxCollider(64, 64, CollisionLayer.GROUND);
        collider.isStatic = true;
        owner.addComponent(collider);
    }

    @Override
    public void update(double dt) {
        animationTimer += (float)dt;
        updateAnimation(dt);
        updateState(dt);
    }

    public abstract void updateAnimation(double dt);
    public abstract void updateState(double dt);

    /**
     * Called when player hits block from below
     */
    public void onHitFromBelow(GameObject player) {
        if (!canBeHit || !isActive) return;

        hitCount++;
        onHit(player);

        if (hitCount >= maxHits) {
            onDeplete();
        }
    }

    /**
     * Template Method: Override in subclasses for specific behavior
     */
    public abstract void onHit(GameObject player);

    protected void onDeplete() {
        isActive = false;
        state = BlockState.DEPLETED;
    }

    @Override
    public void onCollision(Collision collision) {
        // Check if hit from below
        if (collision.isFromBelow() &&
                collision.other.getName().equals("Mario")) {
            onHitFromBelow(collision.other);
        }
    }

    // Getters/Setters
    public BlockState getState() { return state; }
    public boolean isActive() { return isActive; }
    public Material getMaterial() { return material; }


}

