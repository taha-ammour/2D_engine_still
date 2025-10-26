// src/main/java/org/example/ui/UIComponent.java
package org.example.ui;

import org.example.gfx.Renderer2D;
import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * Base class for all UI components
 *
 * SOLID Principles:
 * - Single Responsibility: Manages component positioning, visibility, and basic interaction
 * - Open/Closed: Open for extension (subclasses), closed for modification
 * - Liskov Substitution: All subclasses can be used interchangeably
 * - Dependency Inversion: Depends on Renderer2D abstraction
 */
public abstract class UIComponent {
    // Positioning
    protected final Vector2f position = new Vector2f();
    protected final Vector2f size = new Vector2f();
    protected final Vector2f pivot = new Vector2f(0, 0); // 0,0 = top-left, 0.5,0.5 = center, 1,1 = bottom-right

    // Layout
    protected PositionMode positionMode = PositionMode.ABSOLUTE;
    protected Anchor anchor = Anchor.TOP_LEFT;
    protected final Vector2f margin = new Vector2f(0, 0);
    protected float padding = 4f;

    // Parent-child hierarchy
    protected UIComponent parent;
    protected int screenWidth;
    protected int screenHeight;

    // Visual properties
    protected final Vector4f backgroundColor = new Vector4f(0, 0, 0, 0);
    protected final Vector4f borderColor = new Vector4f(1, 1, 1, 1);
    protected float borderWidth = 0f;

    // State
    protected boolean visible = true;
    protected boolean enabled = true;
    protected int zOrder = 0;

    /**
     * Position modes for flexible layouts
     */
    public enum PositionMode {
        ABSOLUTE,           // x, y are absolute pixel coordinates
        RELATIVE_TO_PARENT, // x, y are percentages of parent size (0-1)
        RELATIVE_TO_SCREEN, // x, y are percentages of screen size (0-1)
        ANCHORED            // Position relative to anchor point
    }

    /**
     * Anchor points for screen-relative positioning
     */
    public enum Anchor {
        TOP_LEFT(0, 1),
        TOP_CENTER(0.5f, 1),
        TOP_RIGHT(1, 1),
        CENTER_LEFT(0, 0.5f),
        CENTER(0.5f, 0.5f),
        CENTER_RIGHT(1, 0.5f),
        BOTTOM_LEFT(0, 0),
        BOTTOM_CENTER(0.5f, 0),
        BOTTOM_RIGHT(1, 0);

        public final float x, y;

