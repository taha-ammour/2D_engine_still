package org.example.game.font;


import org.example.engine.core.GameObject;
import org.example.engine.core.Component;
import org.example.engine.rendering.BitmapFontRenderer;
import org.example.engine.rendering.Camera;
import org.example.engine.scene.Scene;
import org.example.engine.scene.SceneManager;
import org.example.game.PlayerStats;

/**
 * Example class showing how to create and use the bitmap font renderer
 */
public class GameTextManager extends Component {
    // UI elements
    private BitmapFontRenderer healthText;
    private BitmapFontRenderer scoreText;
    private BitmapFontRenderer titleText;

    // Game state references
    private PlayerStats playerStats;
    private Camera camera;

    @Override
    protected void onInit() {
        // Find player stats
        GameObject player = SceneManager.getInstance().getActiveScene().findGameObjectByTag("Player");
        if (player != null) {
            playerStats = player.getComponent(PlayerStats.class);
        }

        // Get camera
        camera = SceneManager.getInstance().getActiveScene().getMainCamera();

        // Create text UI elements
        createTextElements();
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // Update position to follow camera
        updatePositions();

        // Update text content
        updateTextContent();
    }

    /**
     * Create all text elements
     */
    private void createTextElements() {
        Scene scene = SceneManager.getInstance().getActiveScene();
        if (scene == null) return;

        // Create health text
        GameObject healthObj = new GameObject("HealthText");
        healthText = new BitmapFontRenderer();
        healthText.setText("Health: \\555100");
        healthText.setScale(2.0f);
        healthObj.addComponent(healthText);
        scene.addGameObject(healthObj);

        // Create score text
        GameObject scoreObj = new GameObject("ScoreText");
        scoreText = new BitmapFontRenderer();
        scoreText.setText("Score: \\5550");
        scoreText.setScale(2.0f);
        scoreObj.addComponent(scoreText);
        scene.addGameObject(scoreObj);

        // Create title text
        GameObject titleObj = new GameObject("TitleText");
        titleText = new BitmapFontRenderer();
        titleText.setText("\\DBL\\CTX\\050MY GAME\\RES");
        titleText.setScale(3.0f);
        titleObj.addComponent(titleText);
        scene.addGameObject(titleObj);
    }

    /**
     * Update text positions relative to camera
     */
    private void updatePositions() {
        if (camera == null) return;

        // Get camera position
        float camX = camera.getPosition().x;
        float camY = camera.getPosition().y;

        // Calculate screen bounds
        float width = camera.getViewportWidth();
        float height = camera.getViewportHeight();
        float left = camX - width/2;
        float top = camY - height/2;

        // Position health text in top-left
        if (healthText != null) {
            GameObject healthObj = healthText.getGameObject();
            healthObj.setPosition(left + 20, top + 20, 10);
        }

        // Position score text in top-right
        if (scoreText != null) {
            GameObject scoreObj = scoreText.getGameObject();
            scoreObj.setPosition(left + width - 150, top + 20, 10);
        }

        // Position title text at top center
        if (titleText != null) {
            GameObject titleObj = titleText.getGameObject();
            titleObj.setPosition(camX, top + 60, 10);
        }
    }

    /**
     * Update text content based on game state
     */
    private void updateTextContent() {
        // Update health text
        if (healthText != null && playerStats != null) {
            int health = playerStats.getHealth();

            // Set color based on health level
            String colorCode = "555"; // White
            if (health < 25) {
                colorCode = "500"; // Red
            } else if (health < 50) {
                colorCode = "550"; // Yellow
            }

            healthText.setText("Health: \\" + colorCode + health);
        }

        // Update score text
        if (scoreText != null && playerStats != null) {
            int coins = playerStats.getCoins();
            scoreText.setText("Score: \\555" + coins);
        }
    }
}