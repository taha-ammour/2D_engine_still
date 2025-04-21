package org.example.engine;

import org.example.engine.rendering.Sprite;
import org.example.engine.rendering.Texture;
import org.example.engine.resource.ResourceManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages sprite sheets and sprite definitions.
 * Allows loading sprite sheets and defining individual sprites within them.
 */
public class SpriteManager {
    private static SpriteManager instance;

    // Maps sheet names to textures
    private final Map<String, Texture> spriteSheets = new HashMap<>();

    // Maps sprite IDs and names to sprite definitions
    private final Map<Integer, SpriteDefinition> spritesById = new HashMap<>();
    private final Map<String, SpriteDefinition> spritesByName = new HashMap<>();

    // Resource manager for loading textures
    private final ResourceManager resourceManager;

    /**
     * Private constructor for singleton pattern
     */
    private SpriteManager() {
        resourceManager = ResourceManager.getInstance();
    }

    /**
     * Get the singleton instance
     */
    public static SpriteManager getInstance() {
        if (instance == null) {
            instance = new SpriteManager();
        }
        return instance;
    }

    /**
     * Load a sprite sheet texture
     */
    public void loadSpriteSheet(String name, String path) {
        Texture texture = resourceManager.loadTexture(name, path);
        spriteSheets.put(name, texture);
    }

    /**
     * Define a sprite within a sprite sheet
     */
    public void defineSprite(int id, String name, String sheetName,
                             int x, int y, int width, int height, String[] palette) {
        // Get the sprite sheet
        Texture sheet = spriteSheets.get(sheetName);
        if (sheet == null) {
            throw new IllegalArgumentException("Sprite sheet not found: " + sheetName);
        }

        // Calculate normalized UV coordinates
        float sheetWidth = sheet.getWidth();
        float sheetHeight = sheet.getHeight();

        float u0 = x / sheetWidth;
        float v0 = y / sheetHeight;
        float u1 = (x + width) / sheetWidth;
        float v1 = (y + height) / sheetHeight;

        // Create sprite definition
        SpriteDefinition definition = new SpriteDefinition(
                id, name, sheet, x, y, width, height, u0, v0, u1, v1, palette
        );

        // Store the definition
        spritesById.put(id, definition);
        spritesByName.put(name.toLowerCase(), definition);
    }

    /**
     * Get a sprite definition by ID
     */
    public SpriteDefinition getSprite(int id) {
        return spritesById.get(id);
    }

    /**
     * Get a sprite definition by name
     */
    public SpriteDefinition getSprite(String name) {
        return spritesByName.get(name.toLowerCase());
    }

    /**
     * Create a Sprite component from a sprite definition
     */
    public Sprite createSprite(int id) {
        SpriteDefinition def = getSprite(id);
        if (def == null) {
            return null;
        }

        // Create a new sprite with the definition's texture and dimensions
        Sprite sprite = new Sprite(
                def.getTexture(), def.getU0(), def.getV0(), def.getU1(), def.getV1(),
                def.getWidth(), def.getHeight()
        );

        // Set the palette if available
        if (def.getPalette() != null) {
            sprite.setPaletteFromCodes(def.getPalette());
        }

        return sprite;
    }

    /**
     * Create a Sprite component from a sprite definition by name
     */
    public Sprite createSprite(String name) {
        SpriteDefinition def = getSprite(name);
        if (def == null) {
            return null;
        }

        // Create a new sprite with the definition's texture and dimensions
        Sprite sprite = new Sprite(
                def.getTexture(), def.getU0(), def.getV0(), def.getU1(), def.getV1(),
                def.getWidth(), def.getHeight()
        );

        // Set the palette if available
        if (def.getPalette() != null) {
            sprite.setPaletteFromCodes(def.getPalette());
        }

        return sprite;
    }

    /**
     * Inner class to hold sprite definition data
     */
    public static class SpriteDefinition {
        private final int id;
        private final String name;
        private final Texture texture;
        private final int x, y, width, height;
        private final float u0, v0, u1, v1;
        private final String[] palette;

        public SpriteDefinition(int id, String name, Texture texture,
                                int x, int y, int width, int height,
                                float u0, float v0, float u1, float v1,
                                String[] palette) {
            this.id = id;
            this.name = name;
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            this.palette = palette;
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public Texture getTexture() { return texture; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public float getU0() { return u0; }
        public float getV0() { return v0; }
        public float getU1() { return u1; }
        public float getV1() { return v1; }
        public String[] getPalette() { return palette; }
    }
}