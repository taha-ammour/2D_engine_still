// src/main/java/org/example/gfx/particles/ParticleSystem.java
package org.example.gfx.particles;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all particles in the game
 * Handles spawning, updating, and recycling
 */
public final class ParticleSystem {
    private final List<Particle> activeParticles = new ArrayList<>();
    private final List<Particle> particlePool = new ArrayList<>();

    private static final int INITIAL_POOL_SIZE = 1000;
    private static final int MAX_PARTICLES = 5000;

    public ParticleSystem() {
        // Pre-allocate particle pool
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            particlePool.add(new Particle(0, 0));
        }
    }

    /**
     * Spawn a new particle
     */
    public Particle spawn(float x, float y) {
        if (activeParticles.size() >= MAX_PARTICLES) {
            return null; // Too many particles
        }

        Particle particle;
        if (!particlePool.isEmpty()) {
            // Reuse from pool
            particle = particlePool.remove(particlePool.size() - 1);
            particle.position.set(x, y);
            particle.velocity.set(0, 0);
            particle.age = 0f;
            particle.rotation = 0f;
            particle.color.set(1, 1, 1, 1);
            particle.size = 4f;
            particle.lifetime = 1f;
        } else {
            // Create new particle
            particle = new Particle(x, y);
        }

        activeParticles.add(particle);
        return particle;
    }

    /**
     * Emit a burst of particles
     */
    public void emitBurst(float x, float y, int count, ParticleConfig config) {
        for (int i = 0; i < count; i++) {
            Particle p = spawn(x, y);
            if (p != null) {
                config.apply(p);
            }
        }
    }

    /**
     * Update all particles
     */
    public void update(float dt) {
        for (int i = activeParticles.size() - 1; i >= 0; i--) {
            Particle p = activeParticles.get(i);
            p.update(dt);

            if (p.isDead()) {
                // Return to pool
                activeParticles.remove(i);
                particlePool.add(p);
            }
        }
    }

    public List<Particle> getActiveParticles() {
        return activeParticles;
    }

    public int getActiveCount() {
        return activeParticles.size();
    }

    public void clear() {
        particlePool.addAll(activeParticles);
        activeParticles.clear();
    }
}