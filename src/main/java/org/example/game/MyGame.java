package org.example.game;

import org.example.engine.Engine;
import org.example.engine.EntityRegistry;
import org.example.engine.SpriteManager;
import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.particles.ParticleSystem;
import org.example.engine.physics.BoxCollider;
import org.example.engine.physics.Rigidbody;
import org.example.engine.rendering.Camera;
import org.example.engine.rendering.Sprite;
import org.example.engine.resource.ResourceManager;
import org.example.engine.scene.Scene;
import org.example.engine.scene.SceneManager;
import org.example.game.map.FloorDecorator;
import org.example.game.map.RoomGenerator;
import org.joml.Vector3f;

import java.io.File;

public class MyGame {
    private Engine engine;
    private Scene gameScene;

    private static final int ROOM_WIDTH = 150;  // In tiles
    private static final int ROOM_HEIGHT = 100; // In tiles
    private static final float ROOM_START_X = 100;
    private static final float ROOM_START_Y = 100;

    private void registerSprites() {
        // Get sprite manager
        SpriteManager spriteManager = SpriteManager.getInstance();

        // Load sprite sheets
        EntityRegistry.registerEntities(spriteManager);
        EntityRegistry.registerTiles(spriteManager);
        EntityRegistry.registerUi(spriteManager);


        System.out.println("Sprites registered");
    }

