// src/main/java/org/example/gfx/BitmapFont.java
package org.example.gfx;

import org.joml.Vector4f;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Bitmap Font Renderer for 8×8 pixel font atlas
 * Supports both NORMAL and CURSIVE styles
 */
public final class BitmapFont {
    private final Texture fontTexture;
    private final TextureAtlas atlas;
    private final Map<Character, Integer> normalCharMap = new HashMap<>();
    private final Map<Character, Integer> cursiveCharMap = new HashMap<>();

    // Font properties
    private static final int GLYPH_WIDTH = 8;
    private static final int GLYPH_HEIGHT = 8;
    private static final int ATLAS_COLS = 16;

    public enum FontStyle {
        NORMAL,
        CURSIVE
    }

    public BitmapFont(String texturePath) {
        this.fontTexture = loadTexture(texturePath);
        this.atlas = new TextureAtlas(fontTexture, ATLAS_COLS, 8);

        initializeCharacterMaps();
    }

    private Texture loadTexture(String filename) {
        String[] pathsToTry = {
                "assets/" + filename,
                "src/main/resources/assets/" + filename,
                "../assets/" + filename,
                "../../assets/" + filename,
                filename
        };

        for (String pathStr : pathsToTry) {
            try {
                Path path = Path.of(pathStr);
                if (Files.exists(path)) {
                    return Texture.load(path.toString());
                }
            } catch (Exception e) {
                // Continue to next path
            }
        }

        // Try classpath
        try {
            var resource = getClass().getClassLoader().getResource(filename);
            if (resource != null) {
                return Texture.load(resource.getPath());
            }
        } catch (Exception e) {
            // Texture not found
        }

        return null;
    }

    private void initializeCharacterMaps() {
        // Normal font (rows 0-4)
        String normal1 = "ABCDEFGHIJKLMNOP";
        String normal2 = "QRSTUVWXYZ      ";  // spaces for empty tiles
        String normal3 = "1234567890.;:/\\";
        String normal4 = "!?@#$%^&*()[]+=-";
        String normal5 = "<>              ";  // rest are special/empty

        mapChars(normal1, 0, normalCharMap);
        mapChars(normal2, 16, normalCharMap);
        mapChars(normal3, 32, normalCharMap);
        mapChars(normal4, 48, normalCharMap);
        mapChars(normal5, 64, normalCharMap);

        // Cursive font (rows 5-6)
        String cursive1 = "ABCDEFGHIJKLMNOP";
        String cursive2 = "QRSTUVWXYZ      ";

        mapChars(cursive1, 80, cursiveCharMap);
        mapChars(cursive2, 96, cursiveCharMap);

        // Space character (always available)
        normalCharMap.put(' ', -1);  // -1 means just advance, don't draw
        cursiveCharMap.put(' ', -1);
    }

    private void mapChars(String chars, int startIndex, Map<Character, Integer> map) {
        for (int i = 0; i < chars.length(); i++) {
            char c = chars.charAt(i);
            if (c != ' ' || i == 0) {  // Map space only if it's the first char
                map.put(c, startIndex + i);
            }
        }
    }

    /**
     * Draw text at specified position
     */
    public void drawText(Renderer2D renderer, String text, float x, float y,
                         float scale, Vector4f color, FontStyle style) {

        Map<Character, Integer> charMap = (style == FontStyle.CURSIVE) ?
                cursiveCharMap : normalCharMap;

        float currentX = x;

        for (char c : text.toCharArray()) {
            // Convert lowercase to uppercase for font lookup
            char upperC = Character.toUpperCase(c);

            Integer glyphIndex = charMap.get(upperC);

            if (glyphIndex != null) {
                if (glyphIndex == -1) {
                    // Space character - just advance
                    currentX += GLYPH_WIDTH * scale;
                } else {
                    // Draw the glyph
                    float[] uv = atlas.frameUV(glyphIndex);

                    Material mat = Material.builder()
                            .texture(fontTexture)
                            .tint(color)
                            .build();

                    renderer.drawQuad(
                            currentX,
                            y,
                            GLYPH_WIDTH * scale,
                            GLYPH_HEIGHT * scale,
                            0f,
                            mat,
                            uv,
                            atlas.texelW(),
                            atlas.texelH()
                    );

                    currentX += GLYPH_WIDTH * scale;
                }
            } else {
                // Unknown character - draw as '?'
                Integer questionMark = charMap.get('?');
                if (questionMark != null && questionMark != -1) {
                    float[] uv = atlas.frameUV(questionMark);

                    Material mat = Material.builder()
                            .texture(fontTexture)
                            .tint(color)
                            .build();

                    renderer.drawQuad(
                            currentX,
                            y,
                            GLYPH_WIDTH * scale,
                            GLYPH_HEIGHT * scale,
                            0f,
                            mat,
                            uv,
                            atlas.texelW(),
                            atlas.texelH()
                    );
                }

                currentX += GLYPH_WIDTH * scale;
            }
        }
    }

    /**
     * Measure text width in pixels
     */
    public float measureText(String text, float scale) {
        return text.length() * GLYPH_WIDTH * scale;
    }

    /**
     * Get text height
     */
    public float getHeight(float scale) {
        return GLYPH_HEIGHT * scale;
    }

    public void close() {
        fontTexture.close();
    }
}