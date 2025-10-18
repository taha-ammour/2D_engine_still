package org.example.game.blocks.effects;

import org.example.ecs.GameObject; /**
 * Strategy Pattern: Different effects implement this interface
 * Single Responsibility: Each effect does one thing
 */
@FunctionalInterface
public interface BlockEffect {
    void apply(GameObject target);

    /**
     * Chain multiple effects together
     */
    default BlockEffect andThen(BlockEffect after) {
        return target -> {
            this.apply(target);
            after.apply(target);
        };
    }
}
