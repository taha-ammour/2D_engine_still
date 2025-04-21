package org.example.game;

import org.example.engine.audio.AudioSystem;
import org.example.engine.core.GameObject;
import org.example.engine.core.Transform;
import org.example.engine.input.InputSystem;
import org.example.engine.particles.ParticleSystem;
import org.example.engine.physics.BoxCollider;
import org.example.engine.physics.CollisionInfo;
import org.example.engine.physics.PhysicsSystem;
import org.example.engine.physics.Rigidbody;
import org.example.engine.rendering.Camera;
import org.example.engine.rendering.RenderSystem;
import org.example.engine.rendering.Sprite;
import org.example.engine.resource.ResourceManager;
import org.example.engine.scene.Scene;
import org.example.engine.scene.SceneManager;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Example 2D platformer game implementation demonstrating engine capabilities.
 */
public class PlatformerGame {
    // Singleton instance
    private static PlatformerGame instance;

    // Game scenes
    private Scene gameScene;
    private Scene menuScene;

    // Game objects
    private GameObject player;
    private GameObject camera;

    // Game state
    private int score = 0;
    private int lives = 3;
    private boolean gamePaused = false;
    private boolean debugMode = false;

    // Systems
    private RenderSystem renderSystem;
    private PhysicsSystem physicsSystem;
    private InputSystem inputSystem;
    private AudioSystem audioSystem;
    private ResourceManager resourceManager;

    /**
     * Get the singleton instance
     */
    public static PlatformerGame getInstance() {
        if (instance == null) {
            instance = new PlatformerGame();
        }
        return instance;
    }

    /**
     * Get debug mode state
     */
    public boolean getDebugMode() {
        return debugMode;
    }

    /**
     * Set debug mode state
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        // Update render system debug mode if available
        if (renderSystem != null) {
            renderSystem.setDebugMode(debugMode);
        }
    }

    /**
     * Initialize the game
     */
    public void init() {
        // Get system references
        renderSystem = RenderSystem.getInstance();
        physicsSystem = PhysicsSystem.getInstance();
        inputSystem = InputSystem.getInstance();
        audioSystem = AudioSystem.getInstance();
        resourceManager = ResourceManager.getInstance();

        // Load resources
        loadResources();

        // Create scenes
        createMenuScene();
        createGameScene();

        // Start with menu scene
        SceneManager.getInstance().loadScene("menu");
    }

    /**
     * Load game resources
     */
    private void loadResources() {
        // Load textures
        resourceManager.loadTexture("player", "/textures/player.png");
        resourceManager.loadTexture("ground", "/textures/ground.png");
        resourceManager.loadTexture("background", "/textures/background.png");
        resourceManager.loadTexture("coin", "/textures/coin.png");
        resourceManager.loadTexture("enemy", "/textures/enemy.png");
        resourceManager.loadTexture("particle", "/textures/particle.png");

        // Load sounds
        resourceManager.loadSound("jump", "/sounds/jump.wav");
        resourceManager.loadSound("coin", "/sounds/coin.wav");
        resourceManager.loadSound("hit", "/sounds/hit.wav");

        // Load music
        audioSystem.playMusic("/music/game_music.ogg", 0.5f, true);
    }

    /**
     * Create the menu scene
     */
    private void createMenuScene() {
        menuScene = new Scene("menu");

        // Set up camera
        GameObject camera = new GameObject("Camera");
        Camera cameraComponent = new Camera(800, 600);
        camera.addComponent(cameraComponent);
        menuScene.setMainCamera(cameraComponent);

        // Create background
        GameObject background = new GameObject("Background");
        Sprite backgroundSprite = new Sprite(resourceManager.getTexture("background"), 800, 600);
        background.addComponent(backgroundSprite);
        background.getTransform().setPosition(0, 0, -1);
        menuScene.addGameObject(background);

        // Create title
        // In a full implementation, you would use proper UI elements here
        GameObject title = new GameObject("Title");
        // Add title components...
        menuScene.addGameObject(title);

        // Create play button
        GameObject playButton = new GameObject("PlayButton");
        // Add button components...
        // Set button click callback to start game
        menuScene.addGameObject(playButton);

        // Initialize the scene
        menuScene.init(renderSystem, physicsSystem);

        // Register scene
        SceneManager.getInstance().registerScene(menuScene);
    }

