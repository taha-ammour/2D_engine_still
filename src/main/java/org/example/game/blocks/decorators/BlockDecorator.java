package org.example.game.blocks.decorators;

import org.example.ecs.GameObject;
import org.example.game.blocks.Block;
import org.example.game.blocks.effects.BlockEffect;  // ✅ ADD THIS IMPORT
import org.example.gfx.Renderer2D;
import org.example.physics.Collision;

/**
 * Decorator Pattern: Adds behavior to blocks dynamically
 * Allows stacking multiple decorators for complex behavior
 */
public abstract class BlockDecorator extends Block {
    protected final Block decoratedBlock;

    public BlockDecorator(Block block) {
        this.decoratedBlock = block;
    }

    // ✅ Override onCollision to call super (which calls OUR onHitFromBelow)
    @Override
    public void onCollision(Collision collision) {
        // Call the base Block's onCollision, which will call THIS decorator's onHitFromBelow
        super.onCollision(collision);
    }

    @Override
    public void onCollisionEnter(GameObject other) {
        decoratedBlock.onCollisionEnter(other);
    }

    @Override
    public void onCollisionExit(GameObject other) {
        decoratedBlock.onCollisionExit(other);
    }

    @Override
    public void setupVisuals() {
        decoratedBlock.setupVisuals();
    }

    @Override
    public void updateAnimation(double dt) {
        decoratedBlock.updateAnimation(dt);
    }

    @Override
    public void updateState(double dt) {
        decoratedBlock.updateState(dt);
    }

    @Override
    public void completeSetup() {
        decoratedBlock.setRenderer(this.renderer);
        decoratedBlock.setOwner(this.owner);
        decoratedBlock.completeSetup();
    }

    @Override
    public void update(double dt) {
        decoratedBlock.update(dt);  // Added this
    }

    @Override
    public void onHit(GameObject player) {
        decoratedBlock.onHit(player);
    }

    @Override
    public void onHitFromBelow(GameObject player) {
        decoratedBlock.onHitFromBelow(player);
    }

    @Override
    public void setRenderer(Renderer2D renderer) {
        this.renderer = renderer;
        decoratedBlock.setRenderer(renderer);
    }

    @Override
    public void setOwner(GameObject owner) {
        this.owner = owner;
        decoratedBlock.setOwner(owner);
    }

    @Override
    public void onAttach() {
        decoratedBlock.setOwner(this.owner);
        decoratedBlock.setRenderer(this.renderer);
        decoratedBlock.onAttach();
    }

    @Override
    public BlockEffect getEffect() {
        return decoratedBlock.getEffect();
    }
}