// src/main/java/org/example/effects/ScreenShake.java
package org.example.effects;

import org.example.gfx.Camera2D;

/**
 * Screen shake effect for impacts, explosions, and dramatic moments
 * Adds juice and feedback to your game!
 */
public final class ScreenShake {
    private final Camera2D camera;

    private float trauma = 0f;
    private float traumaDecay = 1.5f;  // How fast shake fades
    private float maxAngle = 0.1f;     // Max rotation (radians)
    private float maxOffset = 10f;     // Max position offset

    private float originalX = 0f;
    private float originalY = 0f;
    private boolean shaking = false;

    public ScreenShake(Camera2D camera) {
        this.camera = camera;
    }

    /**
     * Add trauma to trigger shake
     * @param amount 0.0 to 1.0 (intensity)
     */
    public void addTrauma(float amount) {
        trauma = Math.min(1f, trauma + amount);

        if (!shaking) {
            originalX = camera.getPosition().x;
            originalY = camera.getPosition().y;
            shaking = true;
        }
    }

    /**
     * Shake with preset intensities
     */
    public void shakeLight() {
        addTrauma(0.2f);
    }

    public void shakeMedium() {
        addTrauma(0.5f);
    }

    public void shakeHeavy() {
        addTrauma(0.8f);
    }

    public void shakeExplosion() {
        addTrauma(1.0f);
    }

    /**
     * Update shake effect
     */
    public void update(float dt) {
        if (trauma <= 0) {
            if (shaking) {
                // Restore original camera position
                camera.setPosition(originalX, originalY);
                shaking = false;
            }
            return;
        }

        // Decay trauma over time
        trauma = Math.max(0, trauma - traumaDecay * dt);

        // Shake amount (squared for smoother feel)
        float shake = trauma * trauma;

        // Random offset and angle
        float offsetX = maxOffset * shake * (float)(Math.random() * 2 - 1);
        float offsetY = maxOffset * shake * (float)(Math.random() * 2 - 1);

        // Apply shake to camera
        camera.setPosition(
                originalX + offsetX,
                originalY + offsetY
        );
    }

    /**
     * Stop shake immediately
     */
    public void stop() {
        trauma = 0f;
        if (shaking) {
            camera.setPosition(originalX, originalY);
            shaking = false;
        }
    }

    /**
     * Configure shake parameters
     */
    public void setDecayRate(float rate) {
        this.traumaDecay = rate;
    }

    public void setMaxOffset(float offset) {
        this.maxOffset = offset;
    }

    public void setMaxAngle(float angle) {
        this.maxAngle = angle;
    }

    public boolean isShaking() {
        return shaking;
    }

    public float getTrauma() {
        return trauma;
    }
}