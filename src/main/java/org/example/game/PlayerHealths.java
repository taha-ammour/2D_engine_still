package org.example.game;

import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.particles.ParticleSystem;
import org.example.engine.scene.SceneManager;
import org.joml.Vector3f;

public class PlayerHealths extends Component {
    private int maxHealth;
    private int currentHealth;
    private PlayerStats playerStats;
    private float invulnerabilityTimer = 0;
    private static final float INVULNERABLE_DURATION = 0.5f; // Seconds of invulnerability after taking damage

    public PlayerHealths(int maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    @Override
    protected void onInit() {
        // Find PlayerStats component if available
        playerStats = getGameObject().getComponent(PlayerStats.class);
        if (playerStats != null) {
            // Sync our health with PlayerStats at startup
            playerStats.setHealth(currentHealth);
            System.out.println("PlayerHealth synchronized with PlayerStats");
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // Update invulnerability timer
        if (invulnerabilityTimer > 0) {
            invulnerabilityTimer -= deltaTime;
        }

        // Find PlayerStats if we haven't already
        if (playerStats == null) {
            playerStats = getGameObject().getComponent(PlayerStats.class);
            if (playerStats != null) {
                // Sync our health with PlayerStats
                playerStats.setHealth(currentHealth);
            }
        }
    }

    /**
     * Take damage with player feedback and invulnerability frames
     */
    public void takeDamage(int amount) {
        // Check if we're in invulnerability period
        if (invulnerabilityTimer > 0) {
            return; // Skip damage during invulnerability
        }

        // Apply damage
        currentHealth -= amount;
        if (currentHealth <= 0) {
            currentHealth = 0;
            die();
        }

        // Set invulnerability timer
        invulnerabilityTimer = INVULNERABLE_DURATION;

        // Create damage effect
        Vector3f position = getGameObject().getTransform().getPosition();
        createDamageEffect(position);

        // Sync with player stats
        if (playerStats != null) {
            playerStats.setHealth(currentHealth);
        }
    }

    /**
     * Set health directly (for syncing with PlayerStats)
     */
    public void setHealth(int health) {
        if (this.currentHealth != health) {
            // Check if this is damage or healing
            boolean isDamage = health < this.currentHealth;

            // Update health
            this.currentHealth = Math.max(0, Math.min(maxHealth, health));

            // Create effects if this was damage
            if (isDamage) {
                Vector3f position = getGameObject().getTransform().getPosition();
                createDamageEffect(position);
            }

            // Check for death
            if (this.currentHealth <= 0) {
                die();
            }
        }
    }

    /**
     * Get current health
     */
    public int getHealth() {
        return currentHealth;
    }

    /**
     * Get maximum health
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    private void die() {
        // Game over logic
        System.out.println("Player died!");

        // Create death effect (more dramatic)
        Vector3f position = getGameObject().getTransform().getPosition();
        createDeathEffect(position);

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
        particles.setEmissionRadius(16);
        particles.setStartColor(1.0f, 0.2f, 0.2f, 0.8f); // Red color
        particles.setEndColor(0.8f, 0.0f, 0.0f, 0.0f);
        particles.setStartSize(5.0f);
        particles.setEndSize(2.0f);
        particles.setLifetime(0.5f);
        particles.setEmissionRate(60); // Higher rate
        particles.setDuration(0.3f);
        particles.setLooping(false);

        // Emit a burst of particles
        particles.emit(15);
    }

    private void createDeathEffect(Vector3f position) {
        GameObject effect = new GameObject("DeathEffect");
        effect.setPosition(position.x, position.y, position.z);

        // Add to scene FIRST
        SceneManager.getInstance().getActiveScene().addGameObject(effect);

        // THEN add particle system
        ParticleSystem particles = new ParticleSystem(100);
        effect.addComponent(particles);

        // Configure particles for a more dramatic death effect
        particles.setEmissionShape(ParticleSystem.EmissionShape.CIRCLE);
        particles.setEmissionRadius(32);
        particles.setStartColor(1.0f, 0.0f, 0.0f, 1.0f); // Bright red
        particles.setEndColor(0.5f, 0.0f, 0.0f, 0.0f);
        particles.setStartSize(8.0f);
        particles.setEndSize(2.0f);
        particles.setLifetime(1.5f);
        particles.setEmissionRate(100);
        particles.setDuration(1.0f);
        particles.setLooping(false);
        particles.setEmissionForce(50.0f); // Strong force outward

        // Emit a large burst of particles
        particles.emit(50);
    }
}
