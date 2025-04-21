package org.example.engine.animation;

import org.example.engine.rendering.Texture;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sequence of sprite frames that form an animation.
 */
public class Animation {
    // List of animation frames
    private final List<AnimationFrame> frames = new ArrayList<>();

    // Default duration for frames (in seconds)
    private float defaultFrameDuration = 0.1f;

    /**
     * Create a new animation with default settings
     */
    public Animation() {
    }

    /**
     * Create a new animation with the specified default frame duration
     */
    public Animation(float defaultFrameDuration) {
        this.defaultFrameDuration = Math.max(0.01f, defaultFrameDuration);
    }

    /**
     * Get the default frame duration
     */
    public float getDefaultFrameDuration() {
        return defaultFrameDuration;
    }

    /**
     * Set the default frame duration
     */
    public void setDefaultFrameDuration(float duration) {
        this.defaultFrameDuration = Math.max(0.01f, duration);
    }

    /**
     * Get the number of frames in this animation
     */
    public int getFrameCount() {
        return frames.size();
    }

    /**
     * Get a specific frame
     */
    public AnimationFrame getFrame(int index) {
        if (index < 0 || index >= frames.size()) {
            throw new IndexOutOfBoundsException("Animation frame index out of bounds: " + index);
        }
        return frames.get(index);
    }

    /**
     * Get the duration of a specific frame
     */
    public float getFrameDuration(int index) {
        if (index < 0 || index >= frames.size()) {
            throw new IndexOutOfBoundsException("Animation frame index out of bounds: " + index);
        }

        float duration = frames.get(index).duration;
        return duration > 0 ? duration : defaultFrameDuration;
    }

    /**
     * Get the total duration of the animation
     */
    public float getTotalDuration() {
        float total = 0;
        for (int i = 0; i < frames.size(); i++) {
            total += getFrameDuration(i);
        }
        return total;
    }

    /**
     * Add a frame to the animation
     */
    public void addFrame(AnimationFrame frame) {
        frames.add(frame);
    }

    /**
     * Add a frame with the specified texture
     * Renamed to avoid ambiguity
     */
    public void addTextureFrame(Texture texture) {
        addFrame(new AnimationFrame(texture, -1));
    }

    /**
     * Add a frame with the specified texture and duration
     * Renamed to avoid ambiguity
     */
    public void addTextureFrame(Texture texture, float duration) {
        addFrame(new AnimationFrame(texture, duration));
    }

    /**
     * Add a frame with the specified texture, UV coordinates, and duration
     */
    public void addTextureFrame(Texture texture, float u0, float v0, float u1, float v1, float duration) {
        AnimationFrame frame = new AnimationFrame(texture, duration);
        frame.u0 = u0;
        frame.v0 = v0;
        frame.u1 = u1;
        frame.v1 = v1;
        addFrame(frame);
    }

    /**
     * Add a frame with a specific palette
     */
    public void addFrameWithPalette(Texture texture, String[] palette, float duration) {
        AnimationFrame frame = new AnimationFrame(texture, duration);
        frame.palette = palette;
        addFrame(frame);
    }

    /**
     * Create an animation from a sprite sheet
     */
    public static Animation fromSpriteSheet(Texture spriteSheet, int frameWidth, int frameHeight,
                                            int frameCount, float frameDuration) {
        Animation animation = new Animation(frameDuration);

        int textureWidth = spriteSheet.getWidth();
        int textureHeight = spriteSheet.getHeight();

        int framesPerRow = textureWidth / frameWidth;

        for (int i = 0; i < frameCount; i++) {
            int col = i % framesPerRow;
            int row = i / framesPerRow;

            float u0 = (float) (col * frameWidth) / textureWidth;
            float v0 = (float) (row * frameHeight) / textureHeight;
            float u1 = (float) ((col + 1) * frameWidth) / textureWidth;
            float v1 = (float) ((row + 1) * frameHeight) / textureHeight;

            animation.addTextureFrame(spriteSheet, u0, v0, u1, v1, frameDuration);
        }

        return animation;
    }

    /**
     * Remove all frames from the animation
     */
    public void clear() {
        frames.clear();
    }
}