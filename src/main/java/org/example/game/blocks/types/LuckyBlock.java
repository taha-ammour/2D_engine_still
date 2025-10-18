// ============================================
// CONCRETE BLOCK IMPLEMENTATIONS - COMPLETE
// ============================================

// ===== 1. LUCKY BLOCK =====
// src/main/java/org/example/blocks/types/LuckyBlock.java
package org.example.game.blocks.types;

import org.example.game.blocks.Block;
import org.example.game.blocks.BlockState;
import org.example.game.blocks.effects.BlockEffect;
import org.example.game.blocks.effects.BlockEffects;
import org.example.ecs.GameObject;
import org.example.ecs.components.*;
import org.example.gfx.*;

/**
 * Lucky Block - Gives random power-ups
 * Strategy Pattern: Uses different effects
 */
public final class LuckyBlock extends Block {
    private final BlockEffect effect;
    private float bounceTimer = 0f;
    private float originalY = 0f;

    public LuckyBlock() {
        this(BlockEffects.COIN); // Default: gives coins
    }

    public LuckyBlock(BlockEffect effect) {
        super();
        this.effect = effect;
        this.canBeHit = true;
        this.maxHits = 1;
    }

    @Override
    public void setupVisuals() {
        SpriteRenderer sprite = new SpriteRenderer(renderer);
        sprite.width = 64;
        sprite.height = 64;

        try {
            Texture texture = Texture.load("assets/blocks/lucky_block.png");
            TextureAtlas atlas = new TextureAtlas(texture, 4, 1);
            sprite.setAtlas(atlas);

            Animator animator = new Animator();
            animator.addClip("idle", AnimationClip.create(0, 4, 8f));
            animator.addClip("hit", AnimationClip.create(0, 1, 1f));
            animator.addClip("depleted", AnimationClip.create(0, 1, 1f));
            owner.addComponent(animator);

        } catch (Exception e) {
            sprite.setTint(1.0f, 0.9f, 0.2f, 1.0f);
        }

        owner.addComponent(sprite);

        Transform transform = owner.getComponent(Transform.class);
        if (transform != null) {
            originalY = transform.position.y;
        }
    }

    @Override
    public void updateAnimation(double dt) {
        if (state == BlockState.IDLE) {
            SpriteRenderer sprite = owner.getComponent(SpriteRenderer.class);
            if (sprite != null) {
                float pulse = 0.9f + 0.1f * (float)Math.sin(animationTimer * 3f);
                sprite.getMaterial().setTint(pulse, pulse * 0.9f, 0.2f, 1.0f);
            }
        }
    }

    @Override
    public void updateState(double dt) {
        if (state == BlockState.HIT) {
            bounceTimer += (float)dt;

            Transform transform = owner.getComponent(Transform.class);
            if (transform != null) {
                float bounceHeight = 10f;
                float bounceSpeed = 15f;

                if (bounceTimer < 0.2f) {
                    transform.position.y = originalY +
                            bounceHeight * (float)Math.sin(bounceTimer * bounceSpeed);
                } else {
                    transform.position.y = originalY;
                    state = BlockState.IDLE;
                    bounceTimer = 0f;
                }
            }
        }
    }

    @Override
    public void onHit(GameObject player) {
        state = BlockState.HIT;
        effect.apply(player);

        SpriteRenderer sprite = owner.getComponent(SpriteRenderer.class);
        if (sprite != null) {
            sprite.addEffect(ShaderEffect.FLASH);
        }
    }

    @Override
    protected void onDeplete() {
        super.onDeplete();

        SpriteRenderer sprite = owner.getComponent(SpriteRenderer.class);
        if (sprite != null) {
            sprite.setTint(0.5f, 0.45f, 0.1f, 1.0f);
            sprite.setEffects(ShaderEffect.NONE.flag);
        }
    }
}
