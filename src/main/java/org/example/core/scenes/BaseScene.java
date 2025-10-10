
// src/main/java/org/example/core/scenes/BaseScene.java
package org.example.core.scenes;

import org.example.core.Scene;
import org.example.engine.input.Input;
import org.example.gfx.Camera2D;
import org.example.gfx.Renderer2D;

/**
 * Template Method Pattern: Provides common scene structure
 */
public abstract class BaseScene implements Scene {
    protected final Input input;
    protected final Renderer2D renderer;
    protected final Camera2D camera;

    public BaseScene(Input input, Renderer2D renderer, Camera2D camera) {
        this.input = input;
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