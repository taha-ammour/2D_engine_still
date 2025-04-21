package org.example;

import org.example.engine.Engine;
import org.example.engine.EntityRegistry;
import org.example.engine.SpriteManager;
import org.example.engine.core.GameObject;
import org.example.engine.physics.PhysicsSystem;
import org.example.engine.rendering.Camera;
import org.example.engine.rendering.Light;
import org.example.engine.rendering.RenderSystem;
import org.example.engine.rendering.ShaderManager;
import org.example.engine.rendering.Sprite;
import org.example.engine.scene.Scene;
import org.example.engine.scene.SceneManager;
import org.joml.Vector3f;

public class Main {

    public static void main(String[] args) {
        // Initialize engine with verbose logging
        System.out.println("START: Engine initialization");
        Engine engine = new Engine();
        engine.init(800, 600, "My Game");

        // Check shader initialization
        ShaderManager shaderManager = ShaderManager.getInstance();
        System.out.println("Shader Manager initialized: " + (shaderManager != null));

        // Get the scene manager
        SceneManager sceneManager = engine.getSceneManager();

        // Create a scene
        Scene scene = sceneManager.createScene("main");
        System.out.println("Scene created: " + scene.getName());

        // Set ambient light (bright for visibility)
        scene.setAmbientLight(0.8f, 0.8f, 0.8f);

        // Register entities, UI, and tiles in correct order
        SpriteManager spriteManager = SpriteManager.getInstance();
        System.out.println("SpriteManager initialized: " + (spriteManager != null));

        try {
            assert spriteManager != null;
            EntityRegistry.registerEntities(spriteManager);
            EntityRegistry.registerUi(spriteManager);
            EntityRegistry.registerTiles(spriteManager);
            System.out.println("Entities registered successfully");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to register entities: " + e.getMessage());
            e.printStackTrace();
        }

        // CRITICAL FIX 1: Create and set up camera BEFORE adding it to the scene
        GameObject cameraObject = new GameObject("Main Camera");
        cameraObject.setPosition(400, 300, 10); // Set position before adding components
        scene.addGameObject(cameraObject); // Add to scene first

        // Now create and add the camera component
        Camera camera = new Camera(800, 600);
        cameraObject.addComponent(camera); // Add camera component to initialized GameObject
        scene.setMainCamera(camera); // Set as main camera
        System.out.println("Camera created and set as main camera");

        // Add a directional light
        Light light = Light.createDirectionalLight(
                new Vector3f(0.0f, 0.0f, -1.0f), // direction pointing toward scene
                new Vector3f(1.0f, 1.0f, 1.0f),  // color (white)
                1.0f                             // intensity
        );
        scene.addLight(light);
        System.out.println("Light added to scene");

        // CRITICAL FIX 2: Proper GameObject and Component initialization
        // Create player GameObject and add it to scene FIRST
        GameObject player = new GameObject("Player");
        player.setPosition(400, 300, 0);
        scene.addGameObject(player); // Add to scene before adding components

        try {
            // Now create sprite and add to initialized GameObject
            Sprite playerSprite = spriteManager.createSprite("player_sprite_d");
            System.out.println("player sprite created: " + playerSprite);
            if (playerSprite != null) {
                player.addComponent(playerSprite);
                System.out.println("Player sprite added successfully");
            } else {
                System.err.println("ERROR: Failed to create sprite - returned null");

                // Fallback sprite
                Sprite fallbackSprite = new Sprite(null, 50, 50);
                fallbackSprite.setColor(0xFF0000, 1.0f); // Red color
                player.addComponent(fallbackSprite);
                System.out.println("Added fallback sprite to player");
            }
        } catch (Exception e) {
            System.err.println("ERROR: Exception creating sprite: " + e.getMessage());
            e.printStackTrace();

            // Fallback sprite
            Sprite fallbackSprite = new Sprite(null, 50, 50);
            fallbackSprite.setColor(0xFF0000, 1.0f); // Red color
            player.addComponent(fallbackSprite);
            System.out.println("Added fallback sprite to player due to exception");
        }

        // Create test objects with proper initialization
        createTestObject(scene, 300, 300, 0, 0x00FF00); // Green
        createTestObject(scene, 500, 300, 0, 0x0000FF); // Blue
        createTestObject(scene, 400, 200, 0, 0xFFFF00); // Yellow
        createTestObject(scene, 400, 400, 0, 0xFF00FF); // Purple

        // Initialize the scene with correct systems
        // FIX: Use PhysicsSystem.getInstance() instead of trying to get it from SceneManager
        scene.init(RenderSystem.getInstance(), PhysicsSystem.getInstance());
        System.out.println("Scene initialized");

        // Load the scene
        sceneManager.loadScene("main");
        System.out.println("Scene loaded");

        // Set clear color
        engine.setClearColor(0.2f, 0.2f, 0.3f);

        // Start the game loop
        System.out.println("Starting game loop...");
        engine.run();
    }

    // Helper method to create test objects with different colors
    private static void createTestObject(Scene scene, float x, float y, float z, int color) {
        // Create the GameObject and add to scene first
        GameObject obj = new GameObject("TestObject_" + color);
        obj.setPosition(x, y, z);
        scene.addGameObject(obj);

        // Now add component to initialized GameObject
        Sprite sprite = new Sprite(null, 30, 30);
        sprite.setColor(color, 1.0f);
        obj.addComponent(sprite);

        System.out.println("Created test object at (" + x + "," + y + "," + z + ") with color " +
                String.format("0x%06X", color));
    }
}