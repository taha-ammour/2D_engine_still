// src/main/java/org/example/core/scenes/BaseScene.java
package org.example.core.scenes;

import org.example.core.Scene;
import org.example.engine.input.Input;
import org.example.engine.input.Mouse;
import org.example.gfx.Camera2D;
import org.example.gfx.Renderer2D;

/**
 * Template Method Pattern: Provides common scene structure
 * Open/Closed: Scenes extend this, closed for modification
 */
public abstract class BaseScene implements Scene {
    protected final Input input;
    protected final Mouse mouse;
    protected final Renderer2D renderer;
    protected final Camera2D camera;

    public BaseScene(Input input, Mouse mouse, Renderer2D renderer, Camera2D camera) {
        this.input = input;
        this.mouse = mouse;
        this.renderer = renderer;
        this.camera = camera;
    }

    @Override
    public void load() {
        onLoad();
    }

    @Override
    public void unload() {
        onUnload();
    }

    protected abstract void onLoad();
    protected abstract void onUnload();
}