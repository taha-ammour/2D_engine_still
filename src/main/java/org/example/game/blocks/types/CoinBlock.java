package org.example.game.blocks.types;

import org.example.ecs.GameObject;
import org.example.ecs.components.SpriteRenderer;
import org.example.game.blocks.Block;
import org.example.gfx.Texture;
import org.example.game.blocks.BlockState;
import org.example.game.blocks.effects.BlockEffects;


public final class CoinBlock extends Block {
    private final int coinCount;
    private int coinsRemaining;

    public CoinBlock(int coinCount) {
        super();
        this.coinCount = coinCount;
        this.coinsRemaining = coinCount;
        this.canBeHit = true;
        this.maxHits = coinCount;
    }

    @Override
    public void setupVisuals() {
        SpriteRenderer sprite = new SpriteRenderer(renderer);
        sprite.width = 64;
        sprite.height = 64;

        try {
            Texture texture = Texture.load("assets/blocks/coin_block.png");
            sprite.setTexture(texture);
        } catch (Exception e) {
            sprite.setTint(1.0f, 0.85f, 0.0f, 1.0f);
        }

        owner.addComponent(sprite);
    }

    @Override
    public void updateAnimation(double dt) {
        SpriteRenderer sprite = owner.getComponent(SpriteRenderer.class);
        if (sprite != null && coinsRemaining > 0) {
            float sparkle = 0.9f + 0.1f * (float) Math.sin(animationTimer * 8f);
            sprite.getMaterial().setTint(sparkle, sparkle * 0.85f, 0.0f, 1.0f);
        }
    }

    @Override
    public void updateState(double dt) {
        if (state == BlockState.HIT && animationTimer > 0.1f) {
            state = BlockState.IDLE;
            animationTimer = 0f;
        }
    }

    @Override
    public void onHit(GameObject player) {
        if (coinsRemaining > 0) {
            state = BlockState.HIT;
            BlockEffects.COIN.apply(player);
            coinsRemaining--;
            System.out.println("🪙 Coin! (" + coinsRemaining + " remaining)");
        }
    }

    @Override
    protected void onDeplete() {
        super.onDeplete();

        SpriteRenderer sprite = owner.getComponent(SpriteRenderer.class);
        if (sprite != null) {
            sprite.setTint(0.6f, 0.5f, 0.0f, 1.0f);
        }
    }
}
