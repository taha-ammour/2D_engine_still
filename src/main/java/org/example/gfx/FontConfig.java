// src/main/java/org/example/gfx/FontConfig.java
package org.example.gfx;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Font Configuration - OCP PATTERN
 *
 * This class allows fonts to be configured externally without modifying BitmapFont.
 * You can create different font configurations for different font atlases.
 *
 * Example:
 *   FontConfig config = FontConfig.builder()
 *       .texturePath("font.png")
 *       .glyphSize(8, 8)
 *       .atlasSize(16, 8)
 *       .addCharacterRange("ABCDEFGHIJKLMNOP", 0, FontStyle.NORMAL)
 *       .build();
 */
public final class FontConfig {
    private final String texturePath;
    private final int glyphWidth;
    private final int glyphHeight;
    private final int atlasColumns;
    private final int atlasRows;

    // Character mappings for each style
    private final Map<BitmapFont.FontStyle, Map<Character, Integer>> characterMaps;
    private final Map<BitmapFont.FontStyle, Integer> fallbackGlyphs;

    private FontConfig(Builder builder) {
        this.texturePath = builder.texturePath;
        this.glyphWidth = builder.glyphWidth;
        this.glyphHeight = builder.glyphHeight;
        this.atlasColumns = builder.atlasColumns;
        this.atlasRows = builder.atlasRows;
        this.characterMaps = builder.characterMaps;
        this.fallbackGlyphs = builder.fallbackGlyphs;
    }

    public String getTexturePath() { return texturePath; }
    public int getGlyphWidth() { return glyphWidth; }
    public int getGlyphHeight() { return glyphHeight; }
    public int getAtlasColumns() { return atlasColumns; }
    public int getAtlasRows() { return atlasRows; }

    /**
     * Get glyph index for a character in a specific style
     */
    public Integer getGlyphIndex(char c, BitmapFont.FontStyle style) {
        Map<Character, Integer> charMap = characterMaps.get(style);
        if (charMap == null) {
            // Fallback to NORMAL style if requested style not available
            charMap = characterMaps.get(BitmapFont.FontStyle.NORMAL);
        }

        if (charMap == null) return null;

        // Convert lowercase to uppercase for lookup
        char upperC = Character.toUpperCase(c);
        return charMap.get(upperC);
    }

    /**
     * Get fallback glyph (usually '?') for unknown characters
     */
    public Integer getFallbackGlyph(BitmapFont.FontStyle style) {
        Integer fallback = fallbackGlyphs.get(style);
        if (fallback == null) {
            fallback = fallbackGlyphs.get(BitmapFont.FontStyle.NORMAL);
        }
        return fallback;
    }

    /**
     * Get total number of characters mapped
     */
    public int getCharacterCount() {
        return characterMaps.values().stream()
                .mapToInt(Map::size)
                .sum();
    }

    // ===== BUILDER =====

    public static class Builder {
        private String texturePath;
        private int glyphWidth = 8;
        private int glyphHeight = 8;
        private int atlasColumns = 16;
        private int atlasRows = 8;
        private final Map<BitmapFont.FontStyle, Map<Character, Integer>> characterMaps = new EnumMap<>(BitmapFont.FontStyle.class);
        private final Map<BitmapFont.FontStyle, Integer> fallbackGlyphs = new EnumMap<>(BitmapFont.FontStyle.class);

        public Builder() {
            // Initialize maps for each style
            for (BitmapFont.FontStyle style : BitmapFont.FontStyle.values()) {
                characterMaps.put(style, new HashMap<>());
            }
        }

        public Builder texturePath(String path) {
            this.texturePath = path;
            return this;
        }

        public Builder glyphSize(int width, int height) {
            this.glyphWidth = width;
            this.glyphHeight = height;
            return this;
        }

        public Builder atlasSize(int cols, int rows) {
            this.atlasColumns = cols;
            this.atlasRows = rows;
            return this;
        }

