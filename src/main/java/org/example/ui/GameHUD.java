// src/main/java/org/example/ui/GameHUD.java
package org.example.ui;

import org.example.gfx.BitmapFont;
import org.example.gfx.Renderer2D;

/**
 * Example HUD for the game
 * Shows health, score, coins, time, etc.
 * Uses screen-relative positioning for responsive layout
 */
public final class GameHUD {
    private final UIManager uiManager;
    private final BitmapFont font;

    // HUD Elements
    private UIPanel topBar;
    private UIBar healthBar;
    private UIText scoreText;
    private UIText coinsText;
    private UIText timeText;
    private UIText livesText;

    // Game state
    private int score = 0;
    private int coins = 0;
    private int lives = 3;
    private float time = 300f; // 5 minutes
    private float health = 100f;

    public GameHUD(BitmapFont font, int screenWidth, int screenHeight) {
        this.font = font;
        this.uiManager = new UIManager(screenWidth, screenHeight);

        createHUD();
    }

    private void createHUD() {
        // ===== TOP BAR (Screen-relative) =====
        float barHeight = 60f;
        topBar = new UIPanel(0, 0, 0, barHeight);
        topBar.setPositionMode(UIComponent.PositionMode.ANCHORED);
        topBar.setAnchor(UIComponent.Anchor.TOP_LEFT);
        topBar.setSize(uiManager.getScreenWidth(), barHeight);
        topBar.setBackgroundColor(0.05f, 0.05f, 0.1f, 0.85f);
        topBar.setBorderColor(0.3f, 0.5f, 0.8f, 1f);
        topBar.setBorderWidth(2f);
        topBar.setZOrder(100); // Always on top
        uiManager.addLayer(topBar);

        float padding = 10f;
        float yPos = padding;

        // ===== HEALTH BAR (Absolute positioning within topBar) =====
        healthBar = new UIBar(font, padding, yPos, 200, 40, 100f);
        healthBar.setPositionMode(UIComponent.PositionMode.ABSOLUTE);
        healthBar.setFillColor(0.2f, 0.9f, 0.2f, 1f);
        healthBar.setCriticalColor(0.95f, 0.2f, 0.2f, 1f);
        healthBar.setCriticalThreshold(0.3f);
        healthBar.setTextScale(1.5f);
        topBar.addChild(healthBar);

        // ===== SCORE (Absolute) =====
        float scoreX = padding + 220f;
        scoreText = new UIText(font, "SCORE: 00000", scoreX, yPos + 5, 2f);
        scoreText.setPositionMode(UIComponent.PositionMode.ABSOLUTE);
        scoreText.setTextColor(1f, 1f, 0.3f, 1f);
        topBar.addChild(scoreText);

        // ===== COINS (Absolute) =====
        float coinsX = scoreX + 200f;
        coinsText = new UIText(font, "COINS: 00", coinsX, yPos + 5, 2f);
        coinsText.setPositionMode(UIComponent.PositionMode.ABSOLUTE);
        coinsText.setTextColor(1f, 0.85f, 0.2f, 1f);
        topBar.addChild(coinsText);

        // ===== TIME (Anchored to top-right) =====
        timeText = new UIText(font, "TIME: 300", 0, 0, 2f);
        timeText.setPositionMode(UIComponent.PositionMode.ANCHORED);
        timeText.setAnchor(UIComponent.Anchor.TOP_RIGHT);
        timeText.setPosition(-180, 10);
        timeText.setTextColor(0.3f, 0.8f, 1f, 1f);
        timeText.setZOrder(101);
        uiManager.addComponent(timeText);

        // ===== LIVES (Anchored to bottom-left) =====
        livesText = new UIText(font, "LIVES: 3", 0, 0, 2.5f);
        livesText.setPositionMode(UIComponent.PositionMode.ANCHORED);
        livesText.setAnchor(UIComponent.Anchor.BOTTOM_LEFT);
        livesText.setPosition(10, -10);
        livesText.setTextColor(1f, 0.3f, 0.3f, 1f);
        livesText.setDrawBackground(true);
        livesText.setBackgroundColor(0.1f, 0.1f, 0.15f, 0.7f);
        livesText.setPadding(8f);
        livesText.setZOrder(100);
        uiManager.addComponent(livesText);
    }

    /**
     * Update HUD
     */
    public void update(double dt) {
        // Update time
        if (time > 0) {
            time -= (float)dt;
            if (time < 0) time = 0;
        }

        // Update UI components
        healthBar.setValue(health);
        scoreText.setText(String.format("SCORE: %05d", score));
        coinsText.setText(String.format("COINS: %02d", coins));
        timeText.setText(String.format("TIME: %d", (int)time));
        livesText.setText(String.format("LIVES: %d", lives));

        // Change time color when running out
        if (time < 30f) {
            float pulse = 0.5f + 0.5f * (float)Math.sin(time * 10f);
            timeText.setTextColor(1f, pulse * 0.3f, pulse * 0.3f, 1f);
        } else {
            timeText.setTextColor(0.3f, 0.8f, 1f, 1f);
        }

        uiManager.update(dt);
    }

    /**
     * Render HUD
     */
    public void render(Renderer2D renderer) {
        uiManager.render(renderer);
    }

    /**
     * Call when screen resizes
     */
    public void onScreenResize(int width, int height) {
        uiManager.setScreenDimensions(width, height);
        topBar.setSize(width, topBar.getSize().y);
    }

    // ===== GAME STATE SETTERS =====

    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(100, health));
    }

    public void addHealth(float amount) {
        setHealth(health + amount);
    }

    public void setScore(int score) {
        this.score = Math.max(0, score);
    }

    public void addScore(int points) {
        setScore(score + points);
    }

    public void setCoins(int coins) {
        this.coins = Math.max(0, coins);
    }

    public void addCoin() {
        coins++;
        addScore(10);
    }

    public void setLives(int lives) {
        this.lives = Math.max(0, lives);
    }

    public void addLife() {
        lives++;
    }

    public void loseLife() {
        lives--;
    }

    public void setTime(float time) {
        this.time = Math.max(0, time);
    }

    public void addTime(float seconds) {
        this.time += seconds;
    }

    // ===== GETTERS =====

    public float getHealth() { return health; }
    public int getScore() { return score; }
    public int getCoins() { return coins; }
    public int getLives() { return lives; }
    public float getTime() { return time; }
    public UIManager getUIManager() { return uiManager; }
}