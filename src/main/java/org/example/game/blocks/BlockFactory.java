// ============================================
// BLOCK FACTORY & BUILDER
// ============================================

// ===== 1. BLOCK FACTORY =====
// src/main/java/org/example/blocks/BlockFactory.java
package org.example.game.blocks;

import org.example.game.blocks.effects.*;
import org.example.ecs.GameObject;
import org.example.gfx.*;
import org.example.physics.*;

/**
 * Factory Pattern: Creates configured blocks
 * Abstract Factory: Different block families
 * Builder Pattern: Fluent API for complex configurations
 */
public final class BlockFactory {
    private final Renderer2D renderer;
    private final CollisionSystem collisionSystem;

    public BlockFactory(Renderer2D renderer, CollisionSystem collisionSystem) {
        this.renderer = renderer;
        this.collisionSystem = collisionSystem;
    }

    /**
     * Create a block with Builder pattern
     */
    public BlockBuilder builder() {
        return new BlockBuilder(renderer, collisionSystem);
    }

    // ===== CONVENIENCE METHODS =====

    public GameObject createLuckyBlock(float x, float y) {
        return builder()
                .position(x, y)
                .lucky()
                .effect(BlockEffects.COIN)
                .build();
    }

    public GameObject createPoisonBlock(float x, float y) {
        return builder()
                .position(x, y)
                .poison()
                .build();
    }

    public GameObject createBouncyBlock(float x, float y) {
        return builder()
                .position(x, y)
                .bouncy()
                .build();
    }

    public GameObject createMysteryBlock(float x, float y) {
        return builder()
                .position(x, y)
                .mystery()
                .build();
    }

    public GameObject createCoinBlock(float x, float y, int coins) {
        return builder()
                .position(x, y)
                .coins(coins)
                .build();
    }

    // ===== ADVANCED BLOCKS WITH DECORATORS =====

    public GameObject createDoubleEffectLuckyBlock(float x, float y) {
        return builder()
                .position(x, y)
                .lucky()
                .effect(BlockEffects.POWER_UP)
                .doubleEffect()
                .animated("lucky_idle", "lucky_hit")
                .shader("effects", ShaderEffect.PULSATE, ShaderEffect.OUTLINE)
                .build();
    }

    public GameObject createAnimatedPoisonBlock(float x, float y) {
        return builder()
                .position(x, y)
                .poison()
                .animated("poison_idle", "poison_hit")
                .shader("effects", ShaderEffect.WOBBLE, ShaderEffect.PULSATE)
                .build();
    }

    public GameObject createSuperBouncyBlock(float x, float y) {
        return builder()
                .position(x, y)
                .bouncy()
                .doubleEffect() // 2x bounce height!
                .build();
    }
}

