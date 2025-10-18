package org.example.game.blocks.types;

import org.example.ecs.GameObject;
import org.example.ecs.components.AnimationClip;
import org.example.ecs.components.Animator;
import org.example.ecs.components.SpriteRenderer;
import org.example.game.blocks.Block;
import org.example.gfx.ShaderEffect;
import org.example.gfx.Texture;
import org.example.gfx.TextureAtlas;
import org.example.game.blocks.BlockState;
import org.example.game.blocks.effects.BlockEffects;


public final class PoisonBlock extends Block {
    private float wobbleTimer = 0f;

    public PoisonBlock() {
        super();
        this.canBeHit = true;
        this.maxHits = 1;
    }

    @Override
    public void setupVisuals() {
        SpriteRenderer sprite = new SpriteRenderer(renderer);
        sprite.width = 64;
        sprite.height = 64;

        try {
            Texture texture = Texture.load("assets/blocks/poison_block.png");
            TextureAtlas atlas = new TextureAtlas(texture, 4, 1);
            sprite.setAtlas(atlas);

            Animator animator = new Animator();
            animator.addClip("idle", AnimationClip.create(0, 4, 6f));
            animator.addClip("hit", AnimationClip.create(0, 1, 1f));
            owner.addComponent(animator);

        } catch (Exception e) {
            sprite.setTint(0.5f, 0.2f, 0.8f, 1.0f);
        }

        sprite.getMaterial().setShaderName("effects");
        sprite.addEffect(ShaderEffect.WOBBLE);

        owner.addComponent(sprite);
    }

    @Override
    public void updateAnimation(double dt) {
        wobbleTimer += (float) dt;

        SpriteRenderer sprite = owner.getComponent(SpriteRenderer.class);
        if (sprite != null && state == BlockState.IDLE) {
            float pulse = 0.7f + 0.3f * (float) Math.sin(wobbleTimer * 4f);
            sprite.getMaterial().setTint(
                    0.5f * pulse,
                    0.2f + 0.3f * pulse,
                    0.8f * pulse,
                    1.0f
            );
        }
    }

    @Override
    public void updateState(double dt) {
        if (state == BlockState.HIT) {
            SpriteRenderer sprite = owner.getComponent(SpriteRenderer.class);
            if (sprite != null) {
                sprite.addEffect(ShaderEffect.DISSOLVE);
            }

            if (animationTimer > 1.0f) {
                state = BlockState.IDLE;
                animationTimer = 0f;
            }
        }
    }

    @Override
    public void onHit(GameObject player) {
        state = BlockState.HIT;
        BlockEffects.POISON.apply(player);
        System.out.println("☠️ Poison block activated!");
    }
}