        /**
         * Add a single character mapping
         */
        public Builder addCharacter(char c, int glyphIndex, BitmapFont.FontStyle... styles) {
            if (styles.length == 0) {
                // Default to NORMAL style
                styles = new BitmapFont.FontStyle[]{BitmapFont.FontStyle.NORMAL};
            }

            char upperC = Character.toUpperCase(c);
            for (BitmapFont.FontStyle style : styles) {
                characterMaps.get(style).put(upperC, glyphIndex);
            }
            return this;
        }

        /**
         * Add a range of characters starting from a specific index
         */
        public Builder addCharacterRange(String chars, int startIndex, BitmapFont.FontStyle... styles) {
            if (styles.length == 0) {
                styles = new BitmapFont.FontStyle[]{BitmapFont.FontStyle.NORMAL};
            }

            for (int i = 0; i < chars.length(); i++) {
                char c = chars.charAt(i);
                if (c != ' ' || i == 0) { // Map space only if it's first char
                    addCharacter(c, startIndex + i, styles);
                }
            }
            return this;
        }

        /**
         * Set fallback glyph (usually '?') for unknown characters
         */
        public Builder setFallbackGlyph(char c, BitmapFont.FontStyle... styles) {
            if (styles.length == 0) {
                styles = new BitmapFont.FontStyle[]{BitmapFont.FontStyle.NORMAL};
            }

            for (BitmapFont.FontStyle style : styles) {
                Integer glyphIndex = characterMaps.get(style).get(Character.toUpperCase(c));
                if (glyphIndex != null) {
                    fallbackGlyphs.put(style, glyphIndex);
                }
            }
            return this;
        }

        public FontConfig build() {
            if (texturePath == null) {
                throw new IllegalStateException("Texture path must be set");
            }

            // Add space character to all styles if not already added
            for (Map<Character, Integer> charMap : characterMaps.values()) {
                if (!charMap.containsKey(' ')) {
                    charMap.put(' ', -1); // -1 means just advance, don't draw
                }
            }

            // Set default fallback glyphs if not specified
            for (BitmapFont.FontStyle style : BitmapFont.FontStyle.values()) {
                if (!fallbackGlyphs.containsKey(style)) {
                    Integer questionMark = characterMaps.get(style).get('?');
                    if (questionMark != null) {
                        fallbackGlyphs.put(style, questionMark);
                    }
                }
            }

            return new FontConfig(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // ===== PRESET CONFIGURATIONS =====

    /**
     * Default 8×8 font configuration matching your font atlas
     *
     * Based on your uploaded font.png:
     * - Rows 0-4: Normal font (uppercase letters, numbers, symbols)
     * - Rows 5-6: Cursive font (uppercase letters)
     */
    public static FontConfig defaultFont() {
        return builder()
                .texturePath("font.png")
                .glyphSize(8, 8)
                .atlasSize(16, 8)
                // Normal font - Row 0
                .addCharacterRange("ABCDEFGHIJKLMNOP", 0, BitmapFont.FontStyle.NORMAL)
                // Normal font - Row 1
                .addCharacterRange("QRSTUVWXYZ", 16, BitmapFont.FontStyle.NORMAL)
                // Normal font - Row 2
                .addCharacterRange("1234567890.;:/\\", 32, BitmapFont.FontStyle.NORMAL)
                // Normal font - Row 3
                .addCharacterRange("!?@#$%^&*()[]+=-", 48, BitmapFont.FontStyle.NORMAL)
                // Normal font - Row 4
                .addCharacterRange("<>", 64, BitmapFont.FontStyle.NORMAL)
                // Cursive font - Row 5
                .addCharacterRange("ABCDEFGHIJKLMNOP", 80, BitmapFont.FontStyle.CURSIVE)
                // Cursive font - Row 6
                .addCharacterRange("QRSTUVWXYZ", 96, BitmapFont.FontStyle.CURSIVE)
                // Set fallback glyphs
                .setFallbackGlyph('?', BitmapFont.FontStyle.NORMAL)
                .setFallbackGlyph('?', BitmapFont.FontStyle.CURSIVE)
                .build();
    }
}