package org.example.engine.rendering;

import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.core.Transform;
import org.example.engine.resource.ResourceManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * A bitmap font renderer component for rendering text using a sprite-based font.
 * This uses a sprite sheet with characters arranged in a grid pattern.
 */
public class BitmapFontRenderer extends Component implements Renderable {
    // Font flags
    private static final int FONT_ITALIC = 1;
    private static final int FONT_DOUBLED = 2;
    private static final int FONT_CENTER_X = 4;

    // Font properties
    private String text = "";
    private float characterSize = 8.0f;
    private float scale = 2.0f;
    private float lineSpacing = 1.2f;
    private int currentColor = 0xFFFFFF;  // White
    private float alpha = 1.0f;

    // Sprite resource
    private Texture fontTexture;
    private final String[] colorPalette = new String[]{"555", "555", "555", "555"};

    // This Sprite is used to retrieve a default shader
    private Sprite sprite;

    // Layout for mapping characters to glyph positions
    private static final String[] FONT_LAYOUT = {
            "ABCDEFGHIJKLMNOP",
            "QRSTUVWXYZ",
            "1234567890.,;:/\\",
            "!?@#$%^&*()[]+-=",
            "<>{}'\"_|" + "\u00DB"
    };

    // Character mapping for quick lookups
    private final Map<Character, CharPosition> charPositions = new HashMap<>();

    // Format flags for current state
    private int currentFlags = 0;

    // Buffers for rendering
    private int vao;
    private int vbo;
    private FloatBuffer vertexBuffer;

    // Track state to detect changes
    private boolean needsRebuild = true;
    private int vertexCount = 0;

    /**
     * Helper class to store character positions in the sprite sheet
     */
    private static class CharPosition {
        final int x;
        final int y;

        CharPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Create a new bitmap font renderer
     */
    public BitmapFontRenderer() {
        // Initialize char positions from layout
        for (int row = 0; row < FONT_LAYOUT.length; row++) {
            String rowChars = FONT_LAYOUT[row];
            for (int col = 0; col < rowChars.length(); col++) {
                char c = rowChars.charAt(col);
                charPositions.put(c, new CharPosition(col, row));
            }
        }

        // Create placeholder sprite to use its shader
        sprite = new Sprite(null, characterSize, characterSize);

        // Allocate vertex buffer for rendering
        vertexBuffer = BufferUtils.createFloatBuffer(1024 * 4 * 9); // 1024 characters max, 4 vertices per char, 9 floats per vertex
    }

    @Override
    protected void onInit() {
        super.onInit();

        // Load font texture
        fontTexture = ResourceManager.getInstance().getTexture("font");
        if (fontTexture == null) {
            System.err.println("Could not load font texture!");
        }

        // Initialize OpenGL resources
        createBuffers();
    }

    /**
     * Create the OpenGL vertex buffers
     */
    private void createBuffers() {
        // Generate VAO and VBO
        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        // Bind VAO
        glBindVertexArray(vao);

        // Bind VBO
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // Allocate buffer space (will be filled with vertex data later)
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, GL_DYNAMIC_DRAW);

        // Set up vertex attributes
        // Position (3 floats)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Texture coordinates (2 floats)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Color (4 floats)
        glVertexAttribPointer(2, 4, GL_FLOAT, false, 9 * Float.BYTES, 5 * Float.BYTES);
        glEnableVertexAttribArray(2);

        // Unbind VAO
        glBindVertexArray(0);
    }

    @Override
    protected void onDestroy() {
        // Clean up OpenGL resources
        if (vbo != 0) {
            glDeleteBuffers(vbo);
            vbo = 0;
        }

        if (vao != 0) {
            glDeleteVertexArrays(vao);
            vao = 0;
        }
    }