    /**
     * Create the game scene
     */
    private void createGameScene() {
        gameScene = new Scene("game");

        // Set up camera
        camera = new GameObject("Camera");
        Camera cameraComponent = new Camera(800, 600);
        camera.addComponent(cameraComponent);
        gameScene.setMainCamera(cameraComponent);

        // Create player
        createPlayer();

        // Create level
        createLevel();

        // Create HUD
        createHUD();

        // Set up camera to follow player
        cameraComponent.follow(player.getTransform().getPosition());
        cameraComponent.setFollowSpeed(0.1f);

        // Initialize the scene
        gameScene.init(renderSystem, physicsSystem);

        // Register scene
        SceneManager.getInstance().registerScene(gameScene);
    }

    /**
     * Create the player object
     */
    private void createPlayer() {
        player = new GameObject("Player");

        // Add sprite
        Sprite playerSprite = new Sprite(resourceManager.getTexture("player"), 32, 32);
        player.addComponent(playerSprite);

        // Add physics components
        Rigidbody rb = new Rigidbody();
        rb.setMass(1.0f);
        rb.setDrag(0.1f);
        player.addComponent(rb);

        BoxCollider collider = new BoxCollider(28, 28);
        collider.setOffset(0, 0);

        // Set up collision callbacks
        collider.setOnCollisionEnter(this::onPlayerCollision);

        player.addComponent(collider);

        // Add player controller component
        PlayerController controller = new PlayerController();
        player.addComponent(controller);

        // Add particle system for dust effect
        ParticleSystem dustParticles = new ParticleSystem(100);
        dustParticles.setParticleTexture(resourceManager.getTexture("particle"));
        dustParticles.setEmissionRate(0); // Only emit when jumping/landing
        dustParticles.setStartColor(0.8f, 0.8f, 0.7f, 0.8f);
        dustParticles.setEndColor(0.8f, 0.8f, 0.7f, 0.0f);
        dustParticles.setStartSize(2.0f);
        dustParticles.setEndSize(0.5f);
        dustParticles.setLifetime(0.5f);
        dustParticles.setEmissionShape(ParticleSystem.EmissionShape.RECTANGLE);
        dustParticles.setEmissionBox(28, 4);
        dustParticles.setGravity(0, -1);
        player.addComponent(dustParticles);

        // Position the player
        player.getTransform().setPosition(100, 100, 0);

        // Add to scene
        gameScene.addGameObject(player);
    }

    /**
     * Create level objects
     */
    private void createLevel() {
        // Create ground
        GameObject ground = new GameObject("Ground");
        Sprite groundSprite = new Sprite(resourceManager.getTexture("ground"), 800, 32);
        ground.addComponent(groundSprite);

        BoxCollider groundCollider = new BoxCollider(800, 32);
        ground.addComponent(groundCollider);

        // Position the ground
        ground.getTransform().setPosition(400, 500, 0);

        // Add to scene
        gameScene.addGameObject(ground);

        // Create platforms
        createPlatform(200, 400, 200, 32);
        createPlatform(500, 300, 200, 32);
        createPlatform(300, 200, 200, 32);

        // Create coins
        createCoin(250, 350);
        createCoin(550, 250);
        createCoin(350, 150);

        // Create enemies
        createEnemy(400, 450, 300, 500);
        createEnemy(300, 350, 200, 400);
    }

    /**
     * Create a platform
     */
    private void createPlatform(float x, float y, float width, float height) {
        GameObject platform = new GameObject("Platform");

        Sprite platformSprite = new Sprite(resourceManager.getTexture("ground"), width, height);
        platform.addComponent(platformSprite);

        BoxCollider platformCollider = new BoxCollider(width, height);
        platform.addComponent(platformCollider);

        platform.getTransform().setPosition(x, y, 0);

        gameScene.addGameObject(platform);
    }

