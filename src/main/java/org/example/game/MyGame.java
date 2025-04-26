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
import org.joml.Vector3f;

import java.io.File;

public class MyGame {
    private Engine engine;
    private Scene gameScene;

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

        // Set up camera
        createCamera();

        engine.getSceneManager().loadScene("GameScene");
        //loadSoundResources();
        registerSprites();

        // Create player
        createPlayer();

        // Create enemies
        createEnemies();

        // Create collectibles
        createCollectibles();

        GameObject starfieldObject = new GameObject("Starfield");
        starfieldObject.setPosition(0, 0, 0); // Position at the origin of the scene or camera space


        // Add the StarsComp component
        StarsComp starsComp = new StarsComp();
        starfieldObject.addComponent(starsComp);

        // Add the starfield object to your scene
        // Make sure this is done AFTER the camera is created and set for the scene
        gameScene.addGameObject(starfieldObject); // Assuming 'gameScene' is your Scene variable


        engine.setClearColor(0.2f, 0.2f, 0.3f);
        // Load scene
        engine.getSceneManager().loadScene("GameScene");
    }

    private void createCamera() {
        GameObject cameraObj = new GameObject("MainCamera");
        Camera camera = new Camera(800, 600);
        cameraObj.addComponent(camera);
        cameraObj.setPosition(400, 300, 10);
        gameScene.addGameObject(cameraObj);
        gameScene.setMainCamera(camera);
    }


    private void createPlayer() {
        GameObject player = new GameObject("Player");
        player.setTag("Player");
        player.setPosition(400, 300, 0);

        // Add sprite
        SpriteManager spriteManager = SpriteManager.getInstance();
        Sprite playerSprite = spriteManager.createSprite("player_sprite_d");

        player.addComponent(playerSprite);


        // Add physics components
        Rigidbody rb = new Rigidbody();
        player.addComponent(rb);

        BoxCollider collider = new BoxCollider(playerSprite.getWidth(), playerSprite.getHeight());
        player.addComponent(collider);

        // Add player controller
        PlayerController controller = new PlayerController();
        player.addComponent(controller);
        player.setScale(2,2,2);

        // Add weapon
        PlayerWeapon weapon = new PlayerWeapon();
        player.addComponent(weapon);

        // Add health
        PlayerHealth health = new PlayerHealth(100);
        player.addComponent(health);

        gameScene.addGameObject(player);

        // Set camera to follow player
        Camera camera = gameScene.getMainCamera();
        if (camera != null) {
            camera.follow(player.getTransform().getPosition());
            camera.setFollowSpeed(1.1f);
        }
    }

    private void createEnemies() {
        // Create some patrol points
        Vector3f[] patrolPoints1 = {
                new Vector3f(200, 200, 0),
                new Vector3f(600, 200, 0),
                new Vector3f(600, 400, 0),
                new Vector3f(200, 400, 0)
        };

        // Create first enemy
        GameObject enemy1 = new GameObject("Enemy1");
        enemy1.setPosition(200, 200, 0);

        Sprite enemySprite = new Sprite(null, 16, 16);
        enemySprite.setColor(0xFF0000, 1.0f);
        enemy1.addComponent(enemySprite);

        BoxCollider enemyCollider = new BoxCollider(16, 16);
        enemy1.addComponent(enemyCollider);

        EnemyAI enemyAI = new EnemyAI(patrolPoints1);
        enemy1.addComponent(enemyAI);

        gameScene.addGameObject(enemy1);

        // Create more enemies as needed...
    }

    private void createCollectibles() {
        // Create several collectibles around the level
        for (int i = 0; i < 10; i++) {
            float x = 100 + (i * 70);
            float y = 250 + (float)(Math.sin(i * 0.5) * 100);

            GameObject collectible = new GameObject("Collectible" + i);
            collectible.setPosition(x, y, 0);

            Sprite collectibleSprite = new Sprite(null, 16, 16);
            collectibleSprite.setColor(0xFFFF00, 1.0f); // Yellow color
            collectible.addComponent(collectibleSprite);

            Collectible behavior = new Collectible();
            collectible.addComponent(behavior);

            gameScene.addGameObject(collectible);
        }
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