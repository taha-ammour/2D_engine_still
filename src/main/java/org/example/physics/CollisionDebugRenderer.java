// src/main/java/org/example/physics/CollisionDebugRenderer.java
package org.example.physics;

import org.example.gfx.Material;
import org.example.gfx.Renderer2D;
import org.joml.Vector4f;

import java.util.List;

/**
 * Debug utility to visualize collision boxes
 * Toggle on/off to see what's colliding
 */
public final class CollisionDebugRenderer {
    private final Renderer2D renderer;
    private boolean enabled = false;

    public CollisionDebugRenderer(Renderer2D renderer) {
        this.renderer = renderer;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
    }

    /**
     * Render all collision boxes
     * Call this after rendering game objects
     */
    public void render(List<BoxCollider> colliders) {
        if (!enabled) return;

        for (BoxCollider col : colliders) {
            if (!col.isEnabled()) continue;

            float x = col.getMinX();
            float y = col.getMinY();
            float w = col.getMaxX() - col.getMinX();
            float h = col.getMaxY() - col.getMinY();

            // Choose color based on layer
            Vector4f color = getColorForLayer(col.layer);

            // Make triggers semi-transparent
            if (col.isTrigger) {
                color.w = 0.3f;
            }

            // Draw outline
            drawBox(x, y, w, h, color);
        }
    }

    private void drawBox(float x, float y, float w, float h, Vector4f color) {
        float lineWidth = 2f;

        // Top
        drawLine(x, y + h, x + w, y + h, lineWidth, color);
        // Bottom
        drawLine(x, y, x + w, y, lineWidth, color);
        // Left
        drawLine(x, y, x, y + h, lineWidth, color);
        // Right
        drawLine(x + w, y, x + w, y + h, lineWidth, color);
    }

    private void drawLine(float x1, float y1, float x2, float y2, float width, Vector4f color) {
        // Calculate line as thin quad
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float)Math.sqrt(dx * dx + dy * dy);
        float angle = (float)Math.atan2(dy, dx);

        Material mat = Material.builder()
                .tint(color)
                .build();

        renderer.drawQuad(
                x1, y1,
                length, width,
                angle,
                mat,
                new float[]{1, 1, 0, 0},
                1f, 1f
        );
    }

    private Vector4f getColorForLayer(CollisionLayer layer) {
        return switch (layer) {
            case PLAYER -> new Vector4f(0.0f, 1.0f, 0.0f, 0.7f);      // Green
            case ENEMY -> new Vector4f(1.0f, 0.0f, 0.0f, 0.7f);       // Red
            case GROUND -> new Vector4f(0.5f, 0.5f, 1.0f, 0.7f);      // Blue
            case PLATFORM -> new Vector4f(0.6f, 0.4f, 0.2f, 0.7f);    // Brown
            case PROJECTILE -> new Vector4f(1.0f, 1.0f, 0.0f, 0.7f);  // Yellow
            case TRIGGER -> new Vector4f(1.0f, 0.0f, 1.0f, 0.3f);     // Magenta
            case ITEM -> new Vector4f(0.0f, 1.0f, 1.0f, 0.7f);        // Cyan
            default -> new Vector4f(1.0f, 1.0f, 1.0f, 0.5f);          // White
        };
    }
}

// ============================================
// USAGE IN PLAYSCENE:
// ============================================

/*
// Add to PlayScene fields:
private CollisionDebugRenderer debugRenderer;

// In onLoad():
debugRenderer = new CollisionDebugRenderer(renderer);

// In handleInput() - toggle with F3:
inputManager.bindKeyPress(GLFW_KEY_F3, dt -> debugRenderer.toggle());

// In render() - after rendering game objects:
if (debugRenderer.isEnabled()) {
    debugRenderer.render(collisionSystem.getAllColliders());
}

// Add to CollisionSystem:
public List<BoxCollider> getAllColliders() {
    return new ArrayList<>(colliders);
}
*/