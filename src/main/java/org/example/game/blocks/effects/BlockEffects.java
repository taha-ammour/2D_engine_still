package org.example.game.blocks.effects;

import org.example.ecs.components.RigidBody;
import org.example.ecs.components.SpriteRenderer;
import org.example.game.MarioController;
import org.example.gfx.ShaderEffect;

/**
 * Factory Pattern: Creates common block effects
 * Flyweight Pattern: Reusable effect instances
 */
public final class BlockEffects {

    // ===== POWER-UP EFFECTS =====

    public static final BlockEffect POWER_UP = target -> {
        MarioController mario = target.getComponent(MarioController.class);
        if (mario != null) {
            mario.powerUp();
            System.out.println("🍄 Mario powered up!");
        }
    };

    public static final BlockEffect INVINCIBILITY = target -> {
        MarioController mario = target.getComponent(MarioController.class);
        SpriteRenderer sprite = target.getComponent(SpriteRenderer.class);

        if (mario != null && sprite != null) {
            // Apply visual effect
            sprite.setEffects(ShaderEffect.combine(
                    ShaderEffect.FLASH,
                    ShaderEffect.OUTLINE
            ));

            // Start invincibility timer (would need timer component)
            System.out.println("⭐ Mario is invincible!");
        }
    };

    public static final BlockEffect COIN = target -> {
        // Add coin to score (would integrate with score system)
        System.out.println("🪙 Coin collected! +10 points");
    };

    // ===== NEGATIVE EFFECTS =====

    public static final BlockEffect POISON = target -> {
        MarioController mario = target.getComponent(MarioController.class);
        SpriteRenderer sprite = target.getComponent(SpriteRenderer.class);

        if (mario != null) {
            mario.takeDamage();
            System.out.println("☠️ Mario poisoned!");
        }

        if (sprite != null) {
            sprite.addEffect(ShaderEffect.PULSATE);
            sprite.setTint(0.5f, 1.0f, 0.5f, 1.0f); // Green tint
        }
    };

    public static final BlockEffect SLOW = target -> {
        RigidBody rb = target.getComponent(RigidBody.class);
        if (rb != null) {
            rb.velocity.mul(0.5f);
            System.out.println("🐌 Mario slowed!");
        }
    };

    // ===== MOVEMENT EFFECTS =====

    public static final BlockEffect BOUNCE = target -> {
        RigidBody rb = target.getComponent(RigidBody.class);
        if (rb != null) {
            rb.velocity.y = 600f; // Super bounce
            System.out.println("🎈 Mario bounced!");
        }
    };

    public static final BlockEffect TELEPORT = target -> {
        // Teleport logic (would need destination)
        System.out.println("🌀 Mario teleported!");
    };

    // ===== UTILITY METHODS =====

    /**
     * Create a 2x multiplier effect wrapper
     */
    public static BlockEffect doubled(BlockEffect effect) {
        return target -> {
            effect.apply(target);
            effect.apply(target);
        };
    }

    /**
     * Create a random effect from a list
     */
    public static BlockEffect random(BlockEffect... effects) {
        return target -> {
            int index = (int) (Math.random() * effects.length);
            effects[index].apply(target);
        };
    }

    /**
     * Create a delayed effect
     */
    public static BlockEffect delayed(BlockEffect effect, float delay) {
        return target -> {
            // Would need timer system to implement properly
            System.out.println("⏰ Effect will trigger in " + delay + "s");
            effect.apply(target);
        };
    }
}
