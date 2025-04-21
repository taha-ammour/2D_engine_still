package org.example.engine.rendering;

import org.example.engine.core.Transform;
import org.joml.Matrix4f;

/**
 * Interface for objects that can be rendered by the RenderSystem.
 */
public interface Renderable {
    /**
     * Render this object
     */
    void render(RenderSystem renderSystem, Matrix4f viewProjectionMatrix);

    /**
     * Get the Z-order for depth sorting
     */
    float getZ();

    /**
     * Get the width for culling calculations
     */
    float getWidth();

    /**
     * Get the height for culling calculations
     */
    float getHeight();

    /**
     * Get the transform for position information
     */
    Transform getTransform();

    /**
     * Check if this object uses transparency
     */
    boolean isTransparent();

    /**
     * Get the material used for rendering
     */
    Material getMaterial();
}