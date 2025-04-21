package org.example.engine.animation;

import org.example.engine.rendering.Texture;

/**
 * Represents a single frame in an animation.
 */
public class AnimationFrame {
    // Texture for the frame
    public Texture texture;

    // UV coordinates (normalized 0-1) for texture regions
    public float u0 = -1;
    public float v0 = -1;
    public float u1 = -1;
    public float v1 = -1;

    // Color palette for palette swapping
    public String[] palette;

    // Frame properties
    public float duration = -1; // Duration in seconds, -1 means use default
    public boolean flipX = false;
    public boolean flipY = false;

    // Optional user data
    public Object userData;

    /**
     * Create a new animation frame with the specified texture
     */
    public AnimationFrame(Texture texture) {
        this.texture = texture;
    }

    /**
     * Create a new animation frame with the specified texture and duration
     */
    public AnimationFrame(Texture texture, float duration) {
        this.texture = texture;
        this.duration = duration;
    }

    /**
     * Create a new animation frame with the specified texture and UV coordinates
     */
    public AnimationFrame(Texture texture, float u0, float v0, float u1, float v1) {
        this.texture = texture;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
    }

    /**
     * Create a copy of this frame
     */
    public AnimationFrame copy() {
        AnimationFrame copy = new AnimationFrame(texture);
        copy.u0 = u0;
        copy.v0 = v0;
        copy.u1 = u1;
        copy.v1 = v1;
        copy.duration = duration;
        copy.flipX = flipX;
        copy.flipY = flipY;

        if (palette != null) {
            copy.palette = new String[palette.length];
            System.arraycopy(palette, 0, copy.palette, 0, palette.length);
        }

        copy.userData = userData;

        return copy;
    }

    /**
     * Set the frame to be flipped horizontally
     */
    public AnimationFrame setFlipX(boolean flip) {
        this.flipX = flip;
        return this;
    }

    /**
     * Set the frame to be flipped vertically
     */
    public AnimationFrame setFlipY(boolean flip) {
        this.flipY = flip;
        return this;
    }

    /**
     * Set the frame duration
     */
    public AnimationFrame setDuration(float duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Set the palette for this frame
     */
    public AnimationFrame setPalette(String[] palette) {
        this.palette = palette;
        return this;
    }

    /**
     * Set user data for this frame
     */
    public AnimationFrame setUserData(Object userData) {
        this.userData = userData;
        return this;
    }
}