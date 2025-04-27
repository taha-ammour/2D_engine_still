package org.example.game;

import org.example.engine.SpriteManager;
import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.rendering.Camera;
import org.example.engine.rendering.Sprite;
import org.example.engine.scene.Scene;
import org.example.engine.scene.SceneManager;

/**
 * Creates and manages the game UI elements using UI sprites
 * Modified to use screen-space positioning instead of world-space
 */
public class GameUI extends Component {
    // UI Element positioning constants
    private static final float UI_PADDING = 10.0f;
    private static final float ICON_SIZE = 24.0f;
    private static final float SPACING = 4.0f;

    // UI Element references
    private GameObject healthIcon;
    private GameObject energyIcon;
    private GameObject armorIcon;
    private GameObject coinCounter;
    private GameObject keyCounter;

    // UI State
    private int health = 100;
    private int energy = 100;
    private int armor = 50;
    private int coins = 0;
    private int keys = 0;

    // References
    private SpriteManager spriteManager;
    private Camera camera;

    @Override
    protected void onInit() {
        spriteManager = SpriteManager.getInstance();

        // Find the camera
        Scene scene = SceneManager.getInstance().getActiveScene();
        if (scene != null) {
            camera = scene.getMainCamera();
        }

        // Create UI elements
        createHUD();
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // Update UI positions in case window is resized
        updatePositions();
    }

    /**
     * Create all HUD elements
     */
    private void createHUD() {
        Scene scene = SceneManager.getInstance().getActiveScene();
        if (scene == null) return;

        // Create health indicator
        healthIcon = createUIElement("health_1", "Health: " + health, 0);
        scene.addGameObject(healthIcon);

        // Create energy indicator
        energyIcon = createUIElement("energy_1", "Energy: " + energy, 1);
        scene.addGameObject(energyIcon);

        // Create armor indicator
        armorIcon = createUIElement("armor_1", "Armor: " + armor, 2);
        scene.addGameObject(armorIcon);

        // Create coin counter
        coinCounter = createUIElement("coin_pickup_1", "Coins: " + coins, 3);
        scene.addGameObject(coinCounter);

        // Create key counter
        keyCounter = createUIElement("key_1", "Keys: " + keys, 4);
        scene.addGameObject(keyCounter);

        // Set initial positions
        updatePositions();
    }

    /**
     * Create a single UI element with icon and text
     *
     * @param spriteName Name of the sprite to use as icon
     * @param labelText Text label to display
     * @param position Position index (0-based, left to right)
     * @return The created GameObject
     */
    private GameObject createUIElement(String spriteName, String labelText, int position) {
        GameObject element = new GameObject("UI_" + spriteName);

        // Get sprite from registry
        Sprite iconSprite = spriteManager.createSprite(spriteName);
        if (iconSprite != null) {
            element.addComponent(iconSprite);

            // Scale up small UI icons
            if (iconSprite.getWidth() <= 8) {
                element.setScale(3.0f, 3.0f, 1.0f);
            }
        } else {
            System.err.println("Failed to create UI sprite: " + spriteName);
        }

        // Set to a high Z value to ensure it renders on top
        element.setPosition(0, 0, 10);

        return element;
    }

    /**
     * Update the positions of all UI elements to align with screen
     * This uses camera-relative positioning instead of world coordinates
     */
    private void updatePositions() {
        if (camera == null) return;

        // Get screen dimensions
        float screenWidth = camera.getViewportWidth();
        float screenHeight = camera.getViewportHeight();

        // Get camera position
        float cameraX = camera.getPosition().x;
        float cameraY = camera.getPosition().y;

        // Calculate UI element positions in camera space
        // This is the key part - we add the camera position to keep UI fixed to camera view
        float baseX = cameraX - (screenWidth / 2) + UI_PADDING;
        float baseY = cameraY - (screenHeight / 2) + UI_PADDING;

        // Update positions of each element
        updateElementPosition(healthIcon, baseX, baseY, 0);
        updateElementPosition(energyIcon, baseX, baseY, 1);
        updateElementPosition(armorIcon, baseX, baseY, 2);

        // Coin counter in top-right
        float rightX = cameraX + (screenWidth / 2) - UI_PADDING - ICON_SIZE;
        updateElementPosition(coinCounter, rightX, baseY, 0);

        // Key counter below coin counter
        updateElementPosition(keyCounter, rightX, baseY, 1);
    }

    /**
     * Update a single UI element's position
     *
     * @param element The GameObject to update
     * @param baseX Base X position
     * @param baseY Base Y position
     * @param index Vertical index (0-based, top to bottom)
     */
    private void updateElementPosition(GameObject element, float baseX, float baseY, int index) {
        if (element == null) return;

        float x = baseX;
        float y = baseY + (index * (ICON_SIZE + SPACING));
        float z = element.getTransform().getPosition().z; // Keep existing Z

        element.setPosition(x, y, z);
    }

    /**
     * Update player stats in the UI
     */
    public void updatePlayerStats(int health, int energy, int armor) {
        this.health = health;
        this.energy = energy;
        this.armor = armor;
    }

    /**
     * Update coin count
     */
    public void updateCoins(int coins) {
        this.coins = coins;
    }

    /**
     * Update key count
     */
    public void updateKeys(int keys) {
        this.keys = keys;
    }

    /**
     * Show a notification message
     */
    public void showNotification(String message) {
        // In a real implementation, you would create a temporary text object
        System.out.println("NOTIFICATION: " + message);
    }
}