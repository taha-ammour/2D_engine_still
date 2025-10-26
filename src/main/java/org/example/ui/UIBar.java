// src/main/java/org/example/ui/UIBar.java
package org.example.ui;

import org.example.gfx.BitmapFont;
import org.example.gfx.Renderer2D;
import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * Progress/Health bar UI component
 * State Pattern: Visual state changes based on value
 */
public final class UIBar extends UIComponent {
    private float currentValue;
    private float maxValue;
    private final Vector4f fillColor = new Vector4f(0.2f, 0.8f, 0.2f, 1f);
    private final Vector4f emptyColor = new Vector4f(0.2f, 0.2f, 0.2f, 0.8f);
    private final Vector4f criticalColor = new Vector4f(0.9f, 0.2f, 0.2f, 1f);

    private float criticalThreshold = 0.25f;
    private boolean showText = true;
    private boolean animated = true;
    private float displayValue; // For smooth animation
    private final BitmapFont font;
    private float textScale = 1.5f;

    public enum BarStyle {
        HORIZONTAL,
        VERTICAL
    }

    private BarStyle style = BarStyle.HORIZONTAL;

    public UIBar(BitmapFont font, float x, float y, float width, float height, float maxValue) {
        super(x, y, width, height);
        this.font = font;
        this.maxValue = maxValue;
        this.currentValue = maxValue;
        this.displayValue = maxValue;

        backgroundColor.set(emptyColor);
        borderColor.set(1, 1, 1, 1);
        borderWidth = 2f;
    }

    @Override
    protected void onUpdate(double dt) {
        // Smooth animation towards current value
        if (animated) {
            float diff = currentValue - displayValue;
            float speed = 5f;
            displayValue += diff * speed * (float)dt;

            if (Math.abs(diff) < 0.01f) {
                displayValue = currentValue;
            }
        } else {
            displayValue = currentValue;
        }
    }

    @Override
    protected void onRender(Renderer2D renderer) {
        Vector2f renderPos = getRenderPosition();

        // Draw background
        drawRect(renderer, renderPos.x, renderPos.y, size.x, size.y, emptyColor);

        // Calculate fill percentage
        float fillPercent = Math.max(0, Math.min(1, displayValue / maxValue));

        // Determine fill color
        Vector4f currentFillColor = (fillPercent <= criticalThreshold) ?
                criticalColor : fillColor;

        // Draw fill
        if (fillPercent > 0) {
            if (style == BarStyle.HORIZONTAL) {
                float fillWidth = (size.x - borderWidth * 2) * fillPercent;
                drawRect(renderer,
                        renderPos.x + borderWidth,
                        renderPos.y + borderWidth,
                        fillWidth,
                        size.y - borderWidth * 2,
                        currentFillColor);
            } else {
                float fillHeight = (size.y - borderWidth * 2) * fillPercent;
                drawRect(renderer,
                        renderPos.x + borderWidth,
                        renderPos.y + borderWidth,
                        size.x - borderWidth * 2,
                        fillHeight,
                        currentFillColor);
            }
        }

        // Draw border
        drawBorder(renderer, renderPos.x, renderPos.y, size.x, size.y);

        // Draw text
        if (showText && font != null) {
            String text = String.format("%d/%d", (int)currentValue, (int)maxValue);
            float textWidth = font.measureText(text, textScale);
            float textHeight = font.getHeight(textScale);

            float textX = renderPos.x + (size.x - textWidth) / 2f;
            float textY = renderPos.y + (size.y - textHeight) / 2f;

            Vector4f textColor = new Vector4f(1, 1, 1, 1);
            font.drawText(renderer, text, textX, textY, textScale,
                    textColor, BitmapFont.FontStyle.NORMAL);
        }
    }

    @Override
    protected void onClickInternal(float mouseX, float mouseY) {
        // Bars don't handle clicks by default
    }

    // ===== VALUE MANAGEMENT =====

    public void setValue(float value) {
        this.currentValue = Math.max(0, Math.min(maxValue, value));
    }

    public void addValue(float amount) {
        setValue(currentValue + amount);
    }

    public void setMaxValue(float max) {
        this.maxValue = max;
        this.currentValue = Math.min(currentValue, max);
    }

    // ===== GETTERS =====

    public float getValue() { return currentValue; }
    public float getMaxValue() { return maxValue; }
    public float getPercent() { return currentValue / maxValue; }

    // ===== STYLE SETTERS =====

    public void setFillColor(float r, float g, float b, float a) {
        fillColor.set(r, g, b, a);
    }

    public void setCriticalColor(float r, float g, float b, float a) {
        criticalColor.set(r, g, b, a);
    }

    public void setEmptyColor(float r, float g, float b, float a) {
        emptyColor.set(r, g, b, a);
    }

    public void setCriticalThreshold(float threshold) {
        this.criticalThreshold = Math.max(0, Math.min(1, threshold));
    }

    public void setShowText(boolean show) {
        this.showText = show;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    public void setStyle(BarStyle style) {
        this.style = style;
    }

    public void setTextScale(float scale) {
        this.textScale = scale;
    }
}