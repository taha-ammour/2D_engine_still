package org.example.engine.scene;

import org.example.engine.core.GameObject;
import org.example.engine.physics.PhysicsSystem;
import org.example.engine.rendering.Camera;
import org.example.engine.rendering.RenderSystem;
import org.example.engine.rendering.Light;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A Scene contains GameObjects and manages their lifecycle.
 * It serves as the root of the game object hierarchy.
 */
public class Scene {
    private final String name;
    private final List<GameObject> rootObjects = new ArrayList<>();
    private final Map<String, GameObject> objectsByTag = new HashMap<>();

    // Systems
    private RenderSystem renderSystem;
    private PhysicsSystem physicsSystem;

    // Main camera
    private Camera mainCamera;

    // Lights
    private final List<Light> lights = new ArrayList<>();

    // Lifecycle
    private boolean initialized = false;
    private boolean active = false;

    // Scene properties
    private float ambientLightR = 0.1f;
    private float ambientLightG = 0.1f;
    private float ambientLightB = 0.1f;

    /**
     * Create a new scene
     */
    public Scene(String name) {
        this.name = name;
    }

    /**
     * Initialize the scene
     */
    public void init(RenderSystem renderSystem, PhysicsSystem physicsSystem) {
        if (initialized) return;

        this.renderSystem = renderSystem;
        this.physicsSystem = physicsSystem;

        // Initialize all root objects
        for (GameObject object : rootObjects) {
            object.init();
        }

        // Set ambient light
        if (renderSystem != null) {
            renderSystem.setAmbientLight(ambientLightR, ambientLightG, ambientLightB);
        }

        initialized = true;
    }

    /**
     * Update the scene
     */
    public void update(float deltaTime) {
        if (!active) return;

        // Update all root objects (they will cascade updates to children)
        for (GameObject object : new ArrayList<>(rootObjects)) {
            if (!object.isDestroyed()) {
                object.update(deltaTime);
            }
        }

        // Perform late updates
        for (GameObject object : new ArrayList<>(rootObjects)) {
            if (!object.isDestroyed()) {
                object.lateUpdate(deltaTime);
            }
        }
    }

    /**
     * Render the scene
     */
    public void render() {
        if (!active || renderSystem == null) return;

        // Update camera matrices
        if (mainCamera != null) {
            renderSystem.updateCamera();

            // Add scene lights to render system
            renderSystem.clearLights();
            for (Light light : lights) {
                renderSystem.addLight(light);
            }

            // Render all objects
            for (GameObject object : rootObjects) {
                if (!object.isDestroyed() && object.isActive()) {
                    renderSystem.submitGameObject(object);
                }
            }

            // Process render queue
            renderSystem.render();
        }
    }

    /**
     * Clean up scene resources
     */
    public void cleanup() {
        // Destroy all game objects
        for (GameObject object : new ArrayList<>(rootObjects)) {
            object.destroy();
        }

        rootObjects.clear();
        objectsByTag.clear();
        lights.clear();
    }

    /**
     * Add a game object to the scene
     */
    public void addGameObject(GameObject gameObject) {
        if (gameObject == null) return;

        // Add to root objects if it has no parent
        if (gameObject.getParent() == null) {
            rootObjects.add(gameObject);
        }

        // Index by tag if it has one
        if (gameObject.getTag() != null && !gameObject.getTag().isEmpty()) {
            objectsByTag.put(gameObject.getTag(), gameObject);
        }

        // Initialize if the scene is already initialized
        if (initialized) {
            gameObject.init();
        }
    }

    /**
     * Remove a game object from the scene
     */
    public void removeGameObject(GameObject gameObject) {
        if (gameObject == null) return;

        // Remove from root objects
        rootObjects.remove(gameObject);

        // Remove from tag index
        if (gameObject.getTag() != null && !gameObject.getTag().isEmpty()) {
            objectsByTag.remove(gameObject.getTag());
        }
    }

    /**
     * Find a game object by tag
     */
    public GameObject findGameObjectByTag(String tag) {
        return objectsByTag.get(tag);
    }

    /**
     * Find all game objects with a specific tag
     */
    public List<GameObject> findGameObjectsByTag(String tag) {
        List<GameObject> result = new ArrayList<>();
        if (tag == null || tag.isEmpty()) return result;

        // Search recursively through all game objects
        forEachGameObject(obj -> {
            if (tag.equals(obj.getTag())) {
                result.add(obj);
            }
        });

        return result;
    }

    /**
     * Find all game objects of a specific type
     */
    public <T extends GameObject> List<T> findGameObjectsOfType(Class<T> type) {
        List<T> result = new ArrayList<>();

        // Search recursively through all game objects
        forEachGameObject(obj -> {
            if (type.isInstance(obj)) {
                result.add(type.cast(obj));
            }
        });

        return result;
    }

    /**
     * Execute a function on each game object in the scene
     */
    public void forEachGameObject(Consumer<GameObject> action) {
        // Create a flattened list of all game objects in the scene
        List<GameObject> allObjects = new ArrayList<>();

        // Helper function to recursively collect all objects
        addGameObjectsRecursively(rootObjects, allObjects);

        // Apply the action to each object
        for (GameObject obj : allObjects) {
            action.accept(obj);
        }
    }

    /**
     * Helper method to recursively collect all game objects
     */
    private void addGameObjectsRecursively(List<GameObject> source, List<GameObject> target) {
        for (GameObject obj : source) {
            target.add(obj);
            addGameObjectsRecursively(obj.getChildren(), target);
        }
    }

    /**
     * Set the main camera for this scene
     */
    public void setMainCamera(Camera camera) {
        this.mainCamera = camera;

        if (renderSystem != null) {
            renderSystem.setCamera(camera);
        }
    }

    /**
     * Get the main camera for this scene
     */
    public Camera getMainCamera() {
        return mainCamera;
    }

    /**
     * Add a light to the scene
     */
    public void addLight(Light light) {
        if (light != null && !lights.contains(light)) {
            lights.add(light);
        }
    }

    /**
     * Remove a light from the scene
     */
    public void removeLight(Light light) {
        lights.remove(light);
    }

    /**
     * Set the ambient light color
     */
    public void setAmbientLight(float r, float g, float b) {
        this.ambientLightR = r;
        this.ambientLightG = g;
        this.ambientLightB = b;

        if (renderSystem != null) {
            renderSystem.setAmbientLight(r, g, b);
        }
    }

    /**
     * Set scene activation state
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Check if the scene is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Get the scene name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the render system
     */
    public RenderSystem getRenderSystem() {
        return renderSystem;
    }

    /**
     * Get the physics system
     */
    public PhysicsSystem getPhysicsSystem() {
        return physicsSystem;
    }
}