    /**
     * Create a coin
     */
    private void createCoin(float x, float y) {
        GameObject coin = new GameObject("Coin");

        Sprite coinSprite = new Sprite(resourceManager.getTexture("coin"), 24, 24);
        coin.addComponent(coinSprite);

        BoxCollider coinCollider = new BoxCollider(24, 24);
        coinCollider.setTrigger(true);
        coin.addComponent(coinCollider);

        // Add coin behavior component
        CoinBehavior behavior = new CoinBehavior();
        coin.addComponent(behavior);

        coin.getTransform().setPosition(x, y, 0);

        gameScene.addGameObject(coin);
    }

    /**
     * Create an enemy
     */
    private void createEnemy(float x, float y, float leftBound, float rightBound) {
        GameObject enemy = new GameObject("Enemy");

        Sprite enemySprite = new Sprite(resourceManager.getTexture("enemy"), 32, 32);
        enemy.addComponent(enemySprite);

        BoxCollider enemyCollider = new BoxCollider(28, 28);
        enemy.addComponent(enemyCollider);

        Rigidbody rb = new Rigidbody();
        rb.setMass(1.0f);
        enemy.addComponent(rb);

        // Add enemy behavior component
        EnemyBehavior behavior = new EnemyBehavior();
        behavior.setPatrolBounds(leftBound, rightBound);
        enemy.addComponent(behavior);

        enemy.getTransform().setPosition(x, y, 0);

        gameScene.addGameObject(enemy);
    }

    /**
     * Create HUD elements
     */
    private void createHUD() {
        // In a full implementation, you would use proper UI elements here
        GameObject hud = new GameObject("HUD");

        // Add HUD components...

        gameScene.addGameObject(hud);
    }

    /**
     * Handle player collisions
     */
    private void onPlayerCollision(CollisionInfo collision) {
        // Check collision with coins
        if (collision.colliderB.getGameObject().getName().startsWith("Coin")) {
            collectCoin(collision.colliderB.getGameObject());
        }

        // Check collision with enemies
        if (collision.colliderB.getGameObject().getName().startsWith("Enemy")) {
            hitEnemy(collision.colliderB.getGameObject());
        }
    }

    /**
     * Handle coin collection
     */
    private void collectCoin(GameObject coin) {
        // Play sound
        audioSystem.playSFX("coin");

        // Increase score
        score += 10;

        // Destroy coin
        coin.destroy();

        // Show particle effect
        GameObject effect = new GameObject("CoinEffect");
        ParticleSystem particles = new ParticleSystem(50);
        particles.setParticleTexture(resourceManager.getTexture("particle"));
        particles.setStartColor(1.0f, 0.8f, 0.0f, 1.0f);
        particles.setEndColor(1.0f, 0.8f, 0.0f, 0.0f);
        particles.setEmissionShape(ParticleSystem.EmissionShape.CIRCLE);
        particles.setEmissionRadius(16);
        particles.setEmissionRate(0);
        particles.setDuration(0.5f);
        particles.setLooping(false);
        particles.emit(20);
        effect.addComponent(particles);

        effect.getTransform().setPosition(
                coin.getTransform().getPosition().x,
                coin.getTransform().getPosition().y,
                coin.getTransform().getPosition().z
        );

        gameScene.addGameObject(effect);
    }

    /**
     * Handle enemy collision
     */
    private void hitEnemy(GameObject enemy) {
        // Check if player is above enemy (jumping on top)
        float playerBottom = player.getTransform().getPosition().y + 16;
        float enemyTop = enemy.getTransform().getPosition().y - 16;

        if (playerBottom < enemyTop + 10) {
            // Player jumped on enemy
            destroyEnemy(enemy);

            // Bounce player up
            Rigidbody rb = player.getComponent(Rigidbody.class);
            if (rb != null) {
                rb.setVelocity(rb.getVelocity().x, -5.0f);
            }
        } else {
            // Player hit enemy from the side
            damagePlayer();
        }
    }

