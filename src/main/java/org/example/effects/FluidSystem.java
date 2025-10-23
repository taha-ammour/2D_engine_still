// src/main/java/org/example/effects/FluidSystem.java
package org.example.effects;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluid Simulation using SPH (Smoothed Particle Hydrodynamics)
 * Creates realistic water, lava, and liquid effects
 *
 * Perfect for:
 * - Water splashes
 * - Lava flows
 * - Slime/goo effects
 * - Blood splatters
 * - Rain effects
 */
public final class FluidSystem {
    private final List<FluidParticle> particles = new ArrayList<>();
    private final int maxParticles;

    // SPH parameters
    private static final float SMOOTHING_RADIUS = 16f;
    private static final float REST_DENSITY = 1f;
    private static final float GAS_CONSTANT = 2000f;
    private static final float GRAVITY = -400f;
    private static final float COLLISION_DAMPING = 0.5f;

    // World bounds (particles bounce off these)
    private float minX = 0f, maxX = 1280f;
    private float minY = 0f, maxY = 720f;

    public FluidSystem(int maxParticles) {
        this.maxParticles = maxParticles;

        // Pre-allocate particles
        for (int i = 0; i < maxParticles; i++) {
            particles.add(new FluidParticle());
        }
    }

    /**
     * Emit a fluid burst (water splash, lava splatter, etc.)
     */
    public void emitFluid(float x, float y, int count, FluidConfig config) {
        for (int i = 0; i < count && i < maxParticles; i++) {
            FluidParticle p = getInactiveParticle();
            if (p == null) break;

            // Random position in small area
            float offsetX = (float)(Math.random() - 0.5f) * config.spreadRadius;
            float offsetY = (float)(Math.random() - 0.5f) * config.spreadRadius;
            p.position.set(x + offsetX, y + offsetY);

            // Random velocity
            float angle = (float)(Math.random() * Math.PI * 2);
            float speed = config.minSpeed + (float)Math.random() * (config.maxSpeed - config.minSpeed);
            p.velocity.set(
                    (float)Math.cos(angle) * speed,
                    (float)Math.sin(angle) * speed
            );

            p.color.set(config.color);
            p.viscosity = config.viscosity;
            p.lifetime = config.lifetime;
            p.mass = config.mass;
            p.size = config.size;
            p.age = 0f;
            p.active = true;
        }
    }

    /**
     * Update fluid simulation
     */
    public void update(float dt) {
        // Step 1: Calculate density and pressure for each particle
        for (FluidParticle pi : particles) {
            if (!pi.active) continue;

            pi.density = 0f;

            // Sum up density from nearby particles
            for (FluidParticle pj : particles) {
                if (!pj.active || pi == pj) continue;

                float dx = pj.position.x - pi.position.x;
                float dy = pj.position.y - pi.position.y;
                float distSq = dx * dx + dy * dy;

                if (distSq < SMOOTHING_RADIUS * SMOOTHING_RADIUS) {
                    float dist = (float)Math.sqrt(distSq);
                    pi.density += pj.mass * smoothingKernel(dist);
                }
            }

            // Calculate pressure from density
            pi.pressure = GAS_CONSTANT * (pi.density - REST_DENSITY);
        }

        // Step 2: Calculate forces (pressure, viscosity, external)
        for (FluidParticle pi : particles) {
            if (!pi.active) continue;

            pi.force.set(0, 0);

            // Gravity
            pi.force.y += GRAVITY * pi.mass;

            // Pressure and viscosity forces from neighbors
            for (FluidParticle pj : particles) {
                if (!pj.active || pi == pj) continue;

                float dx = pj.position.x - pi.position.x;
                float dy = pj.position.y - pi.position.y;
                float distSq = dx * dx + dy * dy;

                if (distSq < SMOOTHING_RADIUS * SMOOTHING_RADIUS && distSq > 0.001f) {
                    float dist = (float)Math.sqrt(distSq);
                    float nx = dx / dist;
                    float ny = dy / dist;

                    // Pressure force
                    float pressureForce = -pj.mass * (pi.pressure + pj.pressure) /
                            (2f * pj.density) * smoothingKernelGradient(dist);
                    pi.force.x += pressureForce * nx;
                    pi.force.y += pressureForce * ny;

                    // Viscosity force (makes fluid stick together)
                    float visc = 2f * pj.mass * smoothingKernelLaplacian(dist) / pj.density;
                    pi.force.x += visc * (pj.velocity.x - pi.velocity.x);
                    pi.force.y += visc * (pj.velocity.y - pi.velocity.y);
                }
            }
        }

        // Step 3: Integrate (update velocities and positions)
        for (FluidParticle p : particles) {
            if (!p.active) continue;

            p.update(dt);

            // Boundary collisions
            if (p.position.x < minX) {
                p.position.x = minX;
                p.velocity.x *= -COLLISION_DAMPING;
            }
            if (p.position.x > maxX) {
                p.position.x = maxX;
                p.velocity.x *= -COLLISION_DAMPING;
            }
            if (p.position.y < minY) {
                p.position.y = minY;
                p.velocity.y *= -COLLISION_DAMPING;
            }
            if (p.position.y > maxY) {
                p.position.y = maxY;
                p.velocity.y *= -COLLISION_DAMPING;
            }
        }
    }

    // SPH kernel functions
    private float smoothingKernel(float r) {
        if (r >= SMOOTHING_RADIUS) return 0f;
        float volume = (float)(Math.PI * Math.pow(SMOOTHING_RADIUS, 4) / 6f);
        return Math.max(0, (SMOOTHING_RADIUS * SMOOTHING_RADIUS - r * r) / volume);
    }

    private float smoothingKernelGradient(float r) {
        if (r >= SMOOTHING_RADIUS) return 0f;
        float volume = (float)(Math.PI * Math.pow(SMOOTHING_RADIUS, 4) / 6f);
        return -r / volume;
    }

    private float smoothingKernelLaplacian(float r) {
        if (r >= SMOOTHING_RADIUS) return 0f;
        float volume = (float)(Math.PI * Math.pow(SMOOTHING_RADIUS, 4) / 6f);
        return -1f / volume;
    }

    private FluidParticle getInactiveParticle() {
        for (FluidParticle p : particles) {
            if (!p.active || p.isDead()) {
                p.reset();
                return p;
            }
        }
        return null;
    }

    public void setBounds(float minX, float minY, float maxX, float maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public List<FluidParticle> getActiveParticles() {
        List<FluidParticle> active = new ArrayList<>();
        for (FluidParticle p : particles) {
            if (p.active && !p.isDead()) {
                active.add(p);
            }
        }
        return active;
    }

    public void clear() {
        for (FluidParticle p : particles) {
            p.active = false;
        }
    }

    public int getActiveCount() {
        int count = 0;
        for (FluidParticle p : particles) {
            if (p.active && !p.isDead()) count++;
        }
        return count;
    }
}