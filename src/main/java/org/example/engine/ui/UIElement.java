package org.example.engine.ui;

import org.example.engine.core.Component;
import org.example.engine.input.InputSystem;
import org.example.engine.rendering.Renderable;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all UI elements.
 * Provides common functionality for positioning, sizing and interactivity.
 */
public abstract class UIElement extends Component implements Renderable {
    // Element properties
    protected float x = 0;
    protected float y = 0;
    protected float width = 100;
    protected float height = 50;
    protected boolean visible = true;
    protected boolean interactive = true;
    protected UIAnchor anchor = UIAnchor.TOP_LEFT;
    protected int zIndex = 0;

    // Parent-child relationship
    protected UIElement parent;
    protected final List<UIElement> children = new ArrayList<>();

    // Interaction state
    protected boolean hovered = false;
    protected boolean pressed = false;
    protected boolean focused = false;

    // Event callbacks
    protected UIEventCallback onClickCallback;
    protected UIEventCallback onHoverCallback;
    protected UIEventCallback onHoverExitCallback;
    protected UIEventCallback onPressCallback;
    protected UIEventCallback onReleaseCallback;

    /**
     * Create a UI element with default size and position
     */
    public UIElement() {
    }

    /**
     * Create a UI element with specified position and size
     */
    public UIElement(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    protected void onInit() {
        super.onInit();
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // Update interaction state based on input
        if (interactive && visible) {
            updateInteraction();
        }

        // Update children
        for (UIElement child : children) {
            if (child.isActive()) {
                child.onUpdate(deltaTime);
            }
        }
    }

    /**
     * Update interaction state (hover, pressed, etc.)
     */
    protected void updateInteraction() {
        InputSystem input = InputSystem.getInstance();
        double mouseX = input.getMouseX();
        double mouseY = input.getMouseY();

        // Calculate global position
        Vector2f globalPos = getGlobalPosition();

        // Check if mouse is over this element
        boolean wasHovered = hovered;
        hovered = (mouseX >= globalPos.x && mouseX <= globalPos.x + width &&
                mouseY >= globalPos.y && mouseY <= globalPos.y + height);

        // Handle hover enter/exit events
        if (hovered && !wasHovered) {
            onHover();
        } else if (!hovered && wasHovered) {
            onHoverExit();
        }

        // Handle mouse press/release
        boolean mouseDown = input.isMouseButtonDown(0); // Left mouse button

        if (hovered && input.isMouseButtonPressed(0)) {
            pressed = true;
            onPress();
        } else if (pressed && input.isMouseButtonReleased(0)) {
            pressed = false;
            onRelease();

            // Click event occurs when release happens while hovering
            if (hovered) {
                onClick();
            }
        }
    }

    /**
     * Add a child UI element
     */
    public void addChild(UIElement child) {
        if (child == null || children.contains(child) || child == this) return;

        // Remove from previous parent
        if (child.parent != null) {
            child.parent.children.remove(child);
        }

        // Add to children
        children.add(child);
        child.parent = this;
    }

    /**
     * Remove a child UI element
     */
    public boolean removeChild(UIElement child) {
        if (children.remove(child)) {
            child.parent = null;
            return true;
        }
        return false;
    }

    /**
     * Get the local position (relative to parent)
     */
    public Vector2f getLocalPosition() {
        return new Vector2f(x, y);
    }

    /**
     * Calculate global position (screen coordinates)
     */
    public Vector2f getGlobalPosition() {
        float globalX = x;
        float globalY = y;

        // Apply parent transform
        if (parent != null) {
            Vector2f parentPos = parent.getGlobalPosition();
            globalX += parentPos.x;
            globalY += parentPos.y;
        }

        // Apply anchoring
        applyAnchorOffset(globalX, globalY);

        return new Vector2f(globalX, globalY);
    }

    /**
     * Apply anchor offset to position
     */
    private Vector2f applyAnchorOffset(float x, float y) {
        Vector2f result = new Vector2f(x, y);

        // Get parent width/height or screen size
        float parentWidth = 0;
        float parentHeight = 0;

        if (parent != null) {
            parentWidth = parent.width;
            parentHeight = parent.height;
        } else {
            // No parent, use screen size
            // This assumes you have a way to get screen dimensions
            // You'll need to adapt this to your engine's specifics
            parentWidth = 800; // Default fallback
            parentHeight = 600; // Default fallback
        }

        // Apply horizontal anchor
        switch (anchor) {
            case TOP_CENTER:
            case CENTER:
            case BOTTOM_CENTER:
                result.x = x + (parentWidth - width) / 2;
                break;
            case TOP_RIGHT:
            case RIGHT:
            case BOTTOM_RIGHT:
                result.x = x + (parentWidth - width);
                break;
            default:
                // Left-aligned, no change needed
                break;
        }

        // Apply vertical anchor
        switch (anchor) {
            case LEFT:
            case CENTER:
            case RIGHT:
                result.y = y + (parentHeight - height) / 2;
                break;
            case BOTTOM_LEFT:
            case BOTTOM_CENTER:
            case BOTTOM_RIGHT:
                result.y = y + (parentHeight - height);
                break;
            default:
                // Top-aligned, no change needed
                break;
        }

        return result;
    }

    /**
     * Set the position
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Set the size
     */
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Set the anchor
     */
    public void setAnchor(UIAnchor anchor) {
        this.anchor = anchor;
    }

    /**
     * Set the Z index (rendering order)
     */
    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    /**
     * Set visibility
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Set interactivity
     */
    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    /**
     * Check if this element is visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Check if this element is interactive
     */
    public boolean isInteractive() {
        return interactive;
    }

    /**
     * Check if the mouse is currently hovering over this element
     */
    public boolean isHovered() {
        return hovered;
    }

    /**
     * Check if this element is currently pressed
     */
    public boolean isPressed() {
        return pressed;
    }

    /**
     * Check if this element has keyboard focus
     */
    public boolean isFocused() {
        return focused;
    }

    /**
     * Set keyboard focus to this element
     */
    public void setFocus(boolean focus) {
        // Remove focus from other elements
        UISystem.getInstance().setFocusedElement(focus ? this : null);

        this.focused = focus;
    }

    /**
     * Event handler for click
     */
    protected void onClick() {
        if (onClickCallback != null) {
            onClickCallback.onEvent(this);
        }
    }

    /**
     * Event handler for hover
     */
    protected void onHover() {
        if (onHoverCallback != null) {
            onHoverCallback.onEvent(this);
        }
    }

    /**
     * Event handler for hover exit
     */
    protected void onHoverExit() {
        if (onHoverExitCallback != null) {
            onHoverExitCallback.onEvent(this);
        }
    }

    /**
     * Event handler for press
     */
    protected void onPress() {
        if (onPressCallback != null) {
            onPressCallback.onEvent(this);
        }
    }

    /**
     * Event handler for release
     */
    protected void onRelease() {
        if (onReleaseCallback != null) {
            onReleaseCallback.onEvent(this);
        }
    }

    /**
     * Set click callback
     */
    public void setOnClickCallback(UIEventCallback callback) {
        this.onClickCallback = callback;
    }

    /**
     * Set hover callback
     */
    public void setOnHoverCallback(UIEventCallback callback) {
        this.onHoverCallback = callback;
    }

    /**
     * Set hover exit callback
     */
    public void setOnHoverExitCallback(UIEventCallback callback) {
        this.onHoverExitCallback = callback;
    }

    /**
     * Set press callback
     */
    public void setOnPressCallback(UIEventCallback callback) {
        this.onPressCallback = callback;
    }

    /**
     * Set release callback
     */
    public void setOnReleaseCallback(UIEventCallback callback) {
        this.onReleaseCallback = callback;
    }

    // Implement Renderable interface
    @Override
    public float getZ() {
        return zIndex;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public boolean isTransparent() {
        return true; // UI elements typically support transparency
    }

    /**
     * Callback interface for UI events
     */
    public interface UIEventCallback {
        void onEvent(UIElement element);
    }
}