    /**
     * Destroy an enemy
     */
    private void destroyEnemy(GameObject enemy) {
        // Play sound
        audioSystem.playSFX("hit");

        // Increase score
        score += 50;

        // Show particle effect
        GameObject effect = new GameObject("EnemyDeathEffect");
        ParticleSystem particles = new ParticleSystem(50);
        particles.setParticleTexture(resourceManager.getTexture("particle"));
        particles.setStartColor(1.0f, 0.2f, 0.2f, 1.0f);
        particles.setEndColor(1.0f, 0.2f, 0.2f, 0.0f);
        particles.setEmissionShape(ParticleSystem.EmissionShape.CIRCLE);
        particles.setEmissionRadius(16);
        particles.setEmissionRate(0);
        particles.setDuration(0.5f);
        particles.setLooping(false);
        particles.emit(30);
        effect.addComponent(particles);

        effect.getTransform().setPosition(
                enemy.getTransform().getPosition().x,
                enemy.getTransform().getPosition().y,
                enemy.getTransform().getPosition().z
        );

        gameScene.addGameObject(effect);

        // Destroy enemy
        enemy.destroy();
    }

    /**
     * Damage the player
     */
    private void damagePlayer() {
        // Play sound
        audioSystem.playSFX("hit");

        // Decrease lives
        lives--;

        // Check for game over
        if (lives <= 0) {
            gameOver();
        } else {
            // Show damage effect
            // In a full implementation, you would add visual feedback
        }
    }

    /**
     * Handle game over
     */
    private void gameOver() {
        // Switch to menu scene
        SceneManager.getInstance().loadScene("menu");

        // Reset game state
        score = 0;
        lives = 3;
    }

    /**
     * Update the game state
     */
    public void update(float deltaTime) {
        // Handle global input
        handleInput();

        // If paused, don't update game logic
        if (gamePaused) {
            return;
        }

        // Game-specific update logic could go here
    }

    /**
     * Handle input
     */
    private void handleInput() {
        // Pause/unpause
        if (inputSystem.isKeyPressed(GLFW_KEY_ESCAPE)) {
            gamePaused = !gamePaused;
        }

        // Debug commands
        if (inputSystem.isKeyPressed(GLFW_KEY_F1)) {
            // Toggle debug rendering
            debugMode = !debugMode;
            renderSystem.setDebugMode(debugMode);
        }
    }

    /**
     * Public player controller component
     */
    public class PlayerController extends org.example.engine.core.Component {
        private float moveSpeed = 5.0f;
        private float jumpForce = 8.0f;
        private boolean grounded = false;
        private float groundCheckDistance = 0.1f;
        private ParticleSystem dustParticles;

        @Override
        protected void onInit() {
            super.onInit();

            // Get dust particle system
            dustParticles = getGameObject().getComponent(ParticleSystem.class);
        }

        @Override
        protected void onUpdate(float deltaTime) {
            // Get components
            Rigidbody rb = getComponent(Rigidbody.class);
            if (rb == null) return;

            // Check if grounded
            boolean wasGrounded = grounded;
            grounded = checkGrounded();

            // Handle landing
            if (grounded && !wasGrounded) {
                onLand();
            }

            // Handle movement
            handleMovement(rb, deltaTime);

            // Handle jumping
            handleJumping(rb);
        }

