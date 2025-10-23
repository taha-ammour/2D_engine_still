// src/main/java/org/example/effects/TrailRenderer.java
package org.example.effects;

import org.example.gfx.Material;
import org.example.gfx.Renderer2D;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates smooth motion trails behind fast-moving objects
 * Perfect for dashing, super-speed, projectiles
 */
public final class TrailRenderer {
    private final Renderer2D renderer;
    private final List<TrailSegment> trails = new ArrayList<>();

    public TrailRenderer(Renderer2D renderer) {
        this.renderer = renderer;
    }

    /**
     * Create a new trail for an object
     */
    public Trail createTrail(TrailConfig config) {
        Trail trail = new Trail(config);
        return trail;
    }

    /**
     * Update all trails
     */
    public void update(float dt) {
        trails.removeIf(segment -> segment.age >= segment.lifetime);

        for (TrailSegment segment : trails) {
            segment.age += dt;
        }
    }

    /**
     * Render all trails
     */
    public void render() {
        for (TrailSegment segment : trails) {
            float alpha = 1f - (segment.age / segment.lifetime);

            Material mat = Material.builder()
                    .shader("sprite")
                    .tint(
                            segment.color.x,
                            segment.color.y,
                            segment.color.z,
                            segment.color.w * alpha
                    )
                    .build();

            renderer.drawQuad(
                    segment.position.x - segment.width * 0.5f,
                    segment.position.y - segment.height * 0.5f,
                    segment.width,
                    segment.height,
                    0f,
                    mat,
                    new float[]{1, 1, 0, 0},
                    1f,
                    1f
            );
        }
    }

    /**
     * Individual trail that follows an object
     */
    public final class Trail {
        private final TrailConfig config;
        private float emissionTimer = 0f;

        Trail(TrailConfig config) {
            this.config = config;
        }

        /**
         * Update trail position (call every frame)
         */
        public void update(float x, float y, float dt) {
            emissionTimer += dt;

            if (emissionTimer >= config.emissionInterval) {
                emissionTimer = 0f;

                TrailSegment segment = new TrailSegment();
                segment.position.set(x, y);
                segment.color.set(config.color);
                segment.width = config.width;
                segment.height = config.height;
                segment.lifetime = config.lifetime;

                trails.add(segment);

                // Limit trail length
                if (trails.size() > config.maxSegments) {
                    trails.remove(0);
                }
            }
        }

        /**
         * Clear this trail's segments
         */
        public void clear() {
            trails.clear();
        }
    }

    private static class TrailSegment {
        Vector2f position = new Vector2f();
        Vector4f color = new Vector4f();
        float width = 10f;
        float height = 10f;
        float lifetime = 0.5f;
        float age = 0f;
    }
}
