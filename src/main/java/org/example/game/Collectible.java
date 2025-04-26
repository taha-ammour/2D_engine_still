package org.example.game;

import org.example.engine.audio.AudioSystem;
import org.example.engine.audio.Sound;
import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.core.Transform;
import org.example.engine.particles.ParticleSystem;
import org.example.engine.physics.BoxCollider;
import org.example.engine.physics.CollisionInfo;
import org.example.engine.resource.ResourceManager;
import org.example.engine.scene.SceneManager;
import org.joml.Vector3f;

public class Collectible extends Component {
    private float rotationSpeed = 00.0f; // Degrees per second
    private float bobAmplitude = 0.5f;
    private float bobFrequency = 2.0f;
    private float initialY;
    private float time = 0;

    @Override
    protected void onInit() {
        initialY = getGameObject().getTransform().getPosition().y;

        // Add a collider for detection
        if (!getGameObject().hasComponent(BoxCollider.class)) {
            BoxCollider collider = new BoxCollider(16, 16); // Size of the collectible
            collider.setTrigger(true); // Set as trigger
            collider.setOnCollisionEnter(this::onCollision);
            getGameObject().addComponent(collider);
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // Update time
        time += deltaTime;

        // Rotate
        Transform transform = getGameObject().getTransform();
        transform.setRotation(transform.getRotation() + (float)Math.toRadians(rotationSpeed * deltaTime));

        // Bob up and down
        float newY = initialY + (float)Math.sin(time * bobFrequency) * bobAmplitude;
        Vector3f position = transform.getPosition();
        transform.setPosition(position.x, newY, position.z);
    }


    private void onCollision(CollisionInfo collision) {
        // Check if player collected this item
        if (collision.colliderB.getGameObject().getName().equals("Player")) {
            // Get position before destroying
            Vector3f position = getGameObject().getTransform().getPosition();

            // Play collection sound - need to ensure the sound exists
            // We'll catch exceptions because the error showed "Sound not found: coin"
            try {
                Sound sound = ResourceManager.getInstance().getSound("pickup");
                if (sound != null) {
                    AudioSystem.getInstance().playSFX("pickup", 1.0f, 1.0f, false);
                } else {
                    // Just log that sound would play here
                    System.out.println("Player picked up collectible");
                }
            } catch (Exception e) {
                // Silently continue
            }

            // Create particle effect - use the stored position
            createCollectionEffect(position);

            // Destroy this collectible
            getGameObject().destroy();
        }
    }

    private void createCollectionEffect(Vector3f position) {
        // Create a new GameObject for the effect
        GameObject effect = new GameObject("CollectionEffect");
        effect.setPosition(position.x, position.y, position.z);

        // Add the effect to the scene FIRST
        SceneManager.getInstance().getActiveScene().addGameObject(effect);

        // THEN add and configure the particle system
        ParticleSystem particles = new ParticleSystem(50);
        effect.addComponent(particles);

        // Now configure the particle system
        particles.setEmissionShape(ParticleSystem.EmissionShape.CIRCLE);
        particles.setEmissionRadius(16);
        particles.setStartColor(1.0f, 1.0f, 0.0f, 1.0f); // Gold color
        particles.setEndColor(1.0f, 1.0f, 0.0f, 0.0f);
        particles.setStartSize(8.0f);
        particles.setEndSize(2.0f);
        particles.setLifetime(0.5f);
        particles.setEmissionRate(100); // Use emission rate instead of emit()
        particles.setDuration(0.5f);
        particles.setLooping(false);

        // No need to call emit() manually, it will emit based on the rate
    }
}