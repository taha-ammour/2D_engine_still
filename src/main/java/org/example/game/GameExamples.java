package org.example.game;

import org.example.engine.Engine;
import org.example.engine.EntityRegistry;
import org.example.engine.SpriteManager;
import org.example.engine.core.GameObject;
import org.example.engine.rendering.Sprite;
import org.example.engine.scene.Scene;

/**
 * Sample game that demonstrates using the EntityRegistry.
 */
public class GameExamples {
    private Engine engine;
    private Scene scene;

    /**
     * Initialize the game
     */
    public void init() {
        // Initialize the engine
        engine = new Engine();
        engine.init(800, 600, "Game Example");

        // Create a scene
        scene = engine.getSceneManager().createScene("MainScene");

        // Register entities, UI, and tiles
        SpriteManager spriteManager = SpriteManager.getInstance();

        // Set custom palette for player if needed
        EntityRegistry.setOverridePalette("player_sprite_d", new String[]{"321", "432", "543", "555"});

        // Register all sprites
        EntityRegistry.registerEntities(spriteManager);
        EntityRegistry.registerUi(spriteManager);
        EntityRegistry.registerTiles(spriteManager);

        // Create a player object
        GameObject player = new GameObject("Player");
        player.setPosition(400, 300, 0);

        // Create a sprite from the registry
        Sprite playerSprite = spriteManager.createSprite("player_sprite_d");
        player.addComponent(playerSprite);

        // Add player to scene
        scene.addGameObject(player);

        // Create enemies
        createEnemy("enemy_demon_sprite_d", 200, 300, 0);
        createEnemy("enemy_ghost_sprite_d", 300, 200, 0);
        createEnemy("enemy_orc_sprite_d", 500, 350, 0);

        // Add some tiles
        createTile("tile_walkable_1", 100, 400, -0.5f);
        createTile("tile_walkable_2", 116, 400, -0.5f);
        createTile("tile_walkable_3", 132, 400, -0.5f);
        createTile("tile_walkable_4", 148, 400, -0.5f);

        // Add some UI elements
        createUIElement("health_1", 20, 20, 1);
        createUIElement("energy_1", 40, 20, 1);

        // Load the scene
        engine.getSceneManager().loadScene("MainScene");

        // Set clear color
        engine.setClearColor(0.1f, 0.1f, 0.2f);
    }

    /**
     * Create an enemy game object
     */
    private GameObject createEnemy(String spriteName, float x, float y, float z) {
        GameObject enemy = new GameObject("Enemy_" + spriteName);
        enemy.setPosition(x, y, z);

        Sprite sprite = SpriteManager.getInstance().createSprite(spriteName);
        enemy.addComponent(sprite);

        scene.addGameObject(enemy);
        return enemy;
    }

    /**
     * Create a tile game object
     */
    private GameObject createTile(String spriteName, float x, float y, float z) {
        GameObject tile = new GameObject("Tile_" + spriteName);
        tile.setPosition(x, y, z);

        Sprite sprite = SpriteManager.getInstance().createSprite(spriteName);
        tile.addComponent(sprite);

        scene.addGameObject(tile);
        return tile;
    }

    /**
     * Create a UI element game object
     */
    private GameObject createUIElement(String spriteName, float x, float y, float z) {
        GameObject uiElement = new GameObject("UI_" + spriteName);
        uiElement.setPosition(x, y, z);

        Sprite sprite = SpriteManager.getInstance().createSprite(spriteName);
        uiElement.addComponent(sprite);

        scene.addGameObject(uiElement);
        return uiElement;
    }

    /**
     * Start the game loop
     */
    public void run() {
        engine.run();
    }

    /**
     * Program entry point
     */
    public static void main(String[] args) {
        GameExamples game = new GameExamples();
        game.init();
        game.run();
    }
}