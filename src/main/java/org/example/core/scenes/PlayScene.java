// src/main/java/org/example/core/scenes/PlayScene.java
package org.example.core.scenes;

import org.example.ecs.GameObject;
import org.example.ecs.components.*;
import org.example.engine.input.Input;
import org.example.engine.input.InputManager;
import org.example.game.blocks.BlockFactory;
import org.example.game.blocks.BlockSpawner;
import org.example.game.blocks.BlockSpawnListener;
import org.example.game.blocks.effects.BlockEffects;
import org.example.gfx.*;
import org.example.game.MarioController;
import org.example.physics.*;
import org.joml.Vector2f;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * PlayScene with FIXED Coordinate System and Collision
 *
 * KEY FIXES:
 * 1. Ground is now at Y=32 (above 0) so objects don't fall into negative space
 * 2. Mario starts at Y=200 (well above ground)
 * 3. Collision detection includes better bounds checking
 */
public final class PlayScene extends BaseScene {
    private final List<GameObject> gameObjects = new ArrayList<>();
    private GameObject marioObject;
    private MarioController marioController;
    private InputManager inputManager;

    private final CollisionSystem collisionSystem = new CollisionSystem();
    private CollisionDebugRenderer debugRenderer;

    // World bounds (prevent objects from falling forever)
    private static final float WORLD_FLOOR = -100f;

    private BlockFactory blockFactory;
    private BlockSpawner blockSpawner;

    public PlayScene(Input input, Renderer2D renderer, Camera2D camera) {
        super(input, renderer, camera);
    }

    @Override
    protected void onLoad() {

        blockFactory = new BlockFactory(renderer, collisionSystem);
        blockSpawner = new BlockSpawner(blockFactory);

        // Add spawn listener for debugging
        blockSpawner.addListener(new BlockSpawnListener() {
            @Override
            public void onBlockSpawned(GameObject block) {
                gameObjects.add(block);
                System.out.println("✅ Block spawned: " + block.getName());
            }

            @Override
            public void onBlockDespawned(GameObject block) {
                gameObjects.remove(block);
                System.out.println("🗑️ Block despawned: " + block.getName());
            }
        });

        createGround();        // Create ground FIRST
        createPlatforms();     // Then platforms
        createBlocks();        // NEW: Create blocks
        createTestLevel();     // Then test structures
        createMario();         // Mario LAST so it spawns above everything

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
        inputManager.bindKeyPress(GLFW_KEY_B, dt -> spawnBlockAtMario());

        // Debug key
        inputManager.bindKeyPress(GLFW_KEY_F3, dt -> debugRenderer.toggle());

        System.out.println("✅ PlayScene loaded successfully!");
        System.out.println("📊 Total GameObjects: " + gameObjects.size());
        System.out.println("🎮 Controls: Arrow Keys/WASD=Move, Space/W/Up=Jump, Shift/X/C=Dash, F3=Debug");
    }

    private void spawnBlockAtMario() {
        Transform marioTransform = marioObject.getComponent(Transform.class);
        if (marioTransform != null) {
            // Spawn random block type above Mario
            String[] templates = {"lucky_coin", "mystery", "poison", "lucky_powerup"};
            String template = templates[(int)(Math.random() * templates.length)];

            blockSpawner.spawn(template,
                    marioTransform.position.x,
                    marioTransform.position.y + 100);

            System.out.println("🎲 Spawned " + template + " block!");
        }
    }

