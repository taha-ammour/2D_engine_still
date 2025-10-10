// src/main/java/org/example/core/scenes/PlayScene.java
package org.example.core.scenes;

import org.example.ecs.GameObject;
import org.example.ecs.components.*;
import org.example.engine.input.Input;
import org.example.engine.input.InputManager;
import org.example.gfx.Camera2D;
import org.example.gfx.Renderer2D;
import org.example.gfx.Texture;
import org.example.gfx.TextureAtlas;
import org.example.game.MarioController;
import org.example.physics.*;
import org.joml.Vector2f;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * PlayScene with ROBUST Collision System
 * Fixed corner cases and multi-collision handling
 */
public final class PlayScene extends BaseScene {
    private final List<GameObject> gameObjects = new ArrayList<>();
    private GameObject marioObject;
    private MarioController marioController;
    private InputManager inputManager;

    private final CollisionSystem collisionSystem = new CollisionSystem();

    // Debug renderer (optional)
    private CollisionDebugRenderer debugRenderer;

    public PlayScene(Input input, Renderer2D renderer, Camera2D camera) {
        super(input, renderer, camera);
    }

    @Override
    protected void onLoad() {
        createMario();
        createGround();
        createPlatforms();
        createTestLevel();  // Additional test structures
        camera.setPosition(0, 0);

        // Initialize debug renderer
        debugRenderer = new CollisionDebugRenderer(renderer);

        inputManager = new InputManager(input);

        // Movement keys
        inputManager.bindKey(GLFW_KEY_RIGHT, dt -> marioController.moveRight());
        inputManager.bindKey(GLFW_KEY_LEFT, dt -> marioController.moveLeft());
        inputManager.bindKey(GLFW_KEY_D, dt -> marioController.moveRight());
        inputManager.bindKey(GLFW_KEY_A, dt -> marioController.moveLeft());

        // Jump keys
        inputManager.bindKeyPress(GLFW_KEY_SPACE, dt -> marioController.jump());
        inputManager.bindKeyPress(GLFW_KEY_W, dt -> marioController.jump());
        inputManager.bindKeyPress(GLFW_KEY_UP, dt -> marioController.jump());

        // Dash keys
        inputManager.bindKeyPress(GLFW_KEY_LEFT_SHIFT, dt -> marioController.dash());
        inputManager.bindKeyPress(GLFW_KEY_X, dt -> marioController.dash());
        inputManager.bindKeyPress(GLFW_KEY_C, dt -> marioController.dash());

        // Debug key
        inputManager.bindKeyPress(GLFW_KEY_F3, dt -> debugRenderer.toggle());
    }

    private void createMario() {
        marioObject = new GameObject("Mario");

        // Transform
        Transform transform = new Transform(100, 100);
        marioObject.addComponent(transform);

        // Physics
        RigidBody rigidBody = new RigidBody();
        rigidBody.useGravity = true;
        rigidBody.drag = 0.0f;
        marioObject.addComponent(rigidBody);

        // Collider - slightly smaller than sprite
        BoxCollider collider = new BoxCollider(28, 44, CollisionLayer.PLAYER);
        collider.offset.set(0, 2);
        marioObject.addComponent(collider);
        collisionSystem.register(collider);

        // Sprite
        SpriteRenderer sprite = new SpriteRenderer(renderer);
        sprite.width = 32;
        sprite.height = 48;

        try {
            Texture texture = loadTexture("mario_atlas.png");
            if (texture != null) {
                TextureAtlas atlas = new TextureAtlas(texture, 4, 2);
                sprite.setAtlas(atlas);
                System.out.println("✅ Mario texture loaded successfully!");
            } else {
                System.err.println("⚠️  Mario texture not found - using default rendering");
            }
        } catch (Exception e) {
            System.err.println("Failed to load Mario texture: " + e.getMessage());
        }

        marioObject.addComponent(sprite);

        // Animator
        Animator animator = new Animator();
        animator.addClip("idle", AnimationClip.create(0, 1, 1f));    // Frame 0
        animator.addClip("run", AnimationClip.create(1, 3, 10f));    // Frames 1-3 ✅
        animator.addClip("jump", AnimationClip.create(4, 1, 1f));    // Frame 4
        marioObject.addComponent(animator);

        // Controller - ✅ NOW WITH COLLISION SYSTEM REFERENCE
        marioController = new MarioController();
        marioController.setCollisionSystem(collisionSystem);
        marioObject.addComponent(marioController);

        gameObjects.add(marioObject);
    }

    private Texture loadTexture(String filename) {
        // List of paths to try (in order)
        String[] pathsToTry = {
                "assets/" + filename,                    // From project root
                "src/main/resources/assets/" + filename,        // Maven/Gradle resources
                "../assets/" + filename,                 // One level up
                "../../assets/" + filename,              // Two levels up
                filename                                 // Current directory
        };

        System.out.println("\n🔍 Searching for texture: " + filename);
        System.out.println("Working directory: " + System.getProperty("user.dir"));

        for (String pathStr : pathsToTry) {
            try {
                Path path = Path.of(pathStr);
                System.out.println("  Trying: " + path.toAbsolutePath());

                if (Files.exists(path)) {
                    System.out.println("  ✅ Found at: " + path.toAbsolutePath());
                    return Texture.load(path.toString());
                }
            } catch (Exception e) {
                System.err.println("  ❌ Error loading from " + pathStr + ": " + e.getMessage());
            }
        }

        // Try loading from classpath/resources
        try {
            System.out.println("  Trying classpath resources...");
            var resource = getClass().getClassLoader().getResource(filename);
            if (resource != null) {
                System.out.println("  ✅ Found in classpath: " + resource);
                return Texture.load(resource.getPath());
            }
        } catch (Exception e) {
            System.err.println("  ❌ Error loading from classpath: " + e.getMessage());
        }

        System.err.println("  ❌ Texture not found in any location!");
        System.err.println("\n💡 To fix this:");
        System.err.println("  1. Create an 'assets' folder in your project root");
        System.err.println("  2. Place " + filename + " in the assets folder");
        System.err.println("  3. Or use a solid color texture for testing");

        return null;
    }

