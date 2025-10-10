// src/main/java/org/example/engine/time/GameTime.java
package org.example.engine.time;

/**
 * Single Responsibility: Tracks and provides time information
 */
public final class GameTime {
    private double deltaTime;
    private double totalTime;
    private int frameCount;
    private float fps;
    private double fpsTimer;

    public void update(double dt) {
        deltaTime = dt;
        totalTime += dt;
        frameCount++;

        fpsTimer += dt;
        if (fpsTimer >= 1.0) {
            fps = frameCount / (float)fpsTimer;
            frameCount = 0;
            fpsTimer = 0.0;
        }
    }

    public double getDeltaTime() { return deltaTime; }
    public double getTotalTime() { return totalTime; }
    public float getFPS() { return fps; }
}