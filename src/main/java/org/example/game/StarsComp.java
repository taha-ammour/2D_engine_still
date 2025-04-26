package org.example.game;

import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.rendering.Camera;
import org.example.engine.rendering.Sprite;
import org.example.engine.scene.Scene;
import org.example.engine.scene.SceneManager;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class StarsComp extends Component {

    private int numStars = 100; // Number of stars
    private float minSpeed = 50.0f;  // Minimum star speed
    private float maxSpeed = 150.0f; // Maximum star speed
    private int starSize = 5;       // Size of each star sprite

    // Track stars and their speeds separately
    private final List<GameObject> starObjects = new ArrayList<>();
    private final Map<GameObject, Float> starSpeeds = new HashMap<>();
    private final Random random = new Random();

    private float windowWidth;
    private float windowHeight;
    private boolean initialized = false;

    @Override
    protected void onInit() {
        System.out.println("StarsComp.onInit() called - waiting for first update to initialize stars");
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // Initialize on first update to ensure scene is properly loaded
        if (!initialized) {
            initializeStars();
            initialized = true;
            return;
        }

        // Update camera dimensions in case of resize
        Camera camera = SceneManager.getInstance().getActiveScene().getMainCamera();
        if (camera == null) {
            return; // Cannot update without camera info
        }

        windowWidth = camera.getViewportWidth();
        windowHeight = camera.getViewportHeight();

        // Update each star's position
        for (GameObject starObj : new ArrayList<>(starObjects)) {
            // Skip if star is not active or has been destroyed
            if (starObj == null || !starObj.isActive()) continue;

            // Get the star's speed from our map
            Float speed = starSpeeds.getOrDefault(starObj, 50.0f);

            // Get current position
            Vector3f position = starObj.getTransform().getPosition();

            // Move the star down
            position.y += speed * deltaTime;

            // If star goes below the screen, reset its position to the top
            if (position.y > windowHeight) {
                position.y = -10; // Reset just above the screen
                position.x = random.nextFloat() * windowWidth; // Randomize horizontal position
            }

            // Update star position
            starObj.getTransform().setPosition(position);
        }
    }

    /**
     * Initialize stars on first update when scene is fully loaded
     */
    private void initializeStars() {
        System.out.println("Initializing stars...");

        // Get the current scene
        Scene scene = SceneManager.getInstance().getActiveScene();
        if (scene == null) {
            System.err.println("ERROR: No active scene found for star initialization!");
            return;
        }

        // Get camera for screen dimensions
        Camera camera = scene.getMainCamera();
        if (camera == null) {
            System.err.println("ERROR: No main camera found for star initialization!");
            return;
        }

        // Get screen dimensions
        windowWidth = camera.getViewportWidth();
        windowHeight = camera.getViewportHeight();

        System.out.println("Creating " + numStars + " stars on screen dimensions: " +
                windowWidth + "x" + windowHeight);

        // Create individual star objects
        for (int i = 0; i < numStars; i++) {
            try {
                // First create the GameObject
                GameObject star = new GameObject("Star_" + i);

                // Set initial position
                float x = random.nextFloat() * windowWidth;
                float y = random.nextFloat() * windowHeight; // Distribute across screen
                float z = 0.0f; // Same plane as other objects
                star.setPosition(x, y, z);

                // IMPORTANT: Add to scene first
                scene.addGameObject(star);

                // Generate random speed
                float speed = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);

                // Store in our maps
                starObjects.add(star);
                starSpeeds.put(star, speed);

                // Now add sprite component AFTER adding to scene
                Sprite starSprite = new Sprite(null, starSize, starSize);
                starSprite.setColor(0xFFFFFF, 1.0f); // Bright white, fully opaque
                star.addComponent(starSprite);

                System.out.println("Created star #" + i + " at " + x + ", " + y);
            } catch (Exception e) {
                System.err.println("Error creating star: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Stars initialization complete - created " + starObjects.size() + " stars");
    }

    @Override
    protected void onDestroy() {
        // Clean up
        for (GameObject star : starObjects) {
            if (star != null && !star.isDestroyed()) {
                star.destroy();
            }
        }
        starObjects.clear();
        starSpeeds.clear();
    }
}