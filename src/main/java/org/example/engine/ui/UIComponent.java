package org.example.engine.ui;

import org.example.engine.rendering.Material;
import org.example.engine.rendering.RenderSystem;
import org.example.engine.rendering.Sprite;
import org.example.engine.rendering.Texture;
import org.example.engine.resource.ResourceManager;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Common UI component definitions
 */

/**
 * UI Anchor positions for elements
 */
enum UIAnchor {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    LEFT,
    CENTER,
    RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT
}

/**
 * A UI panel element (container with background)
 */
class UIPanel extends UIElement {
    private Sprite background;
    private Vector4f backgroundColor = new Vector4f(0.2f, 0.2f, 0.2f, 0.8f);
    private Texture backgroundTexture;
    private boolean useTexture = false;

    public UIPanel() {
        super();
        init();
    }

    public UIPanel(float x, float y, float width, float height) {
        super(x, y, width, height);
        init();
    }

    private void init() {
        // Create background sprite
        if (getGameObject() != null) {
            background = new Sprite(null, width, height);
            background.setColor(
                    (int)(backgroundColor.x * 255) << 16 |
                            (int)(backgroundColor.y * 255) << 8 |
                            (int)(backgroundColor.z * 255),
                    backgroundColor.w
            );
            getGameObject().addComponent(background);
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        super.onUpdate(deltaTime);

        // Update background size if changed
        if (background != null) {
            // TODO: Update background size when panel size changes
        }
    }

    @Override
    public void render(RenderSystem renderSystem, Matrix4f viewProjectionMatrix) {
        if (!visible) return;

        // Background should be rendered automatically via the GameObject hierarchy
    }

    /**
     * Set the background color
     */
    public void setBackgroundColor(float r, float g, float b, float a) {
        backgroundColor.set(r, g, b, a);

        if (background != null) {
            background.setColor(
                    (int)(r * 255) << 16 |
                            (int)(g * 255) << 8 |
                            (int)(b * 255),
                    a
            );
        }
    }

    /**
     * Set the background texture
     */
    public void setBackgroundTexture(Texture texture) {
        this.backgroundTexture = texture;
        this.useTexture = (texture != null);

        if (background != null) {
            background.setTexture(texture);
        }
    }

    @Override
    public Material getMaterial() {
        return background != null ? background.getMaterial() : null;
    }
}

/**
 * A UI button element
 */
class UIButton extends UIElement {
    private Sprite normalSprite;
    private Sprite hoverSprite;
    private Sprite pressedSprite;
    private Sprite disabledSprite;

    private Texture normalTexture;
    private Texture hoverTexture;
    private Texture pressedTexture;
    private Texture disabledTexture;

    private Vector4f normalColor = new Vector4f(0.3f, 0.3f, 0.8f, 1.0f);
    private Vector4f hoverColor = new Vector4f(0.4f, 0.4f, 0.9f, 1.0f);
    private Vector4f pressedColor = new Vector4f(0.2f, 0.2f, 0.7f, 1.0f);
    private Vector4f disabledColor = new Vector4f(0.5f, 0.5f, 0.5f, 0.5f);

    private UIText label;
    private String text = "Button";

    private boolean enabled = true;

    public UIButton() {
        super();
        init();
    }

    public UIButton(float x, float y, float width, float height) {
        super(x, y, width, height);
        init();
    }

    public UIButton(String text, float x, float y, float width, float height) {
        super(x, y, width, height);
        this.text = text;
        init();
    }

