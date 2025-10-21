package org.example.game.blocks.types;

import org.example.ecs.GameObject;
import org.example.ecs.components.SpriteRenderer;
import org.example.ecs.components.Transform;
import org.example.game.blocks.Block;
import org.example.game.blocks.BlockState;
import org.example.game.blocks.effects.BlockEffect;
import org.example.game.blocks.effects.BlockEffects;
import org.example.gfx.Texture;

public final class BouncyBlock extends Block {
    private float squashTimer = 0f;

    public BouncyBlock() {
        super();
        this.canBeHit = true;
        this.isBreakable = false;
        this.maxHits = Integer.MAX_VALUE;  // Never depletes
    }

    @Override
    public BlockEffect getEffect() {
        return BlockEffects.BOUNCE;
    }

    @Override
    public void onHit(GameObject player) {
        state = BlockState.HIT;

        BlockEffect effect = getEffect();
        if (effect != null) {
            effect.apply(player);
        }
    }

    @Override
    public void setupVisuals() {
        SpriteRenderer sprite = new SpriteRenderer(renderer);
        sprite.width = 64;
        sprite.height = 64;

        try {
            Texture texture = Texture.load("assets/blocks/bouncy_block.png");
            sprite.setTexture(texture);
        } catch (Exception e) {
            // Fallback: pink color if texture not found
            sprite.setTint(1.0f, 0.3f, 0.5f, 1.0f);
        }

        owner.addComponent(sprite);
    }

    @Override
    public void updateAnimation(double dt) {
        if (state == BlockState.IDLE) {
            // Subtle breathing animation
            float breathe = 0.95f + 0.05f * (float) Math.sin(animationTimer * 2f);
            Transform transform = owner.getComponent(Transform.class);
            if (transform != null) {
                transform.scale.set(breathe, breathe);
            }
        }
    }

    @Override
    public void updateState(double dt) {
        if (state == BlockState.HIT) {
            squashTimer += (float) dt;

            Transform transform = owner.getComponent(Transform.class);
            if (transform != null) {
                // Squash and stretch animation
                if (squashTimer < 0.1f) {
                    // Squash down
                    transform.scale.set(1.2f, 0.8f);
                } else if (squashTimer < 0.2f) {
                    // Stretch up
                    transform.scale.set(0.9f, 1.1f);
                } else {
                    // Return to normal
                    transform.scale.set(1f, 1f);
                    state = BlockState.IDLE;
                    squashTimer = 0f;
                }
            }
        }
    }

    @Override
    protected void onDeplete() {
    }
}