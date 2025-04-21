package org.example.engine.particles;

import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.rendering.Material;
import org.example.engine.rendering.Renderable;
import org.example.engine.rendering.RenderSystem;
import org.example.engine.rendering.Texture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Particle system component for rendering particle effects.
 * Supports various emission shapes, colors, sizes, and behaviors.
 */
public class ParticleSystem extends Component implements Renderable {
    // Particle pool
    private final List<Particle> particles = new ArrayList<>();
    private final List<Particle> activeParticles = new ArrayList<>();
    private int maxParticles = 1000;

    // Emission properties
    private float emissionRate = 10.0f; // Particles per second
    private EmissionShape emissionShape = EmissionShape.POINT;
    private float emissionRadius = 1.0f;
    private Vector2f emissionBox = new Vector2f(1.0f, 1.0f);
    private Vector2f emissionDirection = new Vector2f(0.0f, -1.0f);
    private float emissionAngle = 30.0f; // Spread angle in degrees
    private float emissionForce = 1.0f;
    private float emissionForceVariance = 0.2f;

    // Particle properties
    private Vector4f startColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private Vector4f endColor = new Vector4f(1.0f, 1.0f, 1.0f, 0.0f);
    private float startSize = 1.0f;
    private float endSize = 0.5f;
    private float sizeVariance = 0.2f;
    private float lifetime = 2.0f;
    private float lifetimeVariance = 0.5f;

    // Physics properties
    private Vector2f gravity = new Vector2f(0.0f, 9.8f);
    private float damping = 0.1f;

    // Rendering properties
    private Texture particleTexture;
    private Material material;
    private int zIndex = 0;
    private BlendMode blendMode = BlendMode.ALPHA;

    // System properties
    private boolean playing = true;
    private boolean looping = true;
    private float duration = 0.0f; // 0 = infinite
    private float timer = 0.0f;
    private float emissionAccumulator = 0.0f;

    // Rendering buffers
    private int vao;
    private int vbo;

    // Random generator
    private final Random random = new Random();

    /**
     * Particle emission shapes
     */
    public enum EmissionShape {
        POINT,
        CIRCLE,
        RECTANGLE,
        CONE
    }

    /**
     * Blending modes
     */
    public enum BlendMode {
        ALPHA,
        ADDITIVE,
        MULTIPLY
    }

    /**
     * Create a new particle system with default settings
     */
    public ParticleSystem() {
        initBuffers();
        initParticlePool();
    }

    /**
     * Create a new particle system with the specified max particles
     */
    public ParticleSystem(int maxParticles) {
        this.maxParticles = maxParticles;
        initBuffers();
        initParticlePool();
    }

    /**
     * Initialize OpenGL buffers
     */
    private void initBuffers() {
        // Create VAO
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Create VBO
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // Allocate buffer - each particle is a quad with 4 vertices, each with position (3), color (4), and texture coords (2)
        glBufferData(GL_ARRAY_BUFFER, maxParticles * 4 * (3 + 4 + 2) * Float.BYTES, GL_DYNAMIC_DRAW);

        // Set up vertex attributes
        // Position (3 floats)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Color (4 floats)
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Texture coordinates (2 floats)
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 9 * Float.BYTES, 7 * Float.BYTES);
        glEnableVertexAttribArray(2);

