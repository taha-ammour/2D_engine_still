// src/main/java/org/example/ui/UIButton.java
package org.example.ui;

import org.example.gfx.BitmapFont;
import org.example.gfx.Renderer2D;
import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * Interactive button UI component
 * Observer Pattern: Callbacks for click events
 */
public final class UIButton extends UIComponent {
    private final BitmapFont font;
    private final UIText label;

    // Colors for different states
    private final Vector4f normalColor = new Vector4f(0.2f, 0.2f, 0.3f, 0.9f);
    private final Vector4f hoverColor = new Vector4f(0.3f, 0.3f, 0.5f, 0.95f);
    private final Vector4f pressedColor = new Vector4f(0.15f, 0.15f, 0.25f, 1f);
    private final Vector4f disabledColor = new Vector4f(0.1f, 0.1f, 0.1f, 0.5f);

    private boolean hovered = false;
    private boolean pressed = false;
    private Runnable onClick;

    public UIButton(BitmapFont font, String text, float x, float y, float width, float height) {
        super(x, y, width, height);
        this.font = font;

        // Create centered label
        float labelScale = 2f;
        this.label = new UIText(font, text, 0, 0, labelScale);
        this.label.setTextColor(1, 1, 1, 1);
        this.label.setParent(this);
        this.label.setPositionMode(PositionMode.RELATIVE_TO_PARENT);
        this.label.setPosition(0.5f, 0.5f);
        this.label.setPivot(0.5f, 0.5f);

        // Default style
        backgroundColor.set(normalColor);
        borderColor.set(1, 1, 1, 1);
        borderWidth = 2f;
    }

    @Override
    protected void onUpdate(double dt) {
        if (!enabled) {
            hovered = false;
            pressed = false;
        }

        // Update label color based on state
        if (!enabled) {
            label.setTextColor(0.5f, 0.5f, 0.5f, 0.7f);
        } else if (pressed) {
            label.setTextColor(1f, 1f, 0.8f, 1f);
        } else if (hovered) {
            label.setTextColor(1f, 1f, 1f, 1f);
        } else {
            label.setTextColor(0.9f, 0.9f, 0.9f, 1f);
        }

        label.update(dt);
    }

    @Override
    protected void onRender(Renderer2D renderer) {
        Vector2f renderPos = getRenderPosition();

        // Determine background color based on state
        Vector4f bgColor;
        if (!enabled) {
            bgColor = disabledColor;
        } else if (pressed) {
            bgColor = pressedColor;
        } else if (hovered) {
            bgColor = hoverColor;
        } else {
            bgColor = normalColor;
        }

        // Draw background
        drawRect(renderer, renderPos.x, renderPos.y, size.x, size.y, bgColor);

        // Draw border
        drawBorder(renderer, renderPos.x, renderPos.y, size.x, size.y);

        // Draw label
        label.render(renderer);
    }

    @Override
    protected void onClickInternal(float mouseX, float mouseY) {
        if (onClick != null && enabled) {
            onClick.run();
        }
    }

    /**
     * Update hover state
     */
    public void onMouseMove(float mouseX, float mouseY) {
        if (!visible || !enabled) {
            hovered = false;
            return;
        }

        hovered = isPointInside(mouseX, mouseY);
    }

    /**
     * Handle mouse press
     */
    public void onMousePress(float mouseX, float mouseY) {
        if (!visible || !enabled) return;

        if (isPointInside(mouseX, mouseY)) {
            pressed = true;
        }
    }

    /**
     * Handle mouse release
     */
    public void onMouseRelease(float mouseX, float mouseY) {
        if (pressed && isPointInside(mouseX, mouseY)) {
            onClickInternal(mouseX, mouseY);
        }
        pressed = false;
    }

    // ===== SETTERS =====

    public void setOnClick(Runnable callback) {
        this.onClick = callback;
    }

    public void setText(String text) {
        label.setText(text);
    }

    public void setNormalColor(float r, float g, float b, float a) {
        normalColor.set(r, g, b, a);
    }

    public void setHoverColor(float r, float g, float b, float a) {
        hoverColor.set(r, g, b, a);
    }

    public void setPressedColor(float r, float g, float b, float a) {
        pressedColor.set(r, g, b, a);
    }

    public void setDisabledColor(float r, float g, float b, float a) {
        disabledColor.set(r, g, b, a);
    }

    @Override
    public void setScreenDimensions(int width, int height) {
        super.setScreenDimensions(width, height);
        label.setScreenDimensions(width, height);
    }

    // ===== GETTERS =====

    public boolean isHovered() { return hovered; }
    public boolean isPressed() { return pressed; }
    public String getText() { return label.getText(); }
}