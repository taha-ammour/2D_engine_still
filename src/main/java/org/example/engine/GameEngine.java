// src/main/java/org/example/engine/GameEngine.java
package org.example.engine;

import org.example.engine.time.GameTime;
import org.example.engine.scene.SceneManager;

import org.example.core.Scene;
import org.example.gfx.Camera2D;
import org.example.gfx.Renderer2D;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;

/**
 * Single Responsibility: Manages the game loop and engine lifecycle
 */
public final class GameEngine implements AutoCloseable {
    private final Window window;
    private final Renderer2D renderer;
    private final Camera2D camera;
    private final SceneManager sceneManager;
    private final GameTime gameTime;

    private boolean running = false;
    private static final double FIXED_TIMESTEP = 1.0 / 60.0;

    public GameEngine(int width, int height, String title) {
        this.window = new Window(width, height, title);
        this.camera = new Camera2D(width, height);
        this.renderer = new Renderer2D(camera);
        this.sceneManager = new SceneManager();
        this.gameTime = new GameTime();
    }

    public void init() {
        window.init();
        // Now that OpenGL context exists, initialize renderer
        renderer.init();
    }

    public void start(Scene initialScene) {
        if (running) throw new IllegalStateException("Engine already running");

        sceneManager.loadScene(initialScene);
        running = true;
        gameLoop();
    }

    private void gameLoop() {
        double accumulator = 0.0;
        double currentTime = glfwGetTime();

        while (running && !window.shouldClose()) {
            double newTime = glfwGetTime();
            double frameTime = newTime - currentTime;
            currentTime = newTime;

            gameTime.update(frameTime);
            accumulator += frameTime;

            window.pollEvents();

            if (window.wasResized()) {
                camera.resize(window.width(), window.height());
            }

            processInput();

            // Fixed timestep updates
            while (accumulator >= FIXED_TIMESTEP) {
                sceneManager.updateCurrent(FIXED_TIMESTEP);
                accumulator -= FIXED_TIMESTEP;
            }

            // Render
            glClearColor(0.10f, 0.12f, 0.16f, 1f);
            glClear(GL_COLOR_BUFFER_BIT);

            renderer.begin(window.width(), window.height());
            sceneManager.renderCurrent();
            renderer.end();

            window.swap();
        }
    }

    private void processInput() {
        sceneManager.handleInput();
    }

    public void stop() {
        running = false;
    }

    public Window getWindow() { return window; }
    public Renderer2D getRenderer() { return renderer; }
    public Camera2D getCamera() { return camera; }
    public GameTime getGameTime() { return gameTime; }

    @Override
    public void close() {
        sceneManager.unloadAll();
        renderer.close();
        window.close();
    }
}