    @Override
    public void render(RenderSystem renderSystem, Matrix4f viewProjectionMatrix) {
        if (text.isEmpty() || fontTexture == null) return;

        // Rebuild vertex data if needed
        if (needsRebuild) {
            buildTextVertices();
            needsRebuild = false;
        }

        // Skip rendering if no vertices were generated
        if (vertexCount == 0) return;

        // Get transform matrix
        Transform transform = getGameObject().getTransform();
        Matrix4f modelMatrix = new Matrix4f()
                .identity()
                .translate(transform.getPosition())
                .rotateZ(transform.getRotation())
                .scale(transform.getScale().x, transform.getScale().y, 1.0f);

        // Combine with view-projection
        Matrix4f mvpMatrix = new Matrix4f(viewProjectionMatrix).mul(modelMatrix);

        // Get shader
        ShaderManager.Shader shader = sprite.getMaterial().getShader();
        if (shader == null) {
            System.err.println("No shader available for font rendering!");
            return;
        }

        // Bind shader
        shader.use();

        // Set MVP matrix
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
        mvpMatrix.get(matrixBuffer);
        shader.setUniformMatrix4fv("u_MVP", matrixBuffer);

        // Set font texture
        fontTexture.bind(0);
        shader.setUniform1i("u_Texture", 0);
        shader.setUniform1i("u_hasTexture", 1);

        // Set color (current color is applied via vertex colors)
        shader.setUniform4f("u_Color", 1.0f, 1.0f, 1.0f, 1.0f);

        // Enable blending for transparency
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Update buffer data
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        vertexBuffer.limit(vertexCount * 9); // Set limit to actual vertex count
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer);

        // Draw the text quads
        glBindVertexArray(vao);

        // Calculate number of quads
        int numQuads = vertexCount / 4;

        // Draw each quad
        for (int i = 0; i < numQuads; i++) {
            glDrawArrays(GL_TRIANGLE_FAN, i * 4, 4);
        }

        // Unbind VAO
        glBindVertexArray(0);

        // Reset blend state
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Build vertex data for all characters in the text
     */
    private void buildTextVertices() {
        // Reset buffer
        vertexBuffer.clear();
        vertexCount = 0;

        float x = 0;
        float y = 0;
        float lineHeight = characterSize * lineSpacing * scale;

        // Calculate text width for centering if needed
        float textWidth = 0;
        if ((currentFlags & FONT_CENTER_X) != 0) {
            textWidth = getTextWidth();
            x = -textWidth / 2;
        }

        // Process each character
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // Handle special formatting codes
            if (c == '\\' && i < text.length() - 3) {
                String code = text.substring(i + 1, i + 4);
                i += 3; // Skip the code

                switch (code) {
                    case "ITA":
                        currentFlags |= FONT_ITALIC;
                        break;
                    case "REG":
                        currentFlags &= ~FONT_ITALIC;
                        break;
                    case "SIN":
                        currentFlags &= ~FONT_DOUBLED;
                        break;
                    case "DBL":
                        currentFlags |= FONT_DOUBLED;
                        break;
                    case "RES":
                        currentFlags &= ~(FONT_ITALIC | FONT_DOUBLED);
                        break;
                    case "CTX":
                        currentFlags |= FONT_CENTER_X;
                        // Recalculate text width and adjust x position
                        textWidth = getTextWidth();
                        x = -textWidth / 2;
                        break;
                    case "PCT":
                        c = '%';
                        break;
                    default:
                        // Check for color code (3 digits 0-5)
                        if (code.matches("[0-5]{3}")) {
                            int r = code.charAt(0) - '0';
                            int g = code.charAt(1) - '0';
                            int b = code.charAt(2) - '0';
                            r *= 51;
                            g *= 51;
                            b *= 51;
                            currentColor = (r << 16) | (g << 8) | b;
                        }
                        continue; // Skip to next character
                }

                // Continue to next character if this was just a format code
                if (c == '\\') continue;
            }

            // Handle newlines
            if (c == '\n') {
                y += lineHeight;
                x = 0;

                // Reset x position for centered text
                if ((currentFlags & FONT_CENTER_X) != 0) {
                    x = -textWidth / 2;
                }

                continue;
            }

            // Get character position in the font sheet
            CharPosition pos = charPositions.get(Character.toUpperCase(c));
            if (pos == null) {
                // Use a default character for unknown ones
                pos = charPositions.get('?');
                if (pos == null) continue; // Skip if we can't even find the fallback
            }

            // Calculate UV coordinates in the font texture
            float texWidth = fontTexture.getWidth();
            float texHeight = fontTexture.getHeight();

            float glyphWidth = characterSize;
            float glyphHeight = characterSize;

            float u0 = pos.x * glyphWidth / texWidth;
            float v0 = pos.y * glyphHeight / texHeight;
            float u1 = (pos.x + 1) * glyphWidth / texWidth;
            float v1 = (pos.y + 1) * glyphHeight / texHeight;

            // Calculate vertex positions
            float charWidth = characterSize * scale;
            float charHeight = characterSize * scale;

            // Apply italic slant if needed
            float italicOffset = 0;
            if ((currentFlags & FONT_ITALIC) != 0) {
                italicOffset = charHeight * 0.2f;
            }

            // Apply double size if needed
            if ((currentFlags & FONT_DOUBLED) != 0) {
                charWidth *= 2;
                charHeight *= 2;
            }

            // Extract color components
            float r = ((currentColor >> 16) & 0xFF) / 255.0f;
            float g = ((currentColor >> 8) & 0xFF) / 255.0f;
            float b = (currentColor & 0xFF) / 255.0f;

            // Add vertices for this character
            // Bottom-left
            addVertex(x, y + charHeight, 0, u0, v1, r, g, b, alpha);

            // Bottom-right
            addVertex(x + charWidth, y + charHeight, 0, u1, v1, r, g, b, alpha);

            // Top-right (with italic offset)
            addVertex(x + charWidth + italicOffset, y, 0, u1, v0, r, g, b, alpha);

            // Top-left (with italic offset)
            addVertex(x + italicOffset, y, 0, u0, v0, r, g, b, alpha);

            // Move to next character position
            x += charWidth + (scale * 2); // Add some spacing between characters
        }