    private void loadSoundResources() {
        try {
            // First check if the directory exists
            File soundDir = new File("src/main/resources/sounds");
            if (soundDir.exists() && soundDir.isDirectory()) {
                System.out.println("Sound directory found at: " + soundDir.getAbsolutePath());
                // List the files in the directory
                File[] files = soundDir.listFiles();
                if (files != null) {
                    System.out.println("Files in sound directory:");
                    for (File file : files) {
                        System.out.println("  - " + file.getName());
                    }
                }
            } else {
                System.out.println("Sound directory not found at: " + soundDir.getAbsolutePath());
            }

            // Load sound effects with better error handling
            try {
                ResourceManager.getInstance().loadSound("pickup", "/sounds/pickup_item.wav");
                System.out.println("Loaded pickup sound");
            } catch (Exception e) {
                System.out.println("Could not load pickup sound: " + e.getMessage());
            }

            try {
                ResourceManager.getInstance().loadSound("drop", "/sounds/drop.wav");
                System.out.println("Loaded drop sound");
            } catch (Exception e) {
                System.out.println("Could not load drop sound: " + e.getMessage());
            }

            try {
                ResourceManager.getInstance().loadSound("hurt", "/sounds/hurt.wav");
                System.out.println("Loaded hurt sound");
            } catch (Exception e) {
                System.out.println("Could not load hurt sound: " + e.getMessage());
            }

            System.out.println("Sound resources loading complete");
        } catch (Exception e) {
            System.out.println("Warning: Error in sound resource loading: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void init() {
        // Initialize engine
        engine = new Engine();
        engine.init(800, 600, "My 2D Game");

        // Create scene
        gameScene = engine.getSceneManager().createScene("GameScene");
        gameScene.setAmbientLight(0.8f, 0.8f, 0.8f);
        engine.getSceneManager().loadScene("GameScene");

        // Set up camera
        createCamera();

        //loadSoundResources();
        registerSprites();

        createRoom();

        // Create player
        createPlayer();

        // Create enemies
        createEnemies();

        // Create collectibles
        createCollectibles();

        createStars();

        createUI();



        engine.setClearColor(0.2f, 0.2f, 0.3f);
        // Load scene
        engine.getSceneManager().loadScene("GameScene");
    }

    private void createRoom() {
        SpriteManager spriteManager = SpriteManager.getInstance();

        // Create and use the room generator
        RoomGenerator roomGenerator = new RoomGenerator(
                gameScene,
                spriteManager,
                ROOM_WIDTH,
                ROOM_HEIGHT,
                ROOM_START_X,
                ROOM_START_Y
        );

        roomGenerator.generateRoom();

        FloorDecorator floorDecorator = new FloorDecorator(
                gameScene,
                spriteManager,
                ROOM_START_X,
                ROOM_START_Y,
                ROOM_WIDTH,
                ROOM_HEIGHT
        );

        floorDecorator.decorateFloor();

    }

    private void createCamera() {
        // Create camera GameObject
        GameObject cameraObj = new GameObject("MainCamera");
        cameraObj.setPosition(400, 300, 0);

        // Add camera component
        Camera camera = new Camera(800, 600);
        cameraObj.addComponent(camera);

        // Add to scene and set as main camera
        gameScene.addGameObject(cameraObj);
        gameScene.setMainCamera(camera);

        // Add our new camera follow component
        CameraFollow cameraFollow = new CameraFollow();
        cameraFollow.setTargetTag("Player"); // Follow GameObject with "Player" tag
        cameraFollow.setFollowSpeed(5.0f);   // Adjust speed as needed
        // You can set offset if you want camera to be shifted relative to player
         cameraFollow.setOffset(-camera.getVirtualViewportWidth()/2, -camera.getViewportHeight()/2); // Example: Camera 100 pixels above player
        cameraObj.addComponent(cameraFollow);
    }


    private void createPlayer() {
        GameObject player = new GameObject("Player");
        player.setTag("Player");

        float roomCenterX = ROOM_START_X + (ROOM_WIDTH * 16 / 2);
        float roomCenterY = ROOM_START_Y + (ROOM_HEIGHT * 16 / 2);

        player.setPosition(roomCenterX, roomCenterY, 0);

        // Add sprite
        SpriteManager spriteManager = SpriteManager.getInstance();
        Sprite playerSprite = spriteManager.createSprite("player_sprite_d");

        player.addComponent(playerSprite);


        // Add physics components
        Rigidbody rb = new Rigidbody();
        player.addComponent(rb);

        BoxCollider collider = new BoxCollider(playerSprite.getWidth()-8, playerSprite.getHeight());
        collider.setOffset(8,0);
        player.addComponent(collider);

        // Add player controller
        PlayerController controller = new PlayerController();
        player.addComponent(controller);
        player.setScale(2,2,2);

        // Add weapon
        PlayerWeapon weapon = new PlayerWeapon();
        player.addComponent(weapon);

        PlayerStats stats = new PlayerStats();
        player.addComponent(stats);

        // Add health
        PlayerHealth health = new PlayerHealth(100);
        player.addComponent(health);

        gameScene.addGameObject(player);


    }

    private void createUI() {
        // Create a UI container object
        GameObject uiContainer = new GameObject("UI_Container");
        uiContainer.setTag("UI"); // Tag for finding it later
        uiContainer.setPosition(0, 0, 10); // High Z to render on top

        // Add UI component
        GameUI gameUI = new GameUI();
        uiContainer.addComponent(gameUI);

        // Add to scene
        gameScene.addGameObject(uiContainer);

        System.out.println("Game UI created");
    }

    private void createEnemies() {

        float roomStartX = ROOM_START_X + 32; // Keep away from walls
        float roomStartY = ROOM_START_Y + 32;
        float roomEndX = ROOM_START_X + ((ROOM_WIDTH-2) * 16);
        float roomEndY = ROOM_START_Y + ((ROOM_HEIGHT-2) * 16);

        // Create some patrol points
        Vector3f[] patrolPoints1 = {
                new Vector3f(roomStartX + 50, roomStartY + 50, 0),
                new Vector3f(roomEndX - 50, roomStartY + 50, 0),
                new Vector3f(roomEndX - 50, roomEndY - 50, 0),
                new Vector3f(roomStartX + 50, roomEndY - 50, 0)
        };

        // Create first enemy
        GameObject enemy1 = new GameObject("Enemy1");
        enemy1.setPosition(roomStartX + 50, roomStartY + 50, 0);

        Sprite enemySprite = new Sprite(null, 16, 16);
        enemySprite.setColor(0xFF0000, 1.0f);
        enemy1.addComponent(enemySprite);

        BoxCollider enemyCollider = new BoxCollider(16, 16);
        enemy1.addComponent(enemyCollider);

        EnemyAI enemyAI = new EnemyAI(patrolPoints1);
        enemy1.addComponent(enemyAI);

        gameScene.addGameObject(enemy1);

    }

    private void createCollectibles() {
        float roomStartX = ROOM_START_X + 32; // Keep away from walls
        float roomStartY = ROOM_START_Y + 32;
        float roomWidth = (ROOM_WIDTH - 2) * 16;
        float roomHeight = (ROOM_HEIGHT - 2) * 16;

        // Get sprite manager
        SpriteManager spriteManager = SpriteManager.getInstance();

        // Create several emerald collectibles around the room
        for (int i = 0; i < 8; i++) {
            // Calculate position within room bounds
            float x = roomStartX + (float)(Math.random() * roomWidth);
            float y = roomStartY + (float)(Math.random() * roomHeight);

            GameObject collectible = new GameObject("Emerald" + i);
            collectible.setPosition(x, y, 0);

            // Use just the first emerald sprite initially - animation will change it
            Sprite collectibleSprite = spriteManager.createSprite("Emerald_id_1");
            Sprite collectibleSprite1 = spriteManager.createSprite("Emerald_id_2");
            Sprite collectibleSprite2 = spriteManager.createSprite("Emerald_id_3");

            if (collectibleSprite != null) {
                collectible.addComponent(collectibleSprite);
                collectible.addComponent(collectibleSprite1);
                collectible.addComponent(collectibleSprite2);

                // FIRST add to scene - IMPORTANT: do this before adding the animator component
                gameScene.addGameObject(collectible);

                // NOW add animated collectible behavior (emerald type)
                AnimatedCollectible behavior = new AnimatedCollectible(true);
                collectible.addComponent(behavior);
            } else {
                System.err.println("Failed to load emerald sprite");
            }
        }

        // Create a few chest collectibles
        for (int i = 0; i < 3; i++) {
            // Calculate position within room bounds
            float x = roomStartX + (float)(Math.random() * roomWidth);
            float y = roomStartY + (float)(Math.random() * roomHeight);

            GameObject chest = new GameObject("SmallChest" + i);
            chest.setPosition(x, y, 0);

            // Use just the first chest sprite initially - animation will change it
            Sprite chestSprite = spriteManager.createSprite("ChestE_id_1");

            if (chestSprite != null) {
                chest.addComponent(chestSprite);

                // FIRST add to scene
                gameScene.addGameObject(chest);

                // THEN add animated collectible behavior
                AnimatedCollectible behavior = new AnimatedCollectible(false);
                chest.addComponent(behavior);
            } else {
                System.err.println("Failed to load chest sprite");
            }
        }

        // Create a special treasure chest near the center of the room
        float roomCenterX = roomStartX + (roomWidth / 2) + 50; // Offset a bit from center
        float roomCenterY = roomStartY + (roomHeight / 2) + 30;

        GameObject treasure = new GameObject("TreasureChest");
        treasure.setPosition(roomCenterX, roomCenterY, 0);

        Sprite treasureSprite = spriteManager.createSprite("ChestH_id_1");
        if (treasureSprite != null) {
            treasure.addComponent(treasureSprite);

            // Scale the treasure chest
            treasure.setScale(2.5f, 2.5f, 1.0f);

            // FIRST add to scene
            gameScene.addGameObject(treasure);

            // THEN add animated collectible behavior
            AnimatedCollectible behavior = new AnimatedCollectible(false);
            treasure.addComponent(behavior);
        } else {
            System.err.println("Failed to load treasure chest sprite");
        }

    }

    private void createStars(){

        GameObject starfieldObject = new GameObject("Starfield");
        starfieldObject.setPosition(0, 0, 0);

        StarsComp starsComp = new StarsComp();
        starfieldObject.addComponent(starsComp);

        gameScene.addGameObject(starfieldObject);
    }

    public void run() {
        engine.run();
    }

    public static void main(String[] args) {
        MyGame game = new MyGame();
        game.init();
        game.run();
    }
}

class PlayerHealth extends Component {
    private int maxHealth;
    private int currentHealth;

    public PlayerHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    public void takeDamage(int amount) {
        currentHealth -= amount;
        if (currentHealth <= 0) {
            die();
        }

        // Create damage effect
        Vector3f position = getGameObject().getTransform().getPosition();
        createDamageEffect(position);
    }

    private void die() {
        // Game over logic
        System.out.println("Player died!");

        // Restart or show game over screen
        // SceneManager.getInstance().loadScene("GameOverScene");
    }

    private void createDamageEffect(Vector3f position) {
        GameObject effect = new GameObject("DamageEffect");
        effect.setPosition(position.x, position.y, position.z);

        // Add to scene FIRST
        SceneManager.getInstance().getActiveScene().addGameObject(effect);

        // THEN add particle system
        ParticleSystem particles = new ParticleSystem(30);
        effect.addComponent(particles);

        // Configure particles
        particles.setEmissionShape(ParticleSystem.EmissionShape.CIRCLE);
        particles.setEmissionRadius(20);
        particles.setStartColor(1.0f, 0.0f, 0.0f, 1.0f); // Red color
        particles.setEndColor(0.8f, 0.0f, 0.0f, 0.0f);
        particles.setStartSize(5.0f);
        particles.setEndSize(1.0f);
        particles.setLifetime(0.5f);
        particles.setEmissionRate(40); // Use emission rate instead of emit()
        particles.setDuration(0.5f);
        particles.setLooping(false);
    }
}