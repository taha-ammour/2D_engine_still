// src/main/java/org/example/ecs/components/AnimationClip.java
package org.example.ecs.components;

/**
 * Value Object: Defines an animation sequence
 */
public final class AnimationClip {
    public final int startFrame;
    public final int frameCount;
    public final float fps;
    public final boolean loop;

    int currentFrame = 0;

    public AnimationClip(int startFrame, int frameCount, float fps, boolean loop) {
        this.startFrame = startFrame;
        this.frameCount = frameCount;
        this.fps = fps;
        this.loop = loop;
    }

    public static AnimationClip create(int start, int count, float fps) {
        return new AnimationClip(start, count, fps, true);
    }
}