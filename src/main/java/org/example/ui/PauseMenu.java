// src/main/java/org/example/ui/PauseMenu.java
package org.example.ui;

import org.example.gfx.BitmapFont;
import org.example.gfx.Renderer2D;

/**
 * Pause menu with resume, restart, and quit options
 * Uses centered screen-relative positioning
 */
public final class PauseMenu {
    private final UIPanel menuPanel;
    private final UIManager uiManager;
    private boolean visible = false;

    private Runnable onResume;
    private Runnable onRestart;
    private Runnable onQuit;

    public PauseMenu(BitmapFont font, int screenWidth, int screenHeight) {
        this.uiManager = new UIManager(screenWidth, screenHeight);

        // ===== MENU PANEL (Centered) =====
        float menuWidth = 400f;
        float menuHeight = 350f;

        menuPanel = new UIPanel(0, 0, menuWidth, menuHeight);
        menuPanel.setPositionMode(UIComponent.PositionMode.ANCHORED);
        menuPanel.setAnchor(UIComponent.Anchor.CENTER);
        menuPanel.setPivot(0.5f, 0.5f);
        menuPanel.setBackgroundColor(0.05f, 0.05f, 0.1f, 0.95f);
        menuPanel.setBorderColor(0.5f, 0.7f, 1f, 1f);
        menuPanel.setBorderWidth(3f);
        menuPanel.setZOrder(1000); // Very high z-order (always on top)

        // ===== TITLE (Relative to panel) =====
        UIText titleText = new UIText(font, "PAUSED", 0, 0, 4f);
        titleText.setPositionMode(UIComponent.PositionMode.RELATIVE_TO_PARENT);
        titleText.setPosition(0.5f, 0.85f);
        titleText.setPivot(0.5f, 0.5f);
        titleText.setTextColor(0.8f, 0.9f, 1f, 1f);
        titleText.setFontStyle(BitmapFont.FontStyle.CURSIVE);
        menuPanel.addChild(titleText);

        // ===== BUTTONS (Relative to panel center) =====
        float buttonWidth = 300f;
        float buttonHeight = 50f;
        float buttonSpacing = 60f;

        // Resume Button
        UIButton resumeBtn = createButton(font, "RESUME",
                0.5f, 0.55f, buttonWidth, buttonHeight);
        resumeBtn.setNormalColor(0.2f, 0.4f, 0.6f, 0.9f);
        resumeBtn.setHoverColor(0.3f, 0.5f, 0.8f, 1f);
        resumeBtn.setOnClick(() -> {
            if (onResume != null) onResume.run();
            hide();
        });
        menuPanel.addChild(resumeBtn);

        // Restart Button
        UIButton restartBtn = createButton(font, "RESTART",
                0.5f, 0.4f, buttonWidth, buttonHeight);
        restartBtn.setNormalColor(0.6f, 0.4f, 0.2f, 0.9f);
        restartBtn.setHoverColor(0.8f, 0.5f, 0.3f, 1f);
        restartBtn.setOnClick(() -> {
            if (onRestart != null) onRestart.run();
            hide();
        });
        menuPanel.addChild(restartBtn);

        // Quit Button
        UIButton quitBtn = createButton(font, "QUIT",
                0.5f, 0.25f, buttonWidth, buttonHeight);
        quitBtn.setNormalColor(0.6f, 0.2f, 0.2f, 0.9f);
        quitBtn.setHoverColor(0.8f, 0.3f, 0.3f, 1f);
        quitBtn.setOnClick(() -> {
            if (onQuit != null) onQuit.run();
        });
        menuPanel.addChild(quitBtn);

        // ===== CONTROLS HINT (Relative to panel bottom) =====
        UIText hint = new UIText(font, "PRESS ESC TO RESUME", 0, 0, 1.5f);
        hint.setPositionMode(UIComponent.PositionMode.RELATIVE_TO_PARENT);
        hint.setPosition(0.5f, 0.1f);
        hint.setPivot(0.5f, 0.5f);
        hint.setTextColor(0.7f, 0.7f, 0.8f, 0.8f);
        menuPanel.addChild(hint);

        uiManager.addLayer(menuPanel);
        menuPanel.setVisible(false);
    }

    /**
     * Helper to create buttons with relative positioning
     */
    private UIButton createButton(BitmapFont font, String text,
                                  float relX, float relY, float width, float height) {
        UIButton button = new UIButton(font, text, 0, 0, width, height);
        button.setPositionMode(UIComponent.PositionMode.RELATIVE_TO_PARENT);
        button.setPosition(relX, relY);
        button.setPivot(0.5f, 0.5f);
        return button;
    }

    public void show() {
        visible = true;
        menuPanel.setVisible(true);
    }

    public void hide() {
        visible = false;
        menuPanel.setVisible(false);
    }

    public void toggle() {
        if (visible) {
            hide();
        } else {
            show();
        }
    }

    public void update(double dt) {
        if (visible) {
            uiManager.update(dt);
        }
    }

    public void render(Renderer2D renderer) {
        if (visible) {
            uiManager.render(renderer);
        }
    }

    /**
     * Call when screen resizes
     */
    public void onScreenResize(int width, int height) {
        uiManager.setScreenDimensions(width, height);
    }

    // ===== MOUSE INPUT FORWARDING =====

    public void onMouseMove(float x, float y) {
        if (visible) {
            uiManager.onMouseMove(x, y);
        }
    }

    public void onMousePress(float x, float y) {
        if (visible) {
            uiManager.onMousePress(x, y);
        }
    }

    public void onMouseRelease(float x, float y) {
        if (visible) {
            uiManager.onMouseRelease(x, y);
        }
    }

    // ===== CALLBACKS =====

    public void setOnResume(Runnable callback) {
        this.onResume = callback;
    }

    public void setOnRestart(Runnable callback) {
        this.onRestart = callback;
    }

    public void setOnQuit(Runnable callback) {
        this.onQuit = callback;
    }

    // ===== GETTERS =====

    public boolean isVisible() { return visible; }
    public UIManager getUIManager() { return uiManager; }
}