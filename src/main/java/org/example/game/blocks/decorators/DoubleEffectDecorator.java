package org.example.game.blocks.decorators;

import org.example.ecs.GameObject;
import org.example.ecs.components.SpriteRenderer;
import org.example.game.blocks.Block;
import org.example.gfx.ShaderEffect;

/**
 * Decorator: Doubles the effect of a block
 * Visual indicator: Adds glow/pulsate effect
 */
public final class DoubleEffectDecorator extends BlockDecorator {

    public DoubleEffectDecorator(Block block) {
        super(block);
    }

    @Override
    public void setupVisuals() {
        decoratedBlock.setupVisuals();  // Correct - calls wrapped block's method.setupVisuals();

        // Add visual indication of 2x effect
        SpriteRenderer sprite = owner.getComponent(SpriteRenderer.class);
        if (sprite != null) {
            sprite.addEffect(ShaderEffect.PULSATE);
        }
    }

    @Override
    public void onHit(GameObject player) {
        // Execute effect twice!
        decoratedBlock.onHit(player);
        decoratedBlock.onHit(player);

        System.out.println("✨ 2X EFFECT TRIGGERED!");
    }
}
