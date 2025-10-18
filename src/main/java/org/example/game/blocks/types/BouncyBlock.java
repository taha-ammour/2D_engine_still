package org.example.game.blocks.types;

import org.example.ecs.GameObject;
import org.example.ecs.components.SpriteRenderer;
import org.example.ecs.components.Transform;
import org.example.game.blocks.Block;
import org.example.game.blocks.BlockState;
import org.example.game.blocks.effects.BlockEffects;
import org.example.gfx.Texture;

public final class BouncyBlock extends Block {
    private float squashTimer = 0f;

    public BouncyBlock() {
        super();
        this.canBeHit = true;
        this.maxHits = Integer.MAX_VALUE;
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
            sprite.setTint(1.0f, 0.3f, 0.5f, 1.0f);
        }

        owner.addComponent(sprite);
    }

    @Override
    public void updateAnimation(double dt) {
        if (state == BlockState.IDLE) {
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
                if (squashTimer < 0.1f) {
                    transform.scale.set(1.2f, 0.8f);
                } else if (squashTimer < 0.2f) {
                    transform.scale.set(0.9f, 1.1f);
                } else {
                    transform.scale.set(1f, 1f);
                    state = BlockState.IDLE;
                    squashTimer = 0f;
                }
            }
        }
    }

    @Override
    public void onHit(GameObject player) {
        state = BlockState.HIT;
        BlockEffects.BOUNCE.apply(player);
        System.out.println("🎈 Bouncy block activated!");
    }
}