    private void createGround() {
        int tileSize = 64;
        int groundHeight = 32;
        int numTiles = 50;

        for (int i = 0; i < numTiles; i++) {
            GameObject groundTile = new GameObject("Ground_" + i);

            Transform transform = new Transform(i * tileSize, 0);
            groundTile.addComponent(transform);

            BoxCollider collider = new BoxCollider(tileSize, groundHeight, CollisionLayer.GROUND);
            collider.isStatic = true;
            groundTile.addComponent(collider);
            collisionSystem.register(collider);

            SpriteRenderer sprite = new SpriteRenderer(renderer);
            sprite.width = tileSize;
            sprite.height = groundHeight;
            sprite.setTint(0.3f, 0.6f, 0.3f, 1.0f);
            groundTile.addComponent(sprite);

            gameObjects.add(groundTile);
        }

        // Reference markers
        for (int i = 0; i < numTiles; i += 5) {
            GameObject marker = new GameObject("Marker_" + i);

            Transform transform = new Transform(i * tileSize, groundHeight);
            marker.addComponent(transform);

            SpriteRenderer sprite = new SpriteRenderer(renderer);
            sprite.width = 16;
            sprite.height = 64;
            sprite.setTint(1.0f, 1.0f, 0.0f, 1.0f);
            marker.addComponent(sprite);

            gameObjects.add(marker);
        }
    }

    private void createPlatforms() {
        createPlatform(400, 150, 3);
        createPlatform(700, 250, 4);
        createPlatform(1100, 180, 2);
        createPlatform(1400, 300, 3);
    }

    /**
     * Create test structures to verify collision fixes
     */
    private void createTestLevel() {
        // Narrow corridor (tests wall sliding)
        createWall(500, 32, 5);
        createWall(500 + 96, 32, 5);

        // Corner test (tests corner resolution)
        createWall(800, 32, 3);
        createPlatform(800, 224, 3);

        // Staircase (tests multiple ground contacts)
        for (int i = 0; i < 5; i++) {
            createPlatform(1600 + i * 64, 32 + i * 64, 1);
        }
    }

    private void createPlatform(float x, float y, int tiles) {
        int tileSize = 64;
        int platformHeight = 32;

        for (int i = 0; i < tiles; i++) {
            GameObject tile = new GameObject("Platform_" + x + "_" + i);

            Transform transform = new Transform(x + i * tileSize, y);
            tile.addComponent(transform);

            BoxCollider collider = new BoxCollider(tileSize, platformHeight, CollisionLayer.PLATFORM);
            collider.isStatic = true;
            tile.addComponent(collider);
            collisionSystem.register(collider);

            SpriteRenderer sprite = new SpriteRenderer(renderer);
            sprite.width = tileSize;
            sprite.height = platformHeight;
            sprite.setTint(0.6f, 0.4f, 0.2f, 1.0f);
            tile.addComponent(sprite);

            gameObjects.add(tile);
        }
    }

    private void createWall(float x, float y, int tiles) {
        int tileSize = 32;
        int wallWidth = 32;

        for (int i = 0; i < tiles; i++) {
            GameObject tile = new GameObject("Wall_" + x + "_" + i);

            Transform transform = new Transform(x, y + i * tileSize);
            tile.addComponent(transform);

            BoxCollider collider = new BoxCollider(wallWidth, tileSize, CollisionLayer.GROUND);
            collider.isStatic = true;
            tile.addComponent(collider);
            collisionSystem.register(collider);

            SpriteRenderer sprite = new SpriteRenderer(renderer);
            sprite.width = wallWidth;
            sprite.height = tileSize;
            sprite.setTint(0.5f, 0.5f, 0.5f, 1.0f);
            tile.addComponent(sprite);

            gameObjects.add(tile);
        }
    }

    @Override
    public void handleInput() {
        input.update();

        if (input.isKeyReleased(GLFW_KEY_SPACE) ||
                input.isKeyReleased(GLFW_KEY_W) ||
                input.isKeyReleased(GLFW_KEY_UP)) {
            marioController.releaseJump();
        }

        inputManager.processInput(0);
    }

    @Override
    public void update(double dt) {
        // Update all game objects
        for (GameObject obj : gameObjects) {
            obj.update(dt);
        }

        // ✅ Update collision system AFTER all movement
        collisionSystem.update();

        // Smooth camera follow
        Transform marioTransform = marioObject.getComponent(Transform.class);
        if (marioTransform != null) {
            float targetX = marioTransform.position.x - 400;
            float targetY = 0;
            float lerpSpeed = 0.1f;

            Vector2f camPos = camera.getPosition();
            float newX = camPos.x + (targetX - camPos.x) * lerpSpeed;
            float newY = camPos.y + (targetY - camPos.y) * lerpSpeed;

            camera.setPosition(newX, newY);
        }
    }

    @Override
    public void render() {
        for (GameObject obj : gameObjects) {
            obj.render();
        }

        // Debug collision visualization (press F3 to toggle)
        if (debugRenderer.isEnabled()) {
            debugRenderer.render(collisionSystem.getAllColliders());
        }
    }

    @Override
    protected void onUnload() {
        collisionSystem.clear();
        gameObjects.clear();
        marioObject = null;
        marioController = null;
        inputManager = null;
    }
}