        /**
         * Handle player movement
         */
        private void handleMovement(Rigidbody rb, float deltaTime) {
            // Get horizontal input
            boolean left = inputSystem.isKeyDown(GLFW_KEY_A) || inputSystem.isKeyDown(GLFW_KEY_LEFT);
            boolean right = inputSystem.isKeyDown(GLFW_KEY_D) || inputSystem.isKeyDown(GLFW_KEY_RIGHT);

            float moveDir = 0;
            if (left) moveDir -= 1;
            if (right) moveDir += 1;

            // Apply movement
            Vector2f velocity = rb.getVelocity();
            velocity.x = moveDir * moveSpeed;
            rb.setVelocity(velocity);

            // Update sprite facing direction
            if (moveDir != 0) {
                Sprite sprite = getComponent(Sprite.class);
                if (sprite != null) {
                    sprite.setFlipX(moveDir < 0);
                }

                // Emit dust particles when moving on ground
                if (grounded && dustParticles != null) {
                    dustParticles.setEmissionRate(10);
                } else if (dustParticles != null) {
                    dustParticles.setEmissionRate(0);
                }
            } else if (dustParticles != null) {
                dustParticles.setEmissionRate(0);
            }
        }

        /**
         * Handle player jumping
         */
        private void handleJumping(Rigidbody rb) {
            // Jump when space is pressed and player is grounded
            if (inputSystem.isKeyPressed(GLFW_KEY_SPACE) && grounded) {
                Vector2f velocity = rb.getVelocity();
                velocity.y = -jumpForce;
                rb.setVelocity(velocity);

                // Play jump sound
                audioSystem.playSFX("jump");

                // Show jump effect
                if (dustParticles != null) {
                    dustParticles.emit(10);
                }

                grounded = false;
            }
        }

        /**
         * Check if player is grounded
         */
        private boolean checkGrounded() {
            // Use raycasting to check if player is on ground
            Vector3f position = getGameObject().getTransform().getPosition();
            Vector2f rayStart = new Vector2f(position.x, position.y + 16);
            Vector2f rayDir = new Vector2f(0, 1);

            CollisionInfo hit = PhysicsSystem.getInstance().raycast(rayStart, rayDir, groundCheckDistance, 0xFFFFFFFF);
            return hit != null;
        }

        /**
         * Handle landing event
         */
        private void onLand() {
            // Show landing effect
            if (dustParticles != null) {
                dustParticles.emit(15);
            }
        }
    }

    /**
     * Inner class for coin behavior component
     */
    private class CoinBehavior extends org.example.engine.core.Component {
        private float rotationSpeed = 90.0f; // degrees per second
        private float bobHeight = 0.5f;
        private float bobSpeed = 2.0f;
        private float initialY;
        private float time = 0;

        @Override
        protected void onInit() {
            super.onInit();
            initialY = getGameObject().getTransform().getPosition().y;
        }

        @Override
        protected void onUpdate(float deltaTime) {
            // Update time
            time += deltaTime;

            // Rotate the coin
            Transform transform = getGameObject().getTransform();
            transform.setRotation(transform.getRotation() + (float)Math.toRadians(rotationSpeed * deltaTime));

            // Bob up and down
            float newY = initialY + (float)Math.sin(time * bobSpeed) * bobHeight;
            Vector3f position = transform.getPosition();
            transform.setPosition(position.x, newY, position.z);
        }
    }

    /**
     * Inner class for enemy behavior component
     */
    private class EnemyBehavior extends org.example.engine.core.Component {
        private float moveSpeed = 2.0f;
        private float leftBound = 0;
        private float rightBound = 0;
        private int direction = 1;

        @Override
        protected void onUpdate(float deltaTime) {
            // Get components
            Rigidbody rb = getComponent(Rigidbody.class);
            if (rb == null) return;

            // Get current position
            Vector3f position = getGameObject().getTransform().getPosition();

            // Check if we need to change direction
            if (position.x > rightBound) {
                direction = -1;
            } else if (position.x < leftBound) {
                direction = 1;
            }

            // Apply movement
            rb.setVelocity(direction * moveSpeed, rb.getVelocity().y);

            // Update sprite facing direction
            Sprite sprite = getComponent(Sprite.class);
            if (sprite != null) {
                sprite.setFlipX(direction < 0);
            }
        }

        /**
         * Set patrol bounds
         */
        public void setPatrolBounds(float left, float right) {
            this.leftBound = left;
            this.rightBound = right;
        }
    }
}