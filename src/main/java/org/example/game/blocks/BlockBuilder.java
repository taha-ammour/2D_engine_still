package org.example.game.blocks;

import org.example.ecs.GameObject;
import org.example.ecs.components.Transform;
import org.example.game.blocks.decorators.AnimatedBlockDecorator;
import org.example.game.blocks.decorators.DoubleEffectDecorator;
import org.example.game.blocks.decorators.ShaderBlockDecorator;
import org.example.game.blocks.effects.BlockEffect;
import org.example.game.blocks.types.*;
import org.example.gfx.Renderer2D;
import org.example.gfx.ShaderEffect;
import org.example.physics.BoxCollider;
import org.example.physics.CollisionSystem;

/**
 * Builder Pattern: Fluent API for creating complex blocks
 * Ensures all required components are added
 */
public final class BlockBuilder {
    private final Renderer2D renderer;
    private final CollisionSystem collisionSystem;

    // Configuration
    private float x = 0, y = 0;
    private Block baseBlock;
    private BlockEffect effect;

    // Decorators to apply
    private boolean applyDoubleEffect = false;
    private boolean applyAnimation = false;
    private String idleAnim, hitAnim;
    private boolean applyShader = false;
    private String shaderName;
    private ShaderEffect[] shaderEffects;

    public BlockBuilder(Renderer2D renderer, CollisionSystem collisionSystem) {
        this.renderer = renderer;
        this.collisionSystem = collisionSystem;
    }

    // ===== POSITION =====

    public BlockBuilder position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    // ===== BLOCK TYPES =====

    public BlockBuilder lucky() {
        this.baseBlock = new LuckyBlock();
        return this;
    }

    public BlockBuilder poison() {
        this.baseBlock = new PoisonBlock();
        return this;
    }

    public BlockBuilder bouncy() {
        this.baseBlock = new BouncyBlock();
        return this;
    }

    public BlockBuilder mystery() {
        this.baseBlock = new MysteryBlock();
        return this;
    }

    public BlockBuilder coins(int count) {
        this.baseBlock = new CoinBlock(count);
        return this;
    }

    public BlockBuilder custom(Block block) {
        this.baseBlock = block;
        return this;
    }

    // ===== EFFECTS =====

    public BlockBuilder effect(BlockEffect effect) {
        this.effect = effect;
        if (baseBlock instanceof LuckyBlock lucky) {
            // Recreate with new effect
            baseBlock = new LuckyBlock(effect);
        }
        return this;
    }

    // ===== DECORATORS =====

    public BlockBuilder doubleEffect() {
        this.applyDoubleEffect = true;
        return this;
    }

    public BlockBuilder animated(String idleAnim, String hitAnim) {
        this.applyAnimation = true;
        this.idleAnim = idleAnim;
        this.hitAnim = hitAnim;
        return this;
    }

    public BlockBuilder shader(String shaderName, ShaderEffect... effects) {
        this.applyShader = true;
        this.shaderName = shaderName;
        this.shaderEffects = effects;
        return this;
    }

    // ===== BUILD =====

    public GameObject build() {
        if (baseBlock == null) {
            throw new IllegalStateException("Must specify block type!");
        }

        // Apply decorators in order
        Block finalBlock = baseBlock;

        if (applyDoubleEffect) {
            finalBlock = new DoubleEffectDecorator(finalBlock);
        }

        if (applyAnimation) {
            finalBlock = new AnimatedBlockDecorator(finalBlock, idleAnim, hitAnim);
        }

        if (applyShader) {
            finalBlock = new ShaderBlockDecorator(finalBlock, shaderName, shaderEffects);
        }

        // Create GameObject
        GameObject blockObject = new GameObject("Block_" + x + "_" + y);

        // Add Transform
        Transform transform = new Transform(x, y);
        blockObject.addComponent(transform);

        // ✅ CRITICAL FIX: Set renderer and owner BEFORE completeSetup
        finalBlock.setOwner(blockObject);
        finalBlock.setRenderer(renderer);

        // Add the block component
        blockObject.addComponent(finalBlock);

        // Now setup visuals (renderer is already set)
        finalBlock.completeSetup();

        // Register collider
        BoxCollider collider = blockObject.getComponent(BoxCollider.class);
        if (collider != null) {
            collisionSystem.register(collider);
        }

        return blockObject;
    }
}