    private void createBlocks() {
        // ===== SIMPLE BLOCKS =====

        // Lucky blocks with coins
        blockSpawner.spawn("lucky_coin", 300, 200);
        blockSpawner.spawn("lucky_coin", 400, 200);
        blockSpawner.spawn("lucky_coin", 500, 200);

        // Lucky block with power-up
        blockSpawner.spawn("lucky_powerup", 600, 250);

        // Poison blocks
        blockSpawner.spawn("poison", 800, 150);
        blockSpawner.spawn("poison", 900, 150);

        // Mystery blocks (random effects)
        blockSpawner.spawn("mystery", 1100, 200);
        blockSpawner.spawn("mystery", 1200, 200);

        // ===== ADVANCED BLOCKS WITH DECORATORS =====

        // Double effect lucky block (gives 2x power-up!)
        GameObject doubleBlock = blockFactory.createDoubleEffectLuckyBlock(700, 300);
        blockSpawner.spawnCustom(700, 300, builder ->
                builder.lucky()
                        .effect(BlockEffects.POWER_UP)
                        .doubleEffect()
                        .shader("effects", ShaderEffect.PULSATE, ShaderEffect.OUTLINE)
        );

        // Animated poison block with wobble
        blockSpawner.spawnCustom(1000, 180, builder ->
                builder.poison()
                        .animated("poison_idle", "poison_hit")
                        .shader("effects", ShaderEffect.WOBBLE, ShaderEffect.PULSATE)
        );

        // Super bouncy block (2x bounce!)
        blockSpawner.spawnCustom(1300, 100, builder ->
                builder.bouncy()
                        .doubleEffect()
                        .shader("effects", ShaderEffect.OUTLINE)
        );

        // ===== BATCH SPAWNING =====

        // Grid of coin blocks
        blockSpawner.spawnGrid("lucky_coin", 1500, 200, 3, 2, 80);

        // Line of mystery blocks
        blockSpawner.spawnLine("mystery", 1800, 150, 2100, 250, 5);

        // ===== CUSTOM BLOCKS =====

        // Create a custom multi-effect block
        blockSpawner.spawnCustom(2200, 200, builder ->
                builder.lucky()
                        .effect(BlockEffects.INVINCIBILITY
                                .andThen(BlockEffects.POWER_UP)
                                .andThen(BlockEffects.BOUNCE))
                        .doubleEffect()
                        .animated("lucky_idle", "lucky_hit")
                        .shader("effects",
                                ShaderEffect.FLASH,
                                ShaderEffect.OUTLINE,
                                ShaderEffect.PULSATE)
        );

        // Coin block with 10 coins
        GameObject coinBlock = blockFactory.createCoinBlock(2400, 180, 10);
        gameObjects.add(coinBlock);

        System.out.println("✅ Blocks created: " + blockSpawner.getActiveBlocks().size());
    }

    private void createMario() {
        marioObject = new GameObject("Mario");

        // Transform - Start at Y=200 (well above ground at Y=32)
        Transform transform = new Transform(100, 200);
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
                System.out.println("✅ Mario texture loaded!");
            } else {
                System.out.println("ℹ️  Using default Mario rendering (no texture)");
                sprite.setTint(1.0f, 0.2f, 0.2f, 1.0f); // Red color for visibility
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to load Mario texture: " + e.getMessage());
            sprite.setTint(1.0f, 0.2f, 0.2f, 1.0f); // Red color for visibility
        }

        marioObject.addComponent(sprite);

        // Animator
        Animator animator = new Animator();
        animator.addClip("idle", AnimationClip.create(0, 1, 1f));
        animator.addClip("run", AnimationClip.create(1, 3, 10f));
        animator.addClip("jump", AnimationClip.create(4, 1, 1f));
        marioObject.addComponent(animator);

        // Controller - with collision system reference
        marioController = new MarioController();
        marioController.setCollisionSystem(collisionSystem);
        marioObject.addComponent(marioController);

