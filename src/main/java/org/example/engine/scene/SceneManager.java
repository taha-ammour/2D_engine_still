// src/main/java/org/example/engine/scene/SceneManager.java
package org.example.engine.scene;

import org.example.core.Scene;
import java.util.HashMap;
import java.util.Map;

/**
 * Single Responsibility: Manages scene lifecycle and transitions
 * Open/Closed: Easily extend with new scene types
 */
public final class SceneManager {
    private Scene currentScene;
    private Scene nextScene;
    private final Map<String, Scene> scenes = new HashMap<>();
    private boolean transitioning = false;

    public void registerScene(String name, Scene scene) {
        scenes.put(name, scene);
    }

    public void loadScene(Scene scene) {
        if (currentScene != null) {
            currentScene.unload();
        }
        currentScene = scene;
        currentScene.load();
    }

    public void transitionTo(String sceneName) {
        Scene scene = scenes.get(sceneName);
        if (scene == null) {
            throw new IllegalArgumentException("Scene not found: " + sceneName);
        }
        nextScene = scene;
        transitioning = true;
    }

    public void updateCurrent(double dt) {
        if (transitioning) {
            loadScene(nextScene);
            nextScene = null;
            transitioning = false;
        }

        if (currentScene != null) {
            currentScene.update(dt);
        }
    }

    public void renderCurrent() {
        if (currentScene != null) {
            currentScene.render();
        }
    }

    public void handleInput() {
        if (currentScene != null) {
            currentScene.handleInput();
        }
    }

    public void unloadAll() {
        if (currentScene != null) {
            currentScene.unload();
        }
        scenes.clear();
    }

    public Scene getCurrentScene() {
        return currentScene;
    }
}