package org.example.game.blocks.decorators;

import org.example.ecs.GameObject;
import org.example.game.blocks.Block;
import org.example.gfx.Renderer2D;

/**
 * Decorator Pattern: Adds behavior to blocks dynamically
 * Allows stacking multiple decorators for complex behavior
 */
public abstract class BlockDecorator extends Block {
    protected final Block decoratedBlock;

    public BlockDecorator(Block block) {
        this.decoratedBlock = block;
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
        // CRITICAL: Pass renderer to decorated block BEFORE setup
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
    public void setRenderer(Renderer2D renderer) {
        this.renderer = renderer;
        // Also set on decorated block
        decoratedBlock.setRenderer(renderer);
    }

    @Override
    public void setOwner(GameObject owner) {
        this.owner = owner;
        // Also set on decorated block
        decoratedBlock.setOwner(owner);
    }

    @Override
    public void onAttach() {
        // CRITICAL: Share owner and renderer with decorated block BEFORE calling setup
        decoratedBlock.setOwner(this.owner);
        decoratedBlock.setRenderer(this.renderer);
        decoratedBlock.onAttach();
    }

}
