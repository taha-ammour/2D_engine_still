package org.example.game.blocks.effects;

import org.example.ecs.GameObject;

/**
 * Strategy Pattern: Different effects implement this interface
 * Single Responsibility: Each effect does one thing
 */
@FunctionalInterface
public interface BlockEffect {
    void apply(GameObject target);

    /**
     * Apply effect with a multiplier (for DoubleEffect decorator)
     * Default implementation: just apply twice
     */
    default void applyWithMultiplier(GameObject target, float multiplier) {
        // Default: apply multiple times based on multiplier
        int times = (int) Math.ceil(multiplier);
        for (int i = 0; i < times; i++) {
            apply(target);
        }
    }

    /**
     * Chain multiple effects together
     */
    default BlockEffect andThen(BlockEffect after) {
        return new BlockEffect() {
            @Override
            public void apply(GameObject target) {
                BlockEffect.this.apply(target);
                after.apply(target);
            }

            @Override
            public void applyWithMultiplier(GameObject target, float multiplier) {
                // Apply both effects with the multiplier
                BlockEffect.this.applyWithMultiplier(target, multiplier);
                after.applyWithMultiplier(target, multiplier);
            }
        };
    }
}