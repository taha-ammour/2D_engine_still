package org.example.game.blocks.types;

import org.example.ecs.GameObject;
import org.example.ecs.components.AnimationClip;
import org.example.ecs.components.Animator;
import org.example.ecs.components.SpriteRenderer;
import org.example.ecs.components.Transform;
import org.example.game.blocks.Block;
import org.example.game.blocks.effects.BlockEffect;
import org.example.game.blocks.effects.BlockEffects;
import org.example.game.blocks.BlockState;
import org.example.gfx.ShaderEffect;
import org.example.gfx.Texture;
import org.example.gfx.TextureAtlas;

public final class MysteryBlock extends Block {
    private static final BlockEffect[] POSSIBLE_EFFECTS = {
            BlockEffects.POWER_UP,
            BlockEffects.COIN,
            BlockEffects.BOUNCE,
            BlockEffects.INVINCIBILITY,
            BlockEffects.POISON,
            BlockEffects.SLOW
    };

    public MysteryBlock() {
        super();
        this.canBeHit = true;
        this.maxHits = 1;  // ✅ Only one use
    }

    @Override
    public void setupVisuals() {
        SpriteRenderer sprite = new SpriteRenderer(renderer);
        sprite.width = 64;
        sprite.height = 64;

        try {
            Texture texture = Texture.load("assets/blocks/mystery_block.png");
            TextureAtlas atlas = new TextureAtlas(texture, 8, 1);
            sprite.setAtlas(atlas);

            Animator animator = new Animator();
            animator.addClip("idle", AnimationClip.create(0, 8, 12f));
            owner.addComponent(animator);

        } catch (Exception e) {
            sprite.getMaterial().setShaderName("effects");
            sprite.addEffect(ShaderEffect.PULSATE);
        }

        owner.addComponent(sprite);
    }

    @Override
    public void updateAnimation(double dt) {
        if (state == BlockState.IDLE) {
            float hue = (animationTimer * 0.5f) % 1.0f;
            float[] rgb = hsvToRgb(hue, 0.8f, 1.0f);

            SpriteRenderer sprite = owner.getComponent(SpriteRenderer.class);
            if (sprite != null) {
                sprite.getMaterial().setTint(rgb[0], rgb[1], rgb[2], 1.0f);
            }
        }
    }

    @Override
    public void updateState(double dt) {
        if (state == BlockState.HIT) {
            Transform transform = owner.getComponent(Transform.class);
            if (transform != null) {
                transform.rotation += (float) dt * 10f;

                if (animationTimer > 0.5f) {
                    transform.rotation = 0f;
                    state = BlockState.IDLE;
                    animationTimer = 0f;
                }
            }
        }
    }

    @Override
    public BlockEffect getEffect() {
        return BlockEffects.random(POSSIBLE_EFFECTS);
    }

    @Override
    public void onHit(GameObject player) {
        state = BlockState.HIT;

        BlockEffect effect = getEffect();
        effect.apply(player);

    }

    @Override
    protected void onDeplete() {
        super.onDeplete();

        SpriteRenderer sprite = owner.getComponent(SpriteRenderer.class);
        if (sprite != null) {
            sprite.setTint(0.3f, 0.3f, 0.3f, 1.0f);
            sprite.setEffects(ShaderEffect.GRAYSCALE.flag);
        }
    }

    private float[] hsvToRgb(float h, float s, float v) {
        float c = v * s;
        float x = c * (1 - Math.abs((h * 6) % 2 - 1));
        float m = v - c;

        float r = 0, g = 0, b = 0;

        if (h < 1f / 6f) {
            r = c;
            g = x;
        } else if (h < 2f / 6f) {
            r = x;
            g = c;
        } else if (h < 3f / 6f) {
            g = c;
            b = x;
        } else if (h < 4f / 6f) {
            g = x;
            b = c;
        } else if (h < 5f / 6f) {
            r = x;
            b = c;
        } else {
            r = c;
            b = x;
        }

        return new float[]{r + m, g + m, b + m};
    }
}