        // Unbind VAO
        glBindVertexArray(0);
    }

    /**
     * Initialize the particle pool
     */
    private void initParticlePool() {
        particles.clear();
        for (int i = 0; i < maxParticles; i++) {
            particles.add(new Particle());
        }
    }

    @Override
    protected void onInit() {
        super.onInit();
    }

    @Override
    protected void onUpdate(float deltaTime) {
        if (!playing) return;

        // Update system timer
        timer += deltaTime;

        // Check if duration is reached for non-looping systems
        if (duration > 0 && timer >= duration && !looping) {
            playing = false;
            return;
        }

        // Reset timer for looping systems
        if (duration > 0 && timer >= duration && looping) {
            timer = 0;
        }

        // Emit new particles
        emissionAccumulator += emissionRate * deltaTime;
        while (emissionAccumulator >= 1.0f) {
            emitParticle();
            emissionAccumulator -= 1.0f;
        }

        // Update active particles
        updateParticles(deltaTime);
    }

    /**
     * Emit a single particle
     */
    private void emitParticle() {
        // Don't emit if we've reached max particles
        if (activeParticles.size() >= maxParticles) {
            return;
        }

        // Get a particle from the pool
        Particle particle = getParticleFromPool();
        if (particle == null) {
            return; // No more particles available
        }

        // Calculate emission position
        Vector3f position = getGameObject().getTransform().getPosition();

        // Apply emission shape
        Vector2f emissionOffset = new Vector2f();
        Vector2f emissionVelocity = new Vector2f();

        switch (emissionShape) {
            case POINT:
                // No offset for point emission
                break;

            case CIRCLE:
                // Random point in a circle
                float angle = random.nextFloat() * 360.0f;
                float distance = random.nextFloat() * emissionRadius;
                emissionOffset.x = (float) Math.cos(Math.toRadians(angle)) * distance;
                emissionOffset.y = (float) Math.sin(Math.toRadians(angle)) * distance;
                break;

            case RECTANGLE:
                // Random point in a rectangle
                emissionOffset.x = (random.nextFloat() * 2 - 1) * emissionBox.x * 0.5f;
                emissionOffset.y = (random.nextFloat() * 2 - 1) * emissionBox.y * 0.5f;
                break;

            case CONE:
                // Calculate direction within cone angle
                float coneAngle = (random.nextFloat() * 2 - 1) * emissionAngle * 0.5f;
                float baseAngle = (float) Math.toDegrees(Math.atan2(emissionDirection.y, emissionDirection.x));
                float finalAngle = baseAngle + coneAngle;

                emissionVelocity.x = (float) Math.cos(Math.toRadians(finalAngle));
                emissionVelocity.y = (float) Math.sin(Math.toRadians(finalAngle));
                break;
        }

        // If emission velocity is not set by shape, use direction with force
        if (emissionVelocity.x == 0 && emissionVelocity.y == 0) {
            emissionVelocity.set(emissionDirection).normalize();
        }

        // Calculate force with variance
        float force = emissionForce * (1.0f + (random.nextFloat() * 2 - 1) * emissionForceVariance);
        emissionVelocity.mul(force);

        // Calculate lifetime with variance
        float particleLifetime = lifetime * (1.0f + (random.nextFloat() * 2 - 1) * lifetimeVariance);

        // Calculate size with variance
        float particleStartSize = startSize * (1.0f + (random.nextFloat() * 2 - 1) * sizeVariance);
        float particleEndSize = endSize * (1.0f + (random.nextFloat() * 2 - 1) * sizeVariance);

        // Initialize particle
        particle.active = true;
        particle.position.set(position.x + emissionOffset.x, position.y + emissionOffset.y, position.z);
        particle.velocity.set(emissionVelocity.x, emissionVelocity.y);
        particle.color.set(startColor);
        particle.startColor.set(startColor);
        particle.endColor.set(endColor);
        particle.size = particleStartSize;
        particle.startSize = particleStartSize;
        particle.endSize = particleEndSize;
        particle.lifetime = particleLifetime;
        particle.timeRemaining = particleLifetime;

        // Add to active particles
        activeParticles.add(particle);
    }

    /**
     * Get an inactive particle from the pool
     */
    private Particle getParticleFromPool() {
        for (Particle particle : particles) {
            if (!particle.active) {
                return particle;
            }
        }
        return null; // No inactive particles available
    }

    /**
     * Update all active particles
     */
    private void updateParticles(float deltaTime) {
        Iterator<Particle> iterator = activeParticles.iterator();

        while (iterator.hasNext()) {
            Particle particle = iterator.next();

            // Update lifetime
            particle.timeRemaining -= deltaTime;

            // Check if particle should die
            if (particle.timeRemaining <= 0) {
                particle.active = false;
                iterator.remove();
                continue;
            }

            // Calculate life progress (0 to 1)
            float lifeProgress = 1.0f - (particle.timeRemaining / particle.lifetime);

            // Update color
            particle.color.x = lerp(particle.startColor.x, particle.endColor.x, lifeProgress);
            particle.color.y = lerp(particle.startColor.y, particle.endColor.y, lifeProgress);
            particle.color.z = lerp(particle.startColor.z, particle.endColor.z, lifeProgress);
            particle.color.w = lerp(particle.startColor.w, particle.endColor.w, lifeProgress);

            // Update size
            particle.size = lerp(particle.startSize, particle.endSize, lifeProgress);

            // Apply physics
            particle.velocity.x -= particle.velocity.x * damping * deltaTime;
            particle.velocity.y -= particle.velocity.y * damping * deltaTime;
            particle.velocity.y += gravity.y * deltaTime;
            particle.velocity.x += gravity.x * deltaTime;

            // Update position
            particle.position.x += particle.velocity.x * deltaTime;
            particle.position.y += particle.velocity.y * deltaTime;
        }
    }

    /**
     * Linear interpolation helper
     */
    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Override
    public void render(RenderSystem renderSystem, Matrix4f viewProjectionMatrix) {
        if (activeParticles.isEmpty() || particleTexture == null) {
            return;
        }

        // Bind particle texture
        particleTexture.bind(0);

        // Set up blending
        glEnable(GL_BLEND);
        switch (blendMode) {
            case ALPHA:
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                break;
            case ADDITIVE:
                glBlendFunc(GL_SRC_ALPHA, GL_ONE);
                break;
            case MULTIPLY:
                glBlendFunc(GL_DST_COLOR, GL_ZERO);
                break;
        }

        // Bind VAO
        glBindVertexArray(vao);

        // Calculate camera-facing quads
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(activeParticles.size() * 4 * 9); // 4 vertices per quad, 9 floats per vertex

        for (Particle particle : activeParticles) {
            float halfSize = particle.size * 0.5f;

            // Calculate corners of the quad
            float x1 = particle.position.x - halfSize;
            float y1 = particle.position.y - halfSize;
            float x2 = particle.position.x + halfSize;
            float y2 = particle.position.y + halfSize;

            // Add vertices for the quad

            // Bottom-left
            vertexBuffer.put(x1).put(y1).put(particle.position.z);
            vertexBuffer.put(particle.color.x).put(particle.color.y).put(particle.color.z).put(particle.color.w);
            vertexBuffer.put(0.0f).put(0.0f);

            // Bottom-right
            vertexBuffer.put(x2).put(y1).put(particle.position.z);
            vertexBuffer.put(particle.color.x).put(particle.color.y).put(particle.color.z).put(particle.color.w);
            vertexBuffer.put(1.0f).put(0.0f);

            // Top-right
            vertexBuffer.put(x2).put(y2).put(particle.position.z);
            vertexBuffer.put(particle.color.x).put(particle.color.y).put(particle.color.z).put(particle.color.w);
            vertexBuffer.put(1.0f).put(1.0f);

            // Top-left
            vertexBuffer.put(x1).put(y2).put(particle.position.z);
            vertexBuffer.put(particle.color.x).put(particle.color.y).put(particle.color.z).put(particle.color.w);
            vertexBuffer.put(0.0f).put(1.0f);
        }

        vertexBuffer.flip();

        // Update buffer data
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);

        // Set view projection matrix
        // In a real implementation, you would set the shader uniform here

        // Draw particles as quads
        for (int i = 0; i < activeParticles.size(); i++) {
            glDrawArrays(GL_TRIANGLE_FAN, i * 4, 4);
        }

        // Unbind VAO
        glBindVertexArray(0);

        // Reset blend mode
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    protected void onDestroy() {
        // Clean up OpenGL resources
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }

    /**
     * Play the particle system
     */
    public void play() {
        playing = true;
    }

    /**
     * Stop the particle system
     */
    public void stop() {
        playing = false;

        // Clear active particles
        for (Particle particle : activeParticles) {
            particle.active = false;
        }
        activeParticles.clear();
    }

    /**
     * Pause the particle system
     */
    public void pause() {
        playing = false;
    }

    /**
     * Resume the particle system
     */
    public void resume() {
        playing = true;
    }

    /**
     * Restart the particle system
     */
    public void restart() {
        stop();
        timer = 0;
        play();
    }

    /**
     * Emit a burst of particles
     */
    public void emit(int count) {
        for (int i = 0; i < count; i++) {
            emitParticle();
        }
    }

    // Getters and setters for all properties

    public int getMaxParticles() {
        return maxParticles;
    }

    public void setMaxParticles(int maxParticles) {
        this.maxParticles = maxParticles;
        initParticlePool();
    }

    public float getEmissionRate() {
        return emissionRate;
    }

    public void setEmissionRate(float emissionRate) {
        this.emissionRate = emissionRate;
    }

    public EmissionShape getEmissionShape() {
        return emissionShape;
    }

    public void setEmissionShape(EmissionShape emissionShape) {
        this.emissionShape = emissionShape;
    }

    public float getEmissionRadius() {
        return emissionRadius;
    }

    public void setEmissionRadius(float emissionRadius) {
        this.emissionRadius = emissionRadius;
    }

    public Vector2f getEmissionBox() {
        return new Vector2f(emissionBox);
    }

    public void setEmissionBox(float width, float height) {
        this.emissionBox.set(width, height);
    }

    public Vector2f getEmissionDirection() {
        return new Vector2f(emissionDirection);
    }

    public void setEmissionDirection(float x, float y) {
        this.emissionDirection.set(x, y).normalize();
    }

    public float getEmissionAngle() {
        return emissionAngle;
    }

    public void setEmissionAngle(float emissionAngle) {
        this.emissionAngle = emissionAngle;
    }

    public float getEmissionForce() {
        return emissionForce;
    }

    public void setEmissionForce(float emissionForce) {
        this.emissionForce = emissionForce;
    }

    public float getEmissionForceVariance() {
        return emissionForceVariance;
    }

    public void setEmissionForceVariance(float emissionForceVariance) {
        this.emissionForceVariance = Math.max(0, Math.min(1, emissionForceVariance));
    }

    public Vector4f getStartColor() {
        return new Vector4f(startColor);
    }

    public void setStartColor(float r, float g, float b, float a) {
        this.startColor.set(r, g, b, a);
    }

    public Vector4f getEndColor() {
        return new Vector4f(endColor);
    }

    public void setEndColor(float r, float g, float b, float a) {
        this.endColor.set(r, g, b, a);
    }

    public float getStartSize() {
        return startSize;
    }

    public void setStartSize(float startSize) {
        this.startSize = startSize;
    }

    public float getEndSize() {
        return endSize;
    }

    public void setEndSize(float endSize) {
        this.endSize = endSize;
    }

    public float getSizeVariance() {
        return sizeVariance;
    }

    public void setSizeVariance(float sizeVariance) {
        this.sizeVariance = Math.max(0, Math.min(1, sizeVariance));
    }

    public float getLifetime() {
        return lifetime;
    }

    public void setLifetime(float lifetime) {
        this.lifetime = lifetime;
    }

    public float getLifetimeVariance() {
        return lifetimeVariance;
    }

    public void setLifetimeVariance(float lifetimeVariance) {
        this.lifetimeVariance = Math.max(0, Math.min(1, lifetimeVariance));
    }

    public Vector2f getGravity() {
        return new Vector2f(gravity);
    }

    public void setGravity(float x, float y) {
        this.gravity.set(x, y);
    }

    public float getDamping() {
        return damping;
    }

    public void setDamping(float damping) {
        this.damping = Math.max(0, Math.min(1, damping));
    }

    public Texture getParticleTexture() {
        return particleTexture;
    }

    public void setParticleTexture(Texture particleTexture) {
        this.particleTexture = particleTexture;
    }

    public int getZIndex() {
        return zIndex;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public BlendMode getBlendMode() {
        return blendMode;
    }

    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public int getActiveParticleCount() {
        return activeParticles.size();
    }

    @Override
    public float getZ() {
        return zIndex;
    }

    @Override
    public float getWidth() {
        return emissionRadius * 2;
    }

    @Override
    public float getHeight() {
        return emissionRadius * 2;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    /**
     * Internal particle class
     */
    private static class Particle {
        boolean active = false;
        Vector3f position = new Vector3f();
        Vector2f velocity = new Vector2f();
        Vector4f color = new Vector4f();
        Vector4f startColor = new Vector4f();
        Vector4f endColor = new Vector4f();
        float size = 1.0f;
        float startSize = 1.0f;
        float endSize = 1.0f;
        float lifetime = 1.0f;
        float timeRemaining = 1.0f;
    }
}