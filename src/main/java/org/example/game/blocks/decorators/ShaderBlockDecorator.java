package org.example.game.blocks.decorators;

import org.example.ecs.components.SpriteRenderer;
import org.example.game.blocks.Block;
import org.example.gfx.ShaderEffect;

/**
 * Decorator: Adds shader effects to blocks
 */
public final class ShaderBlockDecorator extends BlockDecorator {
    private final int shaderEffects;
    private final String shaderName;

    public ShaderBlockDecorator(Block block, String shaderName, ShaderEffect... effects) {
        super(block);
        this.shaderName = shaderName;
        this.shaderEffects = ShaderEffect.combine(effects);
    }

    @Override
    public void setupVisuals() {
        decoratedBlock.setupVisuals();  // Correct - calls wrapped block's method.setupVisuals();

        SpriteRenderer sprite = owner.getComponent(SpriteRenderer.class);
        if (sprite != null) {
            sprite.getMaterial().setShaderName(shaderName);
            sprite.setEffects(shaderEffects);
        }
    }
}
