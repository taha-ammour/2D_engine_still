package org.example.game.blocks.decorators;

import org.example.ecs.GameObject;
import org.example.ecs.components.SpriteRenderer;
import org.example.game.blocks.Block;
import org.example.game.blocks.BlockState;
import org.example.game.blocks.effects.BlockEffect;
import org.example.gfx.ShaderEffect;

/**
 * Decorator: Doubles the effect of a block
 *
 * SOLID Principles:
 * - Open/Closed: Works with any block type without modification
 * - Single Responsibility: Only handles effect multiplication
 * - Dependency Inversion: Depends on BlockEffect abstraction
 */

public final class DoubleEffectDecorator extends BlockDecorator {
    private static final float MULTIPLIER = 2.0f;

    public DoubleEffectDecorator(Block block) {
        super(block);
        this.maxHits = block.getMaxHits();
        this.canBeHit = block.getCanBeHit();
        this.isBreakable = block.getIsBreakable();
    }

    @Override
    public void setupVisuals() {
        decoratedBlock.setupVisuals();

        SpriteRenderer sprite = owner.getComponent(SpriteRenderer.class);
        if (sprite != null) {
            sprite.addEffect(ShaderEffect.PULSATE);
            sprite.addEffect(ShaderEffect.OUTLINE);
        }
    }

    @Override
    public void onHitFromBelow(GameObject player) {
        if (!canBeHit || !isActive) return;

        hitCount++;

        // Apply the enhanced effect
        BlockEffect effect = decoratedBlock.getEffect();
        if (effect != null) {
            effect.applyWithMultiplier(player, MULTIPLIER);
            System.out.println("✨ 2X EFFECT! (" + MULTIPLIER + "x power)");
        }

        // Update decorated block's state
        decoratedBlock.setState(BlockState.HIT);

        if (hitCount >= maxHits) {
            onDeplete();
        }
    }
}