        gameObjects.add(marioObject);
        System.out.println("✅ Mario created at position: (100, 200)");
    }

    private Texture loadTexture(String filename) {
        String[] pathsToTry = {
                "assets/" + filename,
                "src/main/resources/assets/" + filename,
                "../assets/" + filename,
                "../../assets/" + filename,
                filename
        };

        for (String pathStr : pathsToTry) {
            try {
                Path path = Path.of(pathStr);
                if (Files.exists(path)) {
                    return Texture.load(path.toString());
                }
            } catch (Exception e) {
                // Continue to next path
            }
        }

        // Try classpath
        try {
            var resource = getClass().getClassLoader().getResource(filename);
            if (resource != null) {
                return Texture.load(resource.getPath());
            }
        } catch (Exception e) {
            // Texture not found
        }

        return null;
    }

    private void createGround() {
        int tileSize = 64;
        int groundHeight = 32;
        int numTiles = 50;
        float groundY = 32f; // ✅ FIXED: Ground is now at Y=32, not Y=0

        for (int i = 0; i < numTiles; i++) {
            GameObject groundTile = new GameObject("Ground_" + i);

            Transform transform = new Transform(i * tileSize, groundY);
            groundTile.addComponent(transform);

            BoxCollider collider = new BoxCollider(tileSize, groundHeight, CollisionLayer.GROUND);
            collider.isStatic = true;
            groundTile.addComponent(collider);
            collisionSystem.register(collider);

            SpriteRenderer sprite = new SpriteRenderer(renderer);
            sprite.width = tileSize;
            sprite.height = groundHeight;
            sprite.setTint(0.3f, 0.6f, 0.3f, 1.0f); // Green ground
            groundTile.addComponent(sprite);

            gameObjects.add(groundTile);
        }

        // Reference markers every 5 tiles
        for (int i = 0; i < numTiles; i += 5) {
            GameObject marker = new GameObject("Marker_" + i);

            Transform transform = new Transform(i * tileSize, groundY + groundHeight);
            marker.addComponent(transform);

            SpriteRenderer sprite = new SpriteRenderer(renderer);
            sprite.width = 16;
            sprite.height = 64;
            sprite.setTint(1.0f, 1.0f, 0.0f, 1.0f); // Yellow markers
            marker.addComponent(sprite);

            gameObjects.add(marker);
        }

        System.out.println("✅ Ground created: " + numTiles + " tiles at Y=" + groundY);
    }

    private void createPlatforms() {
        // Platforms at various heights above ground
        createPlatform(400, 150, 3);
        createPlatform(700, 250, 4);
        createPlatform(1100, 180, 2);
        createPlatform(1400, 300, 3);

        System.out.println("✅ Platforms created");
    }

    private void createTestLevel() {
        // Narrow corridor (tests wall sliding)
        createWall(500, 64, 5);
        createWall(596, 64, 5); // 500 + 96

        // Corner test
        createWall(800, 64, 3);
        createPlatform(800, 224, 3);

        // Staircase
        for (int i = 0; i < 5; i++) {
            createPlatform(1600 + i * 64, 64 + i * 64, 1);
        }

        System.out.println("✅ Test structures created");
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
            sprite.setTint(0.6f, 0.4f, 0.2f, 1.0f); // Brown platforms
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
            sprite.setTint(0.5f, 0.5f, 0.5f, 1.0f); // Gray walls
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

        // Safety: Prevent falling through world bounds
        Transform marioTransform = marioObject.getComponent(Transform.class);
        if (marioTransform != null && marioTransform.position.y < WORLD_FLOOR) {
            marioTransform.position.y = 200; // Respawn at start height
            marioTransform.position.x = 100;
            RigidBody rb = marioObject.getComponent(RigidBody.class);
            if (rb != null) {
                rb.velocity.set(0, 0);
            }
            System.out.println("⚠️ Mario fell through world - respawned");
        }

        // Smooth camera follow
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

        // Debug collision visualization (F3 to toggle)
        if (debugRenderer.isEnabled()) {
            debugRenderer.render(collisionSystem.getAllColliders());
        }
    }

    @Override
    protected void onUnload() {
        blockSpawner.clear();
        collisionSystem.clear();
        gameObjects.clear();
        marioObject = null;
        marioController = null;
        inputManager = null;
    }
}