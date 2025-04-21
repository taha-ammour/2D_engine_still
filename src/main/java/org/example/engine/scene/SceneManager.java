package org.example.engine.scene;

import org.example.engine.physics.PhysicsSystem;
import org.example.engine.rendering.RenderSystem;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages multiple scenes and handles transitions between them.
 */
public class SceneManager {
    private static SceneManager instance;

    // Scene collection
    private final Map<String, Scene> scenes = new HashMap<>();

    // Currently active scene
    private Scene activeScene;

    // Systems
    private RenderSystem renderSystem;
    private PhysicsSystem physicsSystem;

    // Scene transition state
    private Scene pendingScene;
    private boolean transitionPending = false;
    private TransitionCallback transitionCallback;

    /**
     * Get the singleton instance
     */
    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    /**
     * Private constructor for singleton
     */
    private SceneManager() {
    }

    /**
     * Initialize the scene manager
     */
    public void init(RenderSystem renderSystem, PhysicsSystem physicsSystem) {
        this.renderSystem = renderSystem;
        this.physicsSystem = physicsSystem;
    }

    /**
     * Register a scene
     */
    public void registerScene(Scene scene) {
        if (scene == null) return;

        scenes.put(scene.getName(), scene);
    }

    /**
     * Unregister a scene
     */
    public void unregisterScene(String sceneName) {
        // Cannot unregister active scene
        if (activeScene != null && activeScene.getName().equals(sceneName)) {
            return;
        }

        Scene scene = scenes.remove(sceneName);
        if (scene != null) {
            scene.cleanup();
        }
    }

    /**
     * Load a scene
     */
    public Scene loadScene(String sceneName) {
        Scene scene = scenes.get(sceneName);
        if (scene == null) {
            throw new IllegalArgumentException("Scene not found: " + sceneName);
        }

        // If we have an active scene, set up transition
        if (activeScene != null) {
            pendingScene = scene;
            transitionPending = true;
            return scene;
        }

        // Otherwise, load immediately
        activateScene(scene);
        return scene;
    }

    /**
     * Load a scene with a transition callback
     */
    public Scene loadScene(String sceneName, TransitionCallback callback) {
        this.transitionCallback = callback;
        return loadScene(sceneName);
    }

    /**
     * Create a new scene and register it
     */
    public Scene createScene(String name) {
        // Check if the scene already exists
        if (scenes.containsKey(name)) {
            throw new IllegalArgumentException("Scene already exists: " + name);
        }

        // Create a new scene
        Scene scene = new Scene(name);
        registerScene(scene);
        return scene;
    }

    /**
     * Activate a scene
     */
    private void activateScene(Scene scene) {
        // Deactivate current scene if exists
        if (activeScene != null) {
            activeScene.setActive(false);
        }

        // Initialize and activate new scene
        scene.init(renderSystem, physicsSystem);
        scene.setActive(true);
        activeScene = scene;
    }

    /**
     * Update the scene manager
     */
    public void update(float deltaTime) {
        // Handle pending scene transition
        if (transitionPending && pendingScene != null) {
            // Call transition callback if exists
            if (transitionCallback != null) {
                transitionCallback.onTransitionStart(activeScene, pendingScene);
            }

            // Switch scenes
            Scene oldScene = activeScene;
            activateScene(pendingScene);

            // Call transition callback again
            if (transitionCallback != null) {
                transitionCallback.onTransitionComplete(oldScene, activeScene);
                transitionCallback = null;
            }

            pendingScene = null;
            transitionPending = false;
        }

        // Update active scene
        if (activeScene != null) {
            activeScene.update(deltaTime);
        }
    }

    /**
     * Render the active scene
     */
    public void render() {
        if (activeScene != null) {
            activeScene.render();
        }
    }

    /**
     * Get the active scene
     */
    public Scene getActiveScene() {
        return activeScene;
    }

    /**
     * Get a scene by name
     */
    public Scene getScene(String name) {
        return scenes.get(name);
    }

    /**
     * Check if a scene exists
     */
    public boolean hasScene(String name) {
        return scenes.containsKey(name);
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        for (Scene scene : scenes.values()) {
            scene.cleanup();
        }
        scenes.clear();
        activeScene = null;
        pendingScene = null;
    }

    /**
     * Interface for scene transition callbacks
     */
    public interface TransitionCallback {
        void onTransitionStart(Scene oldScene, Scene newScene);
        void onTransitionComplete(Scene oldScene, Scene newScene);
    }
}