package org.example.game.blocks.decorators;

import org.example.ecs.components.Animator;
import org.example.game.blocks.Block;

/**
 * Decorator: Adds animation to any block
 */
public final class AnimatedBlockDecorator extends BlockDecorator {
    private final String idleAnimName;
    private final String hitAnimName;

    public AnimatedBlockDecorator(Block block, String idleAnim, String hitAnim) {
        super(block);
        this.idleAnimName = idleAnim;
        this.hitAnimName = hitAnim;
    }

    @Override
    public void setupVisuals() {
        decoratedBlock.setupVisuals();  // Correct - calls wrapped block's method.setupVisuals();

        // Add animator
        Animator animator = owner.getComponent(Animator.class);
        if (animator != null) {
            animator.play(idleAnimName);
        }
    }

    @Override
    public void updateAnimation(double dt) {
        super.updateAnimation(dt);

        Animator animator = owner.getComponent(Animator.class);
        if (animator != null) {
            switch(decoratedBlock.getState()) {
                case IDLE -> animator.play(idleAnimName);
                case HIT -> animator.play(hitAnimName);
                case DEPLETED -> animator.play("depleted");
            }
        }
    }
}
