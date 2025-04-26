package org.example.game;

import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.scene.Scene;
import org.example.engine.scene.SceneManager;

/**
 * Manages player statistics like health, coins, etc.
 */
public class PlayerStats extends Component {
    // Player stats
    private int maxHealth = 100;
    private int currentHealth = 100;
    private int maxEnergy = 100;
    private int currentEnergy = 100;
    private int armor = 50;
    private int coins = 0;
    private int keys = 0;

    // Reference to the UI
    private GameUI gameUI;

    @Override
    protected void onInit() {
        // Find the game UI
        findGameUI();

        // Update UI with initial values
        updateUI();
    }

    /**
     * Find the GameUI component in the scene
     */
    private void findGameUI() {
        Scene scene = SceneManager.getInstance().getActiveScene();
        if (scene != null) {
            GameObject uiContainer = scene.findGameObjectByTag("UI");
            if (uiContainer != null) {
                gameUI = uiContainer.getComponent(GameUI.class);
            }
        }

        if (gameUI == null) {
            System.out.println("Warning: GameUI not found, UI updates will be disabled");
        }
    }

    /**
     * Update the UI with current stats
     */
    private void updateUI() {
        if (gameUI != null) {
            gameUI.updatePlayerStats(currentHealth, currentEnergy, armor);
            gameUI.updateCoins(coins);
            gameUI.updateKeys(keys);
        }
    }

    /**
     * Take damage
     *
     * @param amount Amount of damage to take
     * @return Actual damage taken after armor
     */
    public int takeDamage(int amount) {
        // Calculate damage reduction from armor
        float damageReduction = armor / 100.0f;
        int reducedDamage = (int)(amount * (1.0f - damageReduction));

        // Apply damage
        currentHealth = Math.max(0, currentHealth - reducedDamage);

        // Check if player died
        if (currentHealth <= 0) {
            onDeath();
        }

        // Update UI
        updateUI();

        return reducedDamage;
    }

    /**
     * Heal the player
     *
     * @param amount Amount to heal
     * @return Actual amount healed
     */
    public int heal(int amount) {
        int previousHealth = currentHealth;
        currentHealth = Math.min(maxHealth, currentHealth + amount);

        int actualHealed = currentHealth - previousHealth;

        // Update UI
        updateUI();

        return actualHealed;
    }

    /**
     * Use energy
     *
     * @param amount Amount of energy to use
     * @return true if enough energy was available, false otherwise
     */
    public boolean useEnergy(int amount) {
        if (currentEnergy >= amount) {
            currentEnergy -= amount;
            updateUI();
            return true;
        }
        return false;
    }

    /**
     * Restore energy
     *
     * @param amount Amount of energy to restore
     */
    public void restoreEnergy(int amount) {
        currentEnergy = Math.min(maxEnergy, currentEnergy + amount);
        updateUI();
    }

    /**
     * Add coins
     *
     * @param amount Number of coins to add
     */
    public void addCoins(int amount) {
        coins += amount;
        updateUI();

        if (gameUI != null) {
            gameUI.showNotification("Collected " + amount + " coins!");
        }
    }

    /**
     * Add a key
     */
    public void addKey() {
        keys++;
        updateUI();

        if (gameUI != null) {
            gameUI.showNotification("Found a key!");
        }
    }

    /**
     * Use a key to unlock something
     *
     * @return true if a key was available to use, false otherwise
     */
    public boolean useKey() {
        if (keys > 0) {
            keys--;
            updateUI();
            return true;
        }
        return false;
    }

    /**
     * Handle player death
     */
    private void onDeath() {
        // In a real implementation, this would trigger game over logic
        System.out.println("Player died!");
    }

    // Getters
    public int getHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public int getEnergy() { return currentEnergy; }
    public int getMaxEnergy() { return maxEnergy; }
    public int getArmor() { return armor; }
    public int getCoins() { return coins; }
    public int getKeys() { return keys; }
}