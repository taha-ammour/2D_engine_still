package org.example;

import org.example.engine.Engine;
import org.example.engine.EntityRegistry;
import org.example.engine.SpriteManager;
import org.example.engine.core.GameObject;
import org.example.engine.rendering.Light;
import org.example.engine.rendering.RenderSystem;
import org.example.engine.rendering.ShaderManager;
import org.example.engine.rendering.Sprite;
import org.example.engine.resource.ResourceManager;
import org.example.engine.scene.Scene;
import org.example.engine.scene.SceneManager;

public class Main {

    public static void main(String[] args) {
        // Initialize engine with verbose logging
        System.out.println("START: Engine initialization");
        Engine engine = new Engine();
        engine.init(800, 600, "My Game");

        // Check shader initialization
        ShaderManager shaderManager = ShaderManager.getInstance();
        System.out.println("Shader Manager initialized: " + (shaderManager != null));

        // Create a scene
        Scene scene = new Scene("main");

        // Set ambient light (bright for visibility)
        scene.setAmbientLight(0.8f, 0.8f, 0.8f);

        // Add a directional light to illuminate the scene
        Light light = Light.createDirectionalLight(
                new org.joml.Vector3f(0.0f, 0.0f, -1.0f), // direction
                new org.joml.Vector3f(1.0f, 1.0f, 1.0f),  // color (white)
                1.0f                                      // intensity
        );
        scene.addLight(light);
        System.out.println("Scene and lighting set up");

        // Initialize sprite manager and register entities
        SpriteManager spriteManager = SpriteManager.getInstance();
        System.out.println("SpriteManager initialized: " + (spriteManager != null));

        try {
            assert spriteManager != null;
            EntityRegistry.registerEntities(spriteManager);
            System.out.println("Entities registered successfully");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to register entities: " + e.getMessage());
            e.printStackTrace();
        }

        // Create a game object
        GameObject player = new GameObject("Player");
        player.setPosition(400, 300, 0);

        try {
            // Create sprite with detailed error reporting
            Sprite playerSprite = spriteManager.createSprite("player_sprite_d");
            if (playerSprite != null) {
                System.out.println("Player sprite created successfully");
                System.out.println("Sprite dimensions: " + playerSprite.getWidth() + "x" + playerSprite.getHeight());
                System.out.println("Texture ID: " + (playerSprite.getTexture() != null ? playerSprite.getTexture().getId() : "null"));
                player.addComponent(playerSprite);
            } else {
                System.err.println("ERROR: Failed to create sprite - returned null");
            }
        } catch (Exception e) {
            System.err.println("ERROR: Exception creating sprite: " + e.getMessage());
            e.printStackTrace();
        }

        // Add to scene
        scene.addGameObject(player);

        // Register and load scene
        SceneManager.getInstance().registerScene(scene);
        SceneManager.getInstance().loadScene("main");

        // Set clear color to a distinctive color to verify rendering is happening
        engine.setClearColor(0.4f, 0.1f, 0.4f);

        // Final verification before running
        RenderSystem renderSystem = RenderSystem.getInstance();
        System.out.println("RenderSystem initialized: " + (renderSystem != null));
        System.out.println("Camera set up: " + (scene.getMainCamera() != null));

        // Start the game loop
        System.out.println("Starting game loop...");
        engine.run();
    }
}