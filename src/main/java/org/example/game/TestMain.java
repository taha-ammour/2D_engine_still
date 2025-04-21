package org.example.game;

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
import org.lwjgl.opengl.GL11;

public class TestMain {

    public static void main(String[] args) {
        // Enable verbose logging to diagnose issues
        boolean verboseLogging = true;

        // Initialize engine
        System.out.println("=== ENGINE INITIALIZATION ===");
        Engine engine = new Engine();
        engine.init(800, 600, "My Game");

        // Print OpenGL information
        System.out.println("OpenGL Version: " + GL11.glGetString(GL11.GL_VERSION));
        System.out.println("OpenGL Renderer: " + GL11.glGetString(GL11.GL_RENDERER));
        System.out.println("OpenGL Vendor: " + GL11.glGetString(GL11.GL_VENDOR));

        // Get shader manager
        ShaderManager shaderManager = ShaderManager.getInstance();
        System.out.println("ShaderManager initialized: " + (shaderManager != null));

        // Check sprite shader
        ShaderManager.Shader spriteShader = shaderManager.getShader("sprite");
        System.out.println("Sprite shader status: " + (spriteShader != null ?
                "Loaded (ID: " + spriteShader.getProgramId() + ")" :
                "Not loaded"));

        // Create a simple fallback shader if needed
        if (spriteShader == null) {
            System.out.println("Creating fallback shader");
            createSimpleFallbackShader(shaderManager);
            spriteShader = shaderManager.getShader("sprite");
            System.out.println("Fallback shader status: " + (spriteShader != null ?
                    "Created (ID: " + spriteShader.getProgramId() + ")" :
                    "Failed to create"));
        }

        // Get render system and set debug mode
        RenderSystem renderSystem = RenderSystem.getInstance();
        renderSystem.setVerboseLogging(verboseLogging);
        renderSystem.setDebugMode(true);
        System.out.println("RenderSystem initialized and set to debug mode");

        // Create a scene
        Scene scene = engine.getSceneManager().createScene("MainScene");
        System.out.println("Scene created: " + scene.getName());

        // Set full bright ambient lighting for maximum visibility
        scene.setAmbientLight(1.0f, 1.0f, 1.0f);
        System.out.println("Ambient light set to maximum brightness for debugging");

        // Create simple colored boxes for testing
        System.out.println("\n=== CREATING TEST OBJECTS ===");

        // First, create and set up a camera
        GameObject cameraObject = new GameObject("MainCamera");
        cameraObject.setPosition(400, 300, 100); // Position far enough to see everything
        scene.addGameObject(cameraObject);
        System.out.println("Camera object added to scene at position (400, 300, 100)");

        // Now create the camera component and add it to the GameObject
        Camera camera = new Camera(800, 600);
        cameraObject.addComponent(camera);

        // Set this camera as the main camera for the scene
        scene.setMainCamera(camera);
        System.out.println("Camera component created and set as main camera");

        // Create a large red square in the center (impossible to miss)
        Light light = Light.createDirectionalLight(new Vector3f(0,-1,0),new Vector3f(1.0f,1.0f,1.0f),1.0f);
        GameObject redSquare = new GameObject("RedSquare");
        redSquare.setPosition(400, 300, 0);
        scene.addGameObject(redSquare);

        Sprite redSprite = new Sprite(null, 200, 200);
        redSprite.setColor(0xFF0000, 1.0f);
        redSprite.setVerboseLogging(verboseLogging);
        redSquare.addComponent(redSprite);
        System.out.println("Created large red square (200x200) at center of screen");

        // Create smaller squares in each corner with different colors
        createColoredSquare(scene, "GreenSquare", 200, 200, 0, 0x00FF00, 100, 100, verboseLogging);
        createColoredSquare(scene, "BlueSquare", 600, 200, 0, 0x0000FF, 100, 100, verboseLogging);
        createColoredSquare(scene, "YellowSquare", 200, 400, 0, 0xFFFF00, 100, 100, verboseLogging);
        createColoredSquare(scene, "CyanSquare", 600, 400, 0, 0x00FFFF, 100, 100, verboseLogging);

        // Initialize the scene
        scene.init(renderSystem, PhysicsSystem.getInstance());
        System.out.println("Scene initialized");

        // Set scene as active
        SceneManager.getInstance().loadScene("MainScene");
        System.out.println("Scene loaded");

        // Set a clear color that's distinctly different from all test squares
        engine.setClearColor(0.2f, 0.1f, 0.3f); // Dark purple
        System.out.println("Clear color set to dark purple for contrast");

        // Log pre-render state
        System.out.println("\n=== PRE-RENDER STATE ===");
        System.out.println("Active scene: " + (SceneManager.getInstance().getActiveScene() != null ?
                SceneManager.getInstance().getActiveScene().getName() : "None"));
        System.out.println("Main camera: " + (scene.getMainCamera() != null ? "Set" : "Not set"));
        System.out.println("Render system: " + (renderSystem != null ? "Ready" : "Not ready"));
        System.out.println("Camera position: " + camera.getPosition());

        // Start the engine loop
        System.out.println("\n=== STARTING GAME LOOP ===");
        engine.run();
    }

    /**
     * Create a colored square game object
     */
    private static void createColoredSquare(Scene scene, String name, float x, float y, float z,
                                            int color, float width, float height, boolean verboseLogging) {
        // Create GameObject and add to scene FIRST
        GameObject obj = new GameObject(name);
        obj.setPosition(x, y, z);
        scene.addGameObject(obj);

        // Now create the sprite component
        Sprite sprite = new Sprite(null, width, height);
        sprite.setColor(color, 1.0f);
        sprite.setVerboseLogging(verboseLogging);

        // Add the sprite component to the initialized GameObject
        obj.addComponent(sprite);

        System.out.println("Created " + name + " at (" + x + "," + y + "," + z + ") with color " +
                String.format("0x%06X", color) + " and size " + width + "x" + height);
    }

    /**
     * Create a simple fallback shader
     */
    private static void createSimpleFallbackShader(ShaderManager shaderManager) {
        String vertexSource =
                "#version 330 core\n" +
                        "layout (location = 0) in vec3 aPos;\n" +
                        "layout (location = 1) in vec2 aTexCoord;\n" +
                        "uniform mat4 u_MVP;\n" +
                        "out vec2 TexCoord;\n" +
                        "void main() {\n" +
                        "    gl_Position = u_MVP * vec4(aPos, 1.0);\n" +
                        "    TexCoord = aTexCoord;\n" +
                        "}\n";

        String fragmentSource =
                "#version 330 core\n" +
                        "in vec2 TexCoord;\n" +
                        "out vec4 FragColor;\n" +
                        "uniform vec4 u_Color;\n" +
                        "void main() {\n" +
                        "    FragColor = u_Color;\n" +
                        "}\n";

        shaderManager.createDefaultShader("sprite", vertexSource, fragmentSource);
    }
}