package org.example.engine.animation;

import org.example.engine.core.Component;
import org.example.engine.rendering.Sprite;

import java.util.*;

/**
 * Component that handles sprite animations.
 * Manages transitions between different animation states.
 */
public class Animator extends Component {
    // Current animation state
    private String currentState;
    private Animation currentAnimation;
    private boolean isPlaying = false;
    private float timeAccumulator = 0;
    private int currentFrameIndex = 0;

    // Target sprite to animate
    private Sprite targetSprite;

    // Animation collection
    private final Map<String, Animation> animations = new HashMap<>();

    // Animation events
    private final Map<String, List<AnimationEventListener>> eventListeners = new HashMap<>();

    // Animation parameters
    private float speed = 1.0f;
    private boolean loop = true;

    /**
     * Create a new animator
     */
    public Animator() {
    }

    /**
     * Create a new animator with a target sprite
     */
    public Animator(Sprite targetSprite) {
        this.targetSprite = targetSprite;
    }

    @Override
    protected void onInit() {
        super.onInit();

        // Find a sprite on the GameObject if none was specified
        if (targetSprite == null) {
            targetSprite = getGameObject().getComponent(Sprite.class);
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (!isPlaying || currentAnimation == null || targetSprite == null) {
            return;
        }

        // Update animation time
        timeAccumulator += deltaTime * speed;

        // Get current frame duration
        float frameDuration = currentAnimation.getFrameDuration(currentFrameIndex);

        // Check if we should advance to the next frame
        if (timeAccumulator >= frameDuration) {
            // Move to next frame
            timeAccumulator -= frameDuration;
            int nextFrame = currentFrameIndex + 1;

            // Check if we've reached the end of the animation
            if (nextFrame >= currentAnimation.getFrameCount()) {
                // Trigger animation complete event
                triggerEvent("complete");

                // Check if we should loop
                if (loop) {
                    currentFrameIndex = 0;
                } else {
                    isPlaying = false;
                    return;
                }
            } else {
                currentFrameIndex = nextFrame;
            }

            // Apply the frame
            applyFrame(currentAnimation.getFrame(currentFrameIndex));

            // Trigger frame changed event
            triggerEvent("frameChanged");
        }
    }

    /**
     * Apply an animation frame to the target sprite
     */
    private void applyFrame(AnimationFrame frame) {
        // Update sprite properties based on the frame
        if (frame.texture != null) {
            targetSprite.setTexture(frame.texture);
        }

        // Set UV coordinates if provided
        if (frame.u0 >= 0 && frame.v0 >= 0 && frame.u1 > 0 && frame.v1 > 0) {
            // TODO: Update UV coordinates (depends on Sprite implementation)
        }

        // Set palette if provided
        if (frame.palette != null) {
            targetSprite.setPaletteFromCodes(frame.palette);
        }

        // Apply flip settings
        targetSprite.setFlipX(frame.flipX);
        targetSprite.setFlipY(frame.flipY);
    }

    /**
     * Add an animation state
     */
    public void addAnimation(String name, Animation animation) {
        animations.put(name, animation);
    }

    /**
     * Get all animation states
     */
    public Set<String> getAnimationNames() {
        return Collections.unmodifiableSet(animations.keySet());
    }

    /**
     * Play an animation state
     */
    public boolean play(String name) {
        // Check if the animation exists
        Animation animation = animations.get(name);
        if (animation == null) {
            return false;
        }

        // Check if we're already playing this animation
        if (isPlaying && name.equals(currentState)) {
            return true;
        }

        // Set the current animation
        currentState = name;
        currentAnimation = animation;
        currentFrameIndex = 0;
        timeAccumulator = 0;
        isPlaying = true;

        // Apply the first frame
        if (targetSprite != null && currentAnimation.getFrameCount() > 0) {
            applyFrame(currentAnimation.getFrame(0));
        }

        // Trigger animation started event
        triggerEvent("start");

        return true;
    }

    /**
     * Stop the current animation
     */
    public void stop() {
        isPlaying = false;
        triggerEvent("stop");
    }

    /**
     * Pause the current animation
     */
    public void pause() {
        isPlaying = false;
        triggerEvent("pause");
    }

    /**
     * Resume the current animation
     */
    public void resume() {
        isPlaying = true;
        triggerEvent("resume");
    }

    /**
     * Check if an animation is playing
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Get the current animation state name
     */
    public String getCurrentState() {
        return currentState;
    }

    /**
     * Get the current animation speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Set the animation speed
     */
    public void setSpeed(float speed) {
        this.speed = Math.max(0.01f, speed);
    }

    /**
     * Check if the animation is looping
     */
    public boolean isLooping() {
        return loop;
    }

    /**
     * Set whether the animation should loop
     */
    public void setLooping(boolean loop) {
        this.loop = loop;
    }

    /**
     * Set the target sprite
     */
    public void setTargetSprite(Sprite sprite) {
        this.targetSprite = sprite;
    }

    /**
     * Get the target sprite
     */
    public Sprite getTargetSprite() {
        return targetSprite;
    }

    /**
     * Add an event listener
     */
    public void addEventListener(String eventType, AnimationEventListener listener) {
        eventListeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Remove an event listener
     */
    public void removeEventListener(String eventType, AnimationEventListener listener) {
        List<AnimationEventListener> listeners = eventListeners.get(eventType);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                eventListeners.remove(eventType);
            }
        }
    }

    /**
     * Trigger an event
     */
    private void triggerEvent(String eventType) {
        List<AnimationEventListener> listeners = eventListeners.get(eventType);
        if (listeners != null) {
            AnimationEvent event = new AnimationEvent(eventType, currentState, currentFrameIndex);
            for (AnimationEventListener listener : listeners) {
                listener.onAnimationEvent(event);
            }
        }
    }

    /**
     * Interface for animation event listeners
     */
    public interface AnimationEventListener {
        void onAnimationEvent(AnimationEvent event);
    }

    /**
     * Animation event class
     */
    public static class AnimationEvent {
        public final String type;
        public final String state;
        public final int frameIndex;

        public AnimationEvent(String type, String state, int frameIndex) {
            this.type = type;
            this.state = state;
            this.frameIndex = frameIndex;
        }
    }
}