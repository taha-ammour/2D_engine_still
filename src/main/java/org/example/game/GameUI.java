package org.example.game;

import org.example.engine.SpriteManager;
import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.rendering.BitmapFontRenderer;
import org.example.engine.rendering.Camera;
import org.example.engine.rendering.Sprite;
import org.example.engine.scene.Scene;
import org.example.engine.scene.SceneManager;

/**
 * Creates and manages the game UI elements using UI sprites
 * Modified to use screen-space positioning and BitmapFontRenderer
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

    // Text renderers for each stat
    private BitmapFontRenderer healthText;
    private BitmapFontRenderer energyText;
    private BitmapFontRenderer armorText;
    private BitmapFontRenderer coinsText;
    private BitmapFontRenderer keysText;

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
        healthText = healthIcon.getComponent(BitmapFontRenderer.class);

        // Create energy indicator
        energyIcon = createUIElement("energy_1", "Energy: " + energy, 1);
        scene.addGameObject(energyIcon);
        energyText = energyIcon.getComponent(BitmapFontRenderer.class);

        // Create armor indicator
        armorIcon = createUIElement("armor_1", "Armor: " + armor, 2);
        scene.addGameObject(armorIcon);
        armorText = armorIcon.getComponent(BitmapFontRenderer.class);

        // Create coin counter
        coinCounter = createUIElement("coin_pickup_1", "Coins: " + coins, 3);
        scene.addGameObject(coinCounter);
        coinsText = coinCounter.getComponent(BitmapFontRenderer.class);

        // Create key counter
        keyCounter = createUIElement("key_1", "Keys: " + keys, 4);
        scene.addGameObject(keyCounter);
        keysText = keyCounter.getComponent(BitmapFontRenderer.class);

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
        // Create parent GameObject for this UI element
        GameObject element = new GameObject("UI_" + spriteName);

        // First add to scene to ensure it's initialized properly
        SceneManager.getInstance().getActiveScene().addGameObject(element);

        // Create child GameObject for icon
        GameObject iconObj = new GameObject("Icon_" + spriteName);
        element.addChild(iconObj);

        // Add icon sprite
        Sprite iconSprite = spriteManager.createSprite(spriteName);
        if (iconSprite != null) {
            iconObj.addComponent(iconSprite);
            // Position icon to the left of text
            iconObj.setPosition(-40, 0, 0);
            // Scale up small UI icons
            if (iconSprite.getWidth() <= 8) {
                iconObj.setScale(3.0f, 3.0f, 1.0f);
            }
        } else {
            System.err.println("Failed to create UI sprite: " + spriteName);
        }

        // Add text renderer
        BitmapFontRenderer textRenderer = new BitmapFontRenderer();
        textRenderer.setText(labelText);
        element.addComponent(textRenderer);

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
        float baseX = cameraX - (screenWidth / 2) + UI_PADDING + 50;
        float baseY = cameraY - (screenHeight / 2) + UI_PADDING;

        // Update positions of each element
        updateElementPosition(healthIcon, baseX, baseY, 0);
        updateElementPosition(energyIcon, baseX, baseY, 1);
        updateElementPosition(armorIcon, baseX, baseY, 2);

        // Coin counter in top-right
        float rightX = cameraX + (screenWidth / 2) - UI_PADDING - 100;
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

        // Update text renderers
        if (healthText != null) {
            // Add color based on health level
            String colorCode = "555"; // White
            if (health < 25) {
                colorCode = "500"; // Red
            } else if (health < 50) {
                colorCode = "550"; // Yellow
            }
            healthText.setText("\\" + colorCode + "Health: " + health);
        }

        if (energyText != null) {
            energyText.setText("\\050Energy: " + energy);
        }

        if (armorText != null) {
            armorText.setText("\\005Armor: " + armor);
        }
    }

    /**
     * Update coin count
     */
    public void updateCoins(int coins) {
        this.coins = coins;
        if (coinsText != null) {
            coinsText.setText("\\540Coins: " + coins);
        }
    }

    /**
     * Update key count
     */
    public void updateKeys(int keys) {
        this.keys = keys;
        if (keysText != null) {
            keysText.setText("\\555Keys: " + keys);
        }
    }

    /**
     * Show a notification message
     */
    public void showNotification(String message) {
        // In a real implementation, you would create a temporary text object
        Scene scene = SceneManager.getInstance().getActiveScene();
        if (scene == null) return;

        // Create temporary notification object
        GameObject notification = new GameObject("Notification");
        notification.setPosition(camera.getPosition().x,
                camera.getPosition().y - 100, 10);

        // Add text renderer
        BitmapFontRenderer notifText = new BitmapFontRenderer();
        notifText.setText("\\DBL\\CTX\\550" + message);
        notifText.setScale(2.0f);
        notification.addComponent(notifText);

        // Add to scene
        scene.addGameObject(notification);

        // Add auto-destroy component to remove after a few seconds
        NotificationTimeout timeout = new NotificationTimeout(3.0f);
        notification.addComponent(timeout);

        System.out.println("NOTIFICATION: " + message);
    }

    /**
     * Helper component to remove notifications after a timeout
     */
    private static class NotificationTimeout extends Component {
        private float timeout;

        public NotificationTimeout(float seconds) {
            this.timeout = seconds;
        }

        @Override
        protected void onUpdate(float deltaTime) {
            timeout -= deltaTime;
            if (timeout <= 0) {
                getGameObject().destroy();
            }
        }
    }
}