    private void init() {
        // Create sprites for different states
        if (getGameObject() != null) {
            normalSprite = new Sprite(null, width, height);
            normalSprite.setColor(
                    (int)(normalColor.x * 255) << 16 |
                            (int)(normalColor.y * 255) << 8 |
                            (int)(normalColor.z * 255),
                    normalColor.w
            );
            getGameObject().addComponent(normalSprite);

            // Create text label
            label = new UIText(text, 0, 0, width, height);
            label.setAlignment(UIText.Alignment.CENTER);
            label.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            addChild(label);
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        super.onUpdate(deltaTime);

        // Update visual state
        updateVisualState();
    }

    /**
     * Update the visual state based on interaction
     */
    private void updateVisualState() {
        if (!enabled) {
            // Disabled state
            if (normalSprite != null) {
                normalSprite.setColor(
                        (int)(disabledColor.x * 255) << 16 |
                                (int)(disabledColor.y * 255) << 8 |
                                (int)(disabledColor.z * 255),
                        disabledColor.w
                );

                if (disabledTexture != null) {
                    normalSprite.setTexture(disabledTexture);
                }
            }
        } else if (pressed) {
            // Pressed state
            if (normalSprite != null) {
                normalSprite.setColor(
                        (int)(pressedColor.x * 255) << 16 |
                                (int)(pressedColor.y * 255) << 8 |
                                (int)(pressedColor.z * 255),
                        pressedColor.w
                );

                if (pressedTexture != null) {
                    normalSprite.setTexture(pressedTexture);
                }
            }
        } else if (hovered) {
            // Hover state
            if (normalSprite != null) {
                normalSprite.setColor(
                        (int)(hoverColor.x * 255) << 16 |
                                (int)(hoverColor.y * 255) << 8 |
                                (int)(hoverColor.z * 255),
                        hoverColor.w
                );

                if (hoverTexture != null) {
                    normalSprite.setTexture(hoverTexture);
                }
            }
        } else {
            // Normal state
            if (normalSprite != null) {
                normalSprite.setColor(
                        (int)(normalColor.x * 255) << 16 |
                                (int)(normalColor.y * 255) << 8 |
                                (int)(normalColor.z * 255),
                        normalColor.w
                );

                if (normalTexture != null) {
                    normalSprite.setTexture(normalTexture);
                }
            }
        }
    }

    @Override
    public void render(RenderSystem renderSystem, Matrix4f viewProjectionMatrix) {
        if (!visible) return;

        // Sprites are rendered automatically via the GameObject hierarchy
    }

    /**
     * Set the button text
     */
    public void setText(String text) {
        this.text = text;
        if (label != null) {
            label.setText(text);
        }
    }

    /**
     * Get the button text
     */
    public String getText() {
        return text;
    }

    /**
     * Set whether the button is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.interactive = enabled;
    }

    /**
     * Check if the button is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set normal state color
     */
    public void setNormalColor(float r, float g, float b, float a) {
        normalColor.set(r, g, b, a);
    }

    /**
     * Set hover state color
     */
    public void setHoverColor(float r, float g, float b, float a) {
        hoverColor.set(r, g, b, a);
    }

    /**
     * Set pressed state color
     */
    public void setPressedColor(float r, float g, float b, float a) {
        pressedColor.set(r, g, b, a);
    }

    /**
     * Set disabled state color
     */
    public void setDisabledColor(float r, float g, float b, float a) {
        disabledColor.set(r, g, b, a);
    }

    /**
     * Set normal state texture
     */
    public void setNormalTexture(Texture texture) {
        this.normalTexture = texture;
    }

    /**
     * Set hover state texture
     */
    public void setHoverTexture(Texture texture) {
        this.hoverTexture = texture;
    }

    /**
     * Set pressed state texture
     */
    public void setPressedTexture(Texture texture) {
        this.pressedTexture = texture;
    }

    /**
     * Set disabled state texture
     */
    public void setDisabledTexture(Texture texture) {
        this.disabledTexture = texture;
    }

    @Override
    public Material getMaterial() {
        return normalSprite != null ? normalSprite.getMaterial() : null;
    }
}

/**
 * A UI text element for displaying text
 */
class UIText extends UIElement {
    // Text properties
    private String text = "";
    private float fontSize = 16.0f;
    private Vector4f textColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private Alignment alignment = Alignment.LEFT;

    // Sprite representing the text
    // In a real implementation, you'd use a proper text rendering system
    private Sprite textSprite;

    /**
     * Text alignment options
     */
    public enum Alignment {
        LEFT,
        CENTER,
        RIGHT
    }

    public UIText() {
        super();
        init();
    }

    public UIText(String text, float x, float y, float width, float height) {
        super(x, y, width, height);
        this.text = text;
        init();
    }

    private void init() {
        // In a full implementation, you would create a text renderer component
        // For now, we'll use a placeholder sprite
        if (getGameObject() != null) {
            textSprite = new Sprite(null, width, height);
            textSprite.setColor(
                    (int)(textColor.x * 255) << 16 |
                            (int)(textColor.y * 255) << 8 |
                            (int)(textColor.z * 255),
                    textColor.w
            );
            getGameObject().addComponent(textSprite);
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        super.onUpdate(deltaTime);

        // In a real implementation, you would update the text rendering here
    }

    @Override
    public void render(RenderSystem renderSystem, Matrix4f viewProjectionMatrix) {
        if (!visible) return;

        // In a real implementation, you would render the text here
        // Currently, the sprite is rendered automatically via the GameObject hierarchy
    }

    /**
     * Set the text to display
     */
    public void setText(String text) {
        this.text = text;
        // In a real implementation, you would update the text renderer
    }

    /**
     * Get the current text
     */
    public String getText() {
        return text;
    }

    /**
     * Set the font size
     */
    public void setFontSize(float fontSize) {
        this.fontSize = Math.max(1.0f, fontSize);
        // In a real implementation, you would update the text renderer
    }

    /**
     * Get the font size
     */
    public float getFontSize() {
        return fontSize;
    }

    /**
     * Set the text color
     */
    public void setColor(float r, float g, float b, float a) {
        textColor.set(r, g, b, a);

        if (textSprite != null) {
            textSprite.setColor(
                    (int)(r * 255) << 16 |
                            (int)(g * 255) << 8 |
                            (int)(b * 255),
                    a
            );
        }
    }

    /**
     * Get the text color
     */
    public Vector4f getColor() {
        return new Vector4f(textColor);
    }

    /**
     * Set the text alignment
     */
    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
        // In a real implementation, you would update the text renderer
    }

    /**
     * Get the text alignment
     */
    public Alignment getAlignment() {
        return alignment;
    }

    @Override
    public Material getMaterial() {
        return textSprite != null ? textSprite.getMaterial() : null;
    }
}