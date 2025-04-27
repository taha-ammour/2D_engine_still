package org.example.game;

import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.physics.CollisionInfo;
import org.example.engine.scene.SceneManager;

/**
 * Manager for handling collectible items in the game
 * Centralizes collectible processing and rewards
 */
public class CollectibleManager extends Component {
    // Types of collectibles with their rewards
    private static CollectibleManager instance;

    public static CollectibleManager getInstance() {
        if (instance != null){
            return instance;
        }

        return new CollectibleManager();
    }

    public CollectibleManager() {}

    public enum CollectibleType {
        EMERALD      (3, 10,  0,  0),    // 10 coins
        SMALL_CHEST  (3, 25,  0,  0),    // 25 coins
        LARGE_CHEST  (3, 50, 10,  1),    // 50 coins, 10 energy, 1 key
        HEALTH_POTION(4,  0,   0,  0, 25); // 25 health (last slot)

        private final int      num;
        private final Integer[] values;

        CollectibleType(int num, int... values) {
            if (values.length != num) {
                throw new IllegalArgumentException(
                        "Expected " + num + " values for " + name() +
                                ", but got " + values.length
                );
            }
            this.num = num;
            this.values = new Integer[num];
            for (int i = 0; i < num; i++) {
                this.values[i] = values[i];
            }
        }

        /** How many parameters (coins, energy, keys, etc) this type carries */
        public int getNum() {
            return num;
        }

        /** The raw values array (boxed) */
        public Integer[] getValues() {
            return values.clone();
        }
    }

    private PlayerStats playerStats;

    @Override
    protected void onInit() {
        // Find player stats
        GameObject player = SceneManager.getInstance().getActiveScene().findGameObjectByTag("Player");
        if (player != null) {
            playerStats = player.getComponent(PlayerStats.class);
            System.out.println("CollectibleManager: Found PlayerStats component");
        } else {
            System.out.println("CollectibleManager: Player not found");
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // Find player stats if not found during init
        if (playerStats == null) {
            GameObject player = SceneManager.getInstance().getActiveScene().findGameObjectByTag("Player");
            if (player != null) {
                playerStats = player.getComponent(PlayerStats.class);
                if (playerStats != null) {
                    System.out.println("CollectibleManager: Found PlayerStats component");
                }
            }
        }
    }

    /**
     * Process a collectible and give appropriate rewards
     * @param collectibleType Type of collectible
     * @param collectible The collectible GameObject (will be destroyed)
     * @return True if successfully collected
     */
    public boolean collectItem(CollectibleType collectibleType, GameObject collectible) {
        if (playerStats == null) return false;

        // Apply rewards based on collectible type
        switch (collectibleType) {
            case EMERALD:
                playerStats.addCoins(10);
                break;

            case SMALL_CHEST:
                playerStats.addCoins(25);
                break;

            case LARGE_CHEST:
                playerStats.addCoins(50);
                playerStats.restoreEnergy(10);
                playerStats.addKey();
                break;

            case HEALTH_POTION:
                playerStats.heal(25);
                break;
        }

        // Destroy the collectible
        if (collectible != null) {
            collectible.destroy();
        }

        return true;
    }

    /**
     * Helper method to process collision with a collectible
     * @param collision The collision info
     * @param collectibleType The type of collectible
     * @return True if successfully collected
     */
    public boolean processCollision(CollisionInfo collision, CollectibleType collectibleType) {
        if (collision.colliderB.getGameObject().getName().equals("Player")) {
            return collectItem(collectibleType, collision.colliderA.getGameObject());
        }
        return false;
    }
}