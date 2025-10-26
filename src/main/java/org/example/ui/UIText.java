// src/main/java/org/example/ui/UIText.java
package org.example.ui;

import org.example.gfx.BitmapFont;
import org.example.gfx.Renderer2D;
import org.joml.Vector2f;
import org.joml.Vector4f;

/**
 * UI component for displaying text
 * Single Responsibility: Renders text only
 */
public final class UIText extends UIComponent {
    private final BitmapFont font;
    private String text;
    private float scale;
    private final Vector4f textColor = new Vector4f(1, 1, 1, 1);
    private BitmapFont.FontStyle style = BitmapFont.FontStyle.NORMAL;
    private boolean drawBackground = false;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;
    private VerticalAlign verticalAlign = VerticalAlign.TOP;

    public enum HorizontalAlign {
        LEFT, CENTER, RIGHT
    }

    public enum VerticalAlign {
        TOP, CENTER, BOTTOM
    }

    public UIText(BitmapFont font, String text, float x, float y, float scale) {
        super(x, y, 0, 0);
        this.font = font;
        this.text = text;
        this.scale = scale;
        updateSize();
    }

    private void updateSize() {
        if (font != null && text != null) {
            size.x = font.measureText(text, scale);
            size.y = font.getHeight(scale);
        }
    }

    @Override
    protected void onUpdate(double dt) {
        // Text doesn't need updates
    }

    @Override
    protected void onRender(Renderer2D renderer) {
        if (font == null || text == null || text.isEmpty()) return;

        Vector2f renderPos = getRenderPosition();

        // Draw background if enabled
        if (drawBackground && backgroundColor.w > 0) {
            drawRect(renderer,
                    renderPos.x - padding,
                    renderPos.y - padding,
                    size.x + padding * 2,
                    size.y + padding * 2,
                    backgroundColor);
        }

        // Calculate aligned text position
        float textX = renderPos.x;
        float textY = renderPos.y;

        switch (horizontalAlign) {
            case CENTER:
                textX = renderPos.x + (size.x - font.measureText(text, scale)) / 2f;
                break;
            case RIGHT:
                textX = renderPos.x + size.x - font.measureText(text, scale);
                break;
        }

        switch (verticalAlign) {
            case CENTER:
                textY = renderPos.y + (size.y - font.getHeight(scale)) / 2f;
                break;
            case BOTTOM:
                textY = renderPos.y + size.y - font.getHeight(scale);
                break;
        }

        // Draw text
        font.drawText(renderer, text, textX, textY, scale, textColor, style);

        // Draw border if needed
        drawBorder(renderer, renderPos.x, renderPos.y, size.x, size.y);
    }

    @Override
    protected void onClickInternal(float mouseX, float mouseY) {
        // Text doesn't handle clicks by default
    }

    // ===== TEXT-SPECIFIC METHODS =====

    public void setText(String text) {
        this.text = text;
        updateSize();
    }

    public void setScale(float scale) {
        this.scale = scale;
        updateSize();
    }

    public void setTextColor(float r, float g, float b, float a) {
        textColor.set(r, g, b, a);
    }

    public void setTextColor(Vector4f color) {
        textColor.set(color);
    }

    public void setFontStyle(BitmapFont.FontStyle style) {
        this.style = style;
    }

    public void setDrawBackground(boolean draw) {
        this.drawBackground = draw;
    }

    public void setHorizontalAlign(HorizontalAlign align) {
        this.horizontalAlign = align;
    }

    public void setVerticalAlign(VerticalAlign align) {
        this.verticalAlign = align;
    }

    // ===== GETTERS =====

    public String getText() { return text; }
    public float getScale() { return scale; }
    public Vector4f getTextColor() { return new Vector4f(textColor); }
    public BitmapFont.FontStyle getFontStyle() { return style; }
}