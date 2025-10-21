package org.example.game.blocks.effects;

import org.example.ecs.GameObject;
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

    public static final BlockEffect POWER_UP = new BlockEffect() {
        @Override
        public void apply(GameObject target) {
            MarioController mario = target.getComponent(MarioController.class);
            if (mario != null) {
                mario.powerUp();
                System.out.println("🍄 Mario powered up!");
            }
        }
    };

    public static final BlockEffect INVINCIBILITY = new BlockEffect() {
        @Override
        public void apply(GameObject target) {
            MarioController mario = target.getComponent(MarioController.class);
            SpriteRenderer sprite = target.getComponent(SpriteRenderer.class);

            if (mario != null && sprite != null) {
                sprite.setEffects(ShaderEffect.combine(
                        ShaderEffect.FLASH,
                        ShaderEffect.OUTLINE
                ));
                System.out.println("⭐ Mario is invincible!");
            }
        }
    };

    public static final BlockEffect COIN = new BlockEffect() {
        @Override
        public void apply(GameObject target) {
            System.out.println("🪙 Coin collected! +10 points");
        }
    };

    // ===== NEGATIVE EFFECTS =====

    public static final BlockEffect POISON = new BlockEffect() {
        @Override
        public void apply(GameObject target) {
            MarioController mario = target.getComponent(MarioController.class);
            SpriteRenderer sprite = target.getComponent(SpriteRenderer.class);

            if (mario != null) {
                mario.takeDamage();
                System.out.println("☠️ Mario poisoned!");
            }

            if (sprite != null) {
                sprite.addEffect(ShaderEffect.PULSATE);
                sprite.setTint(0.5f, 1.0f, 0.5f, 1.0f);
            }
        }
    };

    public static final BlockEffect SLOW = new BlockEffect() {
        @Override
        public void apply(GameObject target) {
            RigidBody rb = target.getComponent(RigidBody.class);
            if (rb != null) {
                rb.velocity.mul(0.5f);
                System.out.println("🐌 Mario slowed!");
            }
        }

        @Override
        public void applyWithMultiplier(GameObject target, float multiplier) {
            RigidBody rb = target.getComponent(RigidBody.class);
            if (rb != null) {
                // Multiply the slow effect (more multiplier = slower)
                rb.velocity.mul(0.5f / multiplier);
                System.out.println("🐌 Mario slowed! (x" + multiplier + ")");
            }
        }
    };

    // ===== MOVEMENT EFFECTS =====

    public static final BlockEffect BOUNCE = new BlockEffect() {
        private static final float BASE_BOUNCE_VELOCITY = 600f;

        @Override
        public void apply(GameObject target) {
            applyWithMultiplier(target, 1.0f);
        }

        @Override
        public void applyWithMultiplier(GameObject target, float multiplier) {
            RigidBody rb = target.getComponent(RigidBody.class);
            if (rb != null) {
                rb.velocity.y = BASE_BOUNCE_VELOCITY * multiplier;
                if (multiplier > 1.0f) {
                    System.out.println("🎈 Mario bounced! (x" + multiplier + " power!)");
                } else {
                    System.out.println("🎈 Mario bounced!");
                }
            }
        }
    };

    public static final BlockEffect TELEPORT = new BlockEffect() {
        @Override
        public void apply(GameObject target) {
            System.out.println("🌀 Mario teleported!");
        }
    };

    // ===== UTILITY METHODS =====

    /**
     * Create a 2x multiplier effect wrapper
     */
    public static BlockEffect doubled(BlockEffect effect) {
        return new BlockEffect() {
            @Override
            public void apply(GameObject target) {
                effect.applyWithMultiplier(target, 2.0f);
            }

            @Override
            public void applyWithMultiplier(GameObject target, float multiplier) {
                effect.applyWithMultiplier(target, multiplier * 2.0f);
            }
        };
    }

    /**
     * Create a random effect from a list
     */
    public static BlockEffect random(BlockEffect... effects) {
        return new BlockEffect() {
            @Override
            public void apply(GameObject target) {
                int index = (int) (Math.random() * effects.length);
                effects[index].apply(target);
            }

            @Override
            public void applyWithMultiplier(GameObject target, float multiplier) {
                // Pick ONE random effect and apply it with the multiplier
                int index = (int) (Math.random() * effects.length);
                effects[index].applyWithMultiplier(target, multiplier);
            }
        };
    }

    /**
     * Create a multiplied effect
     */
    public static BlockEffect multiplied(BlockEffect effect, float multiplier) {
        return new BlockEffect() {
            @Override
            public void apply(GameObject target) {
                effect.applyWithMultiplier(target, multiplier);
            }

            @Override
            public void applyWithMultiplier(GameObject target, float mult) {
                effect.applyWithMultiplier(target, multiplier * mult);
            }
        };
    }
}
