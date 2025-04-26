package org.example.game;

import org.example.engine.SpriteManager;
import org.example.engine.animation.Animation;
import org.example.engine.animation.AnimationFrame;
import org.example.engine.animation.Animator;
import org.example.engine.audio.AudioSystem;
import org.example.engine.audio.Sound;
import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.particles.ParticleSystem;
import org.example.engine.physics.BoxCollider;
import org.example.engine.physics.CollisionInfo;
import org.example.engine.rendering.Sprite;
import org.example.engine.rendering.Texture;
import org.example.engine.resource.ResourceManager;
import org.example.engine.scene.SceneManager;
import org.joml.Vector3f;

/**
 * Animated collectible that cycles through different sprites
 */
public class AnimatedCollectible extends Component {
    private float bobAmplitude = 0.5f;
    private float bobFrequency = 2.0f;
    private float rotationSpeed = 0.0f; // Degrees per second
    private float initialY;
    private float time = 0;
    private boolean isEmerald = false;

    // Scale for the collectible
    private float scale = 2.0f;

    /**
     * Create a new collectible
     */
    public AnimatedCollectible() {
    }

    /**
     * Create a new collectible with specified animation type
     * @param isEmerald Whether this is an emerald (true) or chest (false)
     */
    public AnimatedCollectible(boolean isEmerald) {
        this.isEmerald = isEmerald;
    }

    @Override
    protected void onInit() {
        // Store initial Y position for bobbing effect
        initialY = getGameObject().getTransform().getPosition().y;

        // Apply the scale
        getGameObject().setScale(scale, scale, 1.0f);

        // Setup animation
        setupAnimation();

        // Add a collider for detection - add this at the end
        if (!getGameObject().hasComponent(BoxCollider.class)) {
            BoxCollider collider = new BoxCollider(16 * scale, 16 * scale);
            collider.setTrigger(true); // Set as trigger
            collider.setOnCollisionEnter(this::onCollision);
            getGameObject().addComponent(collider);
        }
    }

    /**
     * Setup animation based on collectible type
     */
    private void setupAnimation() {
        SpriteManager spriteManager = SpriteManager.getInstance();
        Sprite targetSprite = getGameObject().getComponent(Sprite.class);

        if (targetSprite == null) {
            System.err.println("AnimatedCollectible: No target sprite found on GameObject");
            return;
        }

        // Create animator component
        Animator animator = new Animator(targetSprite);
        getGameObject().addComponent(animator);

        // Create animation
        Animation animation = new Animation(0.3f); // 0.3s per frame

        if (isEmerald) {
            // Emerald animation
            Sprite emerald1 = spriteManager.createSprite("Emerald_id_1");
            Sprite emerald2 = spriteManager.createSprite("Emerald_id_2");
            Sprite emerald3 = spriteManager.createSprite("Emerald_id_3");

            if (emerald1 != null && emerald2 != null && emerald3 != null) {
                AnimationFrame frame1 = new AnimationFrame(emerald1.getTexture());
                AnimationFrame frame2 = new AnimationFrame(emerald2.getTexture());
                AnimationFrame frame3 = new AnimationFrame(emerald3.getTexture());

                animation.addFrame(frame1);
                animation.addFrame(frame2);
                animation.addFrame(frame3);
                animation.addFrame(frame2); // Back to middle frame for smooth loop

                // Add animation to animator
                animator.addAnimation("idle", animation);
                animator.play("idle");
                animator.setLooping(true);
            } else {
                System.err.println("Failed to load emerald sprites for animation");
            }
        } else {
            // Chest animation
            Sprite chest1 = spriteManager.createSprite("ChestE_id_1");
            Sprite chest2 = spriteManager.createSprite("ChestE_id_2");
            Sprite chest3 = spriteManager.createSprite("ChestE_id_3");

            if (chest1 != null && chest2 != null && chest3 != null) {
                AnimationFrame frame1 = new AnimationFrame(chest1.getTexture());
                AnimationFrame frame2 = new AnimationFrame(chest2.getTexture());
                AnimationFrame frame3 = new AnimationFrame(chest3.getTexture());

                animation.addFrame(frame1);
                animation.addFrame(frame2);
                animation.addFrame(frame3);
                animation.addFrame(frame2); // Back to middle frame for smooth loop

                // Add animation to animator
                animator.addAnimation("idle", animation);
                animator.play("idle");
                animator.setLooping(true);
            } else {
                System.err.println("Failed to load chest sprites for animation");
            }
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // Update time
        time += deltaTime;

        // Bob up and down
        float newY = initialY + (float)Math.sin(time * bobFrequency) * bobAmplitude;
        Vector3f position = getGameObject().getTransform().getPosition();
        getGameObject().getTransform().setPosition(position.x, newY, position.z);

        // Gentle rotation
        getGameObject().getTransform().rotate(rotationSpeed * deltaTime);
    }

    private void onCollision(CollisionInfo collision) {
        // Check if player collected this item
        if (collision.colliderB.getGameObject().getName().equals("Player")) {
            // Get position before destroying
            Vector3f position = getGameObject().getTransform().getPosition();

            // Play collection sound - need to ensure the sound exists
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
        try {
            // Create a new GameObject for the effect
            GameObject effect = new GameObject("CollectionEffect");
            effect.setPosition(position.x, position.y, position.z);

            // Add the effect to the scene FIRST
            SceneManager.getInstance().getActiveScene().addGameObject(effect);

            // Instead of creating the particle system directly, create a simple component
            // that will add the particle system on its first update (next frame)
            DelayedParticleCreator delayedCreator = new DelayedParticleCreator(isEmerald, scale);
            effect.addComponent(delayedCreator);
        } catch (Exception e) {
            // Log the error but don't crash when collecting
            System.err.println("Error creating collection effect: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper component that creates a particle system on its first update
     * This avoids the ConcurrentModificationException and OpenGL context issues
     */
    private static class DelayedParticleCreator extends Component {
        private boolean isEmerald;
        private float scale;
        private boolean created = false;

        public DelayedParticleCreator(boolean isEmerald, float scale) {
            this.isEmerald = isEmerald;
            this.scale = scale;
        }

        @Override
        protected void onUpdate(float deltaTime) {
            if (!created) {
                created = true;

                try {
                    // Create the particle system on the main thread during a regular update
                    ParticleSystem particles = new ParticleSystem(50);
                    getGameObject().addComponent(particles);

                    // Configure particle color based on collectible type
                    if (isEmerald) {
                        // Green particles for emerald
                        particles.setStartColor(0.0f, 1.0f, 0.5f, 1.0f);
                        particles.setEndColor(0.0f, 0.8f, 0.2f, 0.0f);
                    } else {
                        // Gold particles for chest
                        particles.setStartColor(1.0f, 0.8f, 0.0f, 1.0f);
                        particles.setEndColor(0.8f, 0.6f, 0.0f, 0.0f);
                    }

                    // Configure the particle system
                    particles.setEmissionShape(ParticleSystem.EmissionShape.CIRCLE);
                    particles.setEmissionRadius(8 * scale); // Scale to match collectible
                    particles.setStartSize(4.0f);
                    particles.setEndSize(1.0f);
                    particles.setLifetime(0.5f);
                    particles.setEmissionRate(100);
                    particles.setDuration(0.5f);
                    particles.setLooping(false);

                    // Emit particles
                    particles.emit(50);
                } catch (Exception e) {
                    System.err.println("Error creating particle system: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}