        Anchor(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public UIComponent(float x, float y, float width, float height) {
        position.set(x, y);
        size.set(width, height);
    }

    /**
     * Update UI logic - Template Method Pattern
     */
    public void update(double dt) {
        if (!visible) return;
        onUpdate(dt);
    }

    /**
     * Render the component - Template Method Pattern
     */
    public void render(Renderer2D renderer) {
        if (!visible) return;
        onRender(renderer);
    }

    /**
     * Template methods for subclasses to override
     */
    protected abstract void onUpdate(double dt);
    protected abstract void onRender(Renderer2D renderer);

    /**
     * Handle mouse click - Chain of Responsibility Pattern
     */
    public boolean onClick(float mouseX, float mouseY) {
        if (!visible || !enabled) return false;

        if (isPointInside(mouseX, mouseY)) {
            onClickInternal(mouseX, mouseY);
            return true;
        }
        return false;
    }

    protected abstract void onClickInternal(float mouseX, float mouseY);

    /**
     * Check if point is inside component bounds
     */
    public boolean isPointInside(float x, float y) {
        Vector2f worldPos = getWorldPosition();
        Vector2f pivotOffset = getPivotOffset();

        float minX = worldPos.x - pivotOffset.x;
        float minY = worldPos.y - pivotOffset.y;
        float maxX = minX + size.x;
        float maxY = minY + size.y;

        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    /**
     * Get world position (absolute screen coordinates)
     * This is where the magic happens - converts all position modes to screen space
     */
    public Vector2f getWorldPosition() {
        Vector2f worldPos = new Vector2f();

        switch (positionMode) {
            case ABSOLUTE:
                // Direct pixel coordinates
                if (parent != null) {
                    Vector2f parentPos = parent.getWorldPosition();
                    worldPos.set(parentPos).add(position);
                } else {
                    worldPos.set(position);
                }
                break;

            case RELATIVE_TO_PARENT:
                // Position as percentage of parent size
                if (parent != null) {
                    Vector2f parentPos = parent.getWorldPosition();
                    Vector2f parentSize = parent.getSize();
                    worldPos.x = parentPos.x + position.x * parentSize.x;
                    worldPos.y = parentPos.y + position.y * parentSize.y;
                } else {
                    worldPos.set(position);
                }
                break;

            case RELATIVE_TO_SCREEN:
                // Position as percentage of screen size
                worldPos.x = position.x * screenWidth;
                worldPos.y = position.y * screenHeight;
                break;

            case ANCHORED:
                // Position relative to anchor point
                float anchorX = anchor.x * screenWidth;
                float anchorY = anchor.y * screenHeight;
                worldPos.x = anchorX + position.x + margin.x;
                worldPos.y = anchorY + position.y + margin.y;
                break;
        }

        return worldPos;
    }

    /**
     * Get pivot offset for rendering
     */
    protected Vector2f getPivotOffset() {
        return new Vector2f(size.x * pivot.x, size.y * pivot.y);
    }

    /**
     * Get render position (world position adjusted for pivot)
     */
    public Vector2f getRenderPosition() {
        Vector2f worldPos = getWorldPosition();
        Vector2f pivotOffset = getPivotOffset();
        return new Vector2f(worldPos.x - pivotOffset.x, worldPos.y - pivotOffset.y);
    }

    /**
     * Set screen dimensions (call when window resizes)
     */
    public void setScreenDimensions(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    // ===== POSITION SETTERS =====

    public void setPosition(float x, float y) {
        position.set(x, y);
    }

    public void setPositionMode(PositionMode mode) {
        this.positionMode = mode;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    public void setPivot(float x, float y) {
        pivot.set(x, y);
    }

    public void setMargin(float x, float y) {
        margin.set(x, y);
    }

    // ===== VISUAL SETTERS =====

    public void setSize(float width, float height) {
        size.set(width, height);
    }

    public void setBackgroundColor(float r, float g, float b, float a) {
        backgroundColor.set(r, g, b, a);
    }

    public void setBorderColor(float r, float g, float b, float a) {
        borderColor.set(r, g, b, a);
    }

    public void setBorderWidth(float width) {
        this.borderWidth = width;
    }

    public void setPadding(float padding) {
        this.padding = padding;
    }

    // ===== STATE SETTERS =====

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setZOrder(int order) {
        this.zOrder = order;
    }

    public void setParent(UIComponent parent) {
        this.parent = parent;
    }

    // ===== GETTERS =====

    public boolean isVisible() { return visible; }
    public boolean isEnabled() { return enabled; }
    public Vector2f getPosition() { return new Vector2f(position); }
    public Vector2f getSize() { return new Vector2f(size); }
    public int getZOrder() { return zOrder; }
    public UIComponent getParent() { return parent; }

    /**
     * Helper method to draw a filled rectangle
     */
    protected void drawRect(Renderer2D renderer, float x, float y, float w, float h, Vector4f color) {
        if (color.w <= 0) return;

        org.example.gfx.Material mat = org.example.gfx.Material.builder()
                .tint(color)
                .build();

        renderer.drawQuad(x, y, w, h, 0f, mat, new float[]{1, 1, 0, 0}, 1f, 1f);
    }

    /**
     * Helper method to draw border
     */
    protected void drawBorder(Renderer2D renderer, float x, float y, float w, float h) {
        if (borderWidth <= 0 || borderColor.w <= 0) return;

        org.example.gfx.Material borderMat = org.example.gfx.Material.builder()
                .tint(borderColor)
                .build();

        float[] uv = new float[]{1, 1, 0, 0};

        // Top
        renderer.drawQuad(x, y + h - borderWidth, w, borderWidth, 0f, borderMat, uv, 1f, 1f);
        // Bottom
        renderer.drawQuad(x, y, w, borderWidth, 0f, borderMat, uv, 1f, 1f);
        // Left
        renderer.drawQuad(x, y, borderWidth, h, 0f, borderMat, uv, 1f, 1f);
        // Right
        renderer.drawQuad(x + w - borderWidth, y, borderWidth, h, 0f, borderMat, uv, 1f, 1f);
    }
}