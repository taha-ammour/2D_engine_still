package org.example.game.blocks;

import org.example.game.blocks.effects.BlockEffect;

/**
 * Value Object: Block configuration template
 * Immutable: Thread-safe and cacheable
 */
public record BlockTemplate(
        String blockType,
        String effectName,
        boolean doubleEffect,
        boolean animated,
        String[] shaderEffects
) {
    // Factory method from template
    public void applyTo(BlockBuilder builder) {
        // Apply block type
        switch (blockType) {
            case "lucky" -> builder.lucky();
            case "poison" -> builder.poison();
            case "bouncy" -> builder.bouncy();
            case "mystery" -> builder.mystery();
        }

        // Apply effect
        if (effectName != null) {
            BlockEffect effect = BlockRegistry.getInstance().getEffect(effectName);
            if (effect != null) {
                builder.effect(effect);
            }
        }

        // Apply decorators
        if (doubleEffect) {
            builder.doubleEffect();
        }

        if (animated) {
            builder.animated(blockType + "_idle", blockType + "_hit");
        }

        if (shaderEffects != null && shaderEffects.length > 0) {
            // Convert string array to ShaderEffect array
            org.example.gfx.ShaderEffect[] effects =
                    new org.example.gfx.ShaderEffect[shaderEffects.length];

            for (int i = 0; i < shaderEffects.length; i++) {
                effects[i] = parseShaderEffect(shaderEffects[i]);
            }

            builder.shader("effects", effects);
        }
    }

    private org.example.gfx.ShaderEffect parseShaderEffect(String name) {
        return switch (name.toLowerCase()) {
            case "grayscale" -> org.example.gfx.ShaderEffect.GRAYSCALE;
            case "pulsate" -> org.example.gfx.ShaderEffect.PULSATE;
            case "wobble" -> org.example.gfx.ShaderEffect.WOBBLE;
            case "outline" -> org.example.gfx.ShaderEffect.OUTLINE;
            case "flash" -> org.example.gfx.ShaderEffect.FLASH;
            case "dissolve" -> org.example.gfx.ShaderEffect.DISSOLVE;
            default -> org.example.gfx.ShaderEffect.NONE;
        };
    }
}
