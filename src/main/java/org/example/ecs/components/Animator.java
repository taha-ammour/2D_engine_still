// src/main/java/org/example/ecs/components/Animator.java
package org.example.ecs.components;

import org.example.ecs.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * Animation Component: Manages sprite animation states
 * State Pattern: Switches between animation clips
 */
public final class Animator extends Component {
    private final Map<String, AnimationClip> clips = new HashMap<>();
    private AnimationClip currentClip;
    private String currentClipName;
    private float time = 0f;

    public void addClip(String name, AnimationClip clip) {
        clips.put(name, clip);
        if (currentClip == null) {
            play(name);
        }
    }

    public void play(String name) {
        if (currentClipName != null && currentClipName.equals(name)) {
            return; // Already playing
        }

        AnimationClip clip = clips.get(name);
        if (clip != null) {
            currentClip = clip;
            currentClipName = name;
            time = 0f;
        }
    }

    @Override
    public void update(double dt) {
        if (currentClip == null) return;

        time += (float)dt;
        float frameDuration = 1f / currentClip.fps;

        if (time >= frameDuration) {
            time = 0f;
            int nextFrame = currentClip.currentFrame + 1;

            if (nextFrame >= currentClip.frameCount) {
                if (currentClip.loop) {
                    currentClip.currentFrame = 0;
                } else {
                    currentClip.currentFrame = currentClip.frameCount - 1;
                }
            } else {
                currentClip.currentFrame = nextFrame;
            }

            // Update sprite renderer frame
            SpriteRenderer sprite = owner.getComponent(SpriteRenderer.class);
            if (sprite != null) {
                sprite.frame = currentClip.startFrame + currentClip.currentFrame;
            }
        }
    }

    public String getCurrentClip() {
        return currentClipName;
    }
}
