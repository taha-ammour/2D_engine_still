// src/main/java/org/example/effects/FluidRenderer.java
package org.example.effects;

import org.example.gfx.Material;
import org.example.gfx.Renderer2D;

/**
 * Renders fluid particles as soft circles with blending
 */
public final class FluidRenderer {
    private final Renderer2D renderer;
    private final FluidSystem fluidSystem;

    public FluidRenderer(Renderer2D renderer, FluidSystem fluidSystem) {
        this.renderer = renderer;
        this.fluidSystem = fluidSystem;
    }

    /**
     * Render all active fluid particles
     */
    public void render() {
        var particles = fluidSystem.getActiveParticles();

        for (FluidParticle p : particles) {
            // Create material for this particle
            Material mat = Material.builder()
                    .shader("sprite")
                    .tint(p.color.x, p.color.y, p.color.z, p.color.w)
                    .build();

            // Render as soft circle
            renderer.drawQuad(
                    p.position.x - p.size * 0.5f,
                    p.position.y - p.size * 0.5f,
                    p.size,
                    p.size,
                    0f,
                    mat,
                    new float[]{1, 1, 0, 0},
                    1f,
                    1f
            );
        }
    }
}