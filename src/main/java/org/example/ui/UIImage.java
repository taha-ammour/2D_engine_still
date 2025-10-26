// src/main/java/org/example/ui/UIImage.java
package org.example.ui;

import org.example.gfx.Material;
import org.example.gfx.Renderer2D;
import org.example.gfx.Texture;
import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * UI component for displaying images/textures
 *
 * Supports different scaling modes:
 * - STRETCH: Stretches to fill the component size
 * - FIT: Maintains aspect ratio, fits inside bounds
 * - FILL: Maintains aspect ratio, fills bounds (may crop)
 * - NATIVE: Uses texture's original size
 */
public final class UIImage extends UIComponent {
    private Texture texture;
    private final Vector4f tint = new Vector4f(1, 1, 1, 1);
    private ScaleMode scaleMode = ScaleMode.STRETCH;
    private float rotation = 0f;

    public enum ScaleMode {
        STRETCH,  // Stretch to fill size
        FIT,      // Fit inside, maintain aspect ratio
        FILL,     // Fill bounds, maintain aspect ratio (may crop)
        NATIVE    // Use texture's native size
    }

    public UIImage(Texture texture, float x, float y, float width, float height) {
        super(x, y, width, height);
        this.texture = texture;
    }

    public UIImage(Texture texture, float x, float y) {
        super(x, y, texture != null ? texture.width() : 64, texture != null ? texture.height() : 64);
        this.texture = texture;
        this.scaleMode = ScaleMode.NATIVE;
    }

    @Override
    protected void onUpdate(double dt) {
        // Images don't need updates
    }

    @Override
    protected void onRender(Renderer2D renderer) {
        if (texture == null) {
            // Draw placeholder (magenta)
            drawRect(renderer, getRenderPosition().x, getRenderPosition().y,
                    size.x, size.y, new Vector4f(1, 0, 1, 0.5f));
            return;
        }

        Vector2f renderPos = getRenderPosition();
        Vector2f renderSize = calculateRenderSize();

        // Center within bounds if using FIT mode
        Vector2f offset = new Vector2f(0, 0);
        if (scaleMode == ScaleMode.FIT) {
            offset.x = (size.x - renderSize.x) / 2f;
            offset.y = (size.y - renderSize.y) / 2f;
        }

        Material mat = Material.builder()
                .texture(texture)
                .tint(tint)
                .build();

        renderer.drawQuad(
                renderPos.x + offset.x,
                renderPos.y + offset.y,
                renderSize.x,
                renderSize.y,
                rotation,
                mat,
                new float[]{1, 1, 0, 0},
                1f / texture.width(),
                1f / texture.height()
        );

        // Draw border if needed
        drawBorder(renderer, renderPos.x, renderPos.y, size.x, size.y);
    }

    /**
     * Calculate render size based on scale mode
     */
    private Vector2f calculateRenderSize() {
        if (texture == null) return new Vector2f(size);

        switch (scaleMode) {
            case STRETCH:
                return new Vector2f(size);

            case FIT:
                // Fit inside bounds, maintain aspect ratio
                float texAspect = (float)texture.width() / texture.height();
                float boundsAspect = size.x / size.y;

                if (texAspect > boundsAspect) {
                    // Texture is wider - fit to width
                    return new Vector2f(size.x, size.x / texAspect);
                } else {
                    // Texture is taller - fit to height
                    return new Vector2f(size.y * texAspect, size.y);
                }

            case FILL:
                // Fill bounds, maintain aspect ratio
                float texAspect2 = (float)texture.width() / texture.height();
                float boundsAspect2 = size.x / size.y;

                if (texAspect2 > boundsAspect2) {
                    // Texture is wider - fit to height
                    return new Vector2f(size.y * texAspect2, size.y);
                } else {
                    // Texture is taller - fit to width
                    return new Vector2f(size.x, size.x / texAspect2);
                }

            case NATIVE:
                return new Vector2f(texture.width(), texture.height());

            default:
                return new Vector2f(size);
        }
    }

    @Override
    protected void onClickInternal(float mouseX, float mouseY) {
        // Images don't handle clicks by default
    }

    // ===== SETTERS =====

    public void setTexture(Texture texture) {
        this.texture = texture;
        if (scaleMode == ScaleMode.NATIVE && texture != null) {
            setSize(texture.width(), texture.height());
        }
    }

    public void setTint(float r, float g, float b, float a) {
        tint.set(r, g, b, a);
    }

    public void setTint(Vector4f color) {
        tint.set(color);
    }

    public void setScaleMode(ScaleMode mode) {
        this.scaleMode = mode;
    }

    public void setRotation(float radians) {
        this.rotation = radians;
    }

    // ===== GETTERS =====

    public Texture getTexture() { return texture; }
    public Vector4f getTint() { return new Vector4f(tint); }
    public ScaleMode getScaleMode() { return scaleMode; }
    public float getRotation() { return rotation; }
}