        // Flip the buffer for reading
        vertexBuffer.flip();
    }

    /**
     * Add a vertex to the buffer
     */
    private void addVertex(float x, float y, float z, float u, float v, float r, float g, float b, float a) {
        vertexBuffer.put(x).put(y).put(z);
        vertexBuffer.put(u).put(v);
        vertexBuffer.put(r).put(g).put(b).put(a);
        vertexCount++;
    }

    /**
     * Set the text to render
     */
    public void setText(String text) {
        if (!this.text.equals(text)) {
            this.text = text;
            needsRebuild = true;
        }
    }

    /**
     * Get the current text
     */
    public String getText() {
        return text;
    }

    /**
     * Set the font color
     */
    public void setColor(int color) {
        if (this.currentColor != color) {
            this.currentColor = color;
            needsRebuild = true;
        }
    }

    /**
     * Set the font alpha (transparency)
     */
    public void setAlpha(float alpha) {
        if (this.alpha != alpha) {
            this.alpha = alpha;
            needsRebuild = true;
        }
    }

    /**
     * Set the font scale
     */
    public void setScale(float scale) {
        if (this.scale != scale) {
            this.scale = scale;
            needsRebuild = true;
        }
    }

    /**
     * Get the width of the rendered text
     */
    public float getTextWidth() {
        float width = 0;
        float maxWidth = 0;
        int flags = currentFlags;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // Handle special formatting codes
            if (c == '\\' && i < text.length() - 3) {
                String code = text.substring(i + 1, i + 4);
                i += 3; // Skip the code

                // Update flags based on formatting code
                switch (code) {
                    case "ITA":
                        flags |= FONT_ITALIC;
                        break;
                    case "REG":
                        flags &= ~FONT_ITALIC;
                        break;
                    case "SIN":
                        flags &= ~FONT_DOUBLED;
                        break;
                    case "DBL":
                        flags |= FONT_DOUBLED;
                        break;
                    case "RES":
                        flags &= ~(FONT_ITALIC | FONT_DOUBLED);
                        break;
                }

                continue;
            }

            // Handle newlines
            if (c == '\n') {
                maxWidth = Math.max(maxWidth, width);
                width = 0;
                continue;
            }

            // Add character width
            float charWidth = characterSize * scale;
            if ((flags & FONT_DOUBLED) != 0) {
                charWidth *= 2;
            }

            width += charWidth + (scale * 2); // Include character spacing
        }

        return Math.max(maxWidth, width);
    }

    /**
     * Get the height of the rendered text
     */
    public float getTextHeight() {
        int lines = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lines++;
            }
        }

        return lines * characterSize * lineSpacing * scale;
    }

    @Override
    public float getZ() {
        return getGameObject().getTransform().getPosition().z;
    }

    @Override
    public float getWidth() {
        return getTextWidth();
    }

    @Override
    public float getHeight() {
        return getTextHeight();
    }

    @Override
    public boolean isTransparent() {
        return alpha < 1.0f;
    }

    @Override
    public Material getMaterial() {
        return sprite.getMaterial();
    }

    @Override
    public Transform getTransform() {
        return getGameObject().getTransform();
    }
}