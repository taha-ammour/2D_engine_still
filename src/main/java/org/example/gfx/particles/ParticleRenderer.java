// src/main/java/org/example/gfx/particles/ParticleRenderer.java
package org.example.gfx.particles;

import org.example.gfx.Material;
import org.example.gfx.Renderer2D;

/**
 * Efficient batch renderer for particles
 */
public final class ParticleRenderer {
    private final Renderer2D renderer;
    private final ParticleSystem particleSystem;

    public ParticleRenderer(Renderer2D renderer, ParticleSystem particleSystem) {
        this.renderer = renderer;
        this.particleSystem = particleSystem;
    }

    /**
     * Render all active particles
     */
    public void render() {
        for (Particle p : particleSystem.getActiveParticles()) {
            renderParticle(p);
        }
    }

    private void renderParticle(Particle p) {
        Material mat = Material.builder()
                .shader("sprite")
                .tint(p.color)
                .build();

        renderer.drawQuad(
                p.position.x - p.size * 0.5f,
                p.position.y - p.size * 0.5f,
                p.size,
                p.size,
                p.rotation,
                mat,
                new float[]{1, 1, 0, 0},
                1f,
                1f
        );
    }
}