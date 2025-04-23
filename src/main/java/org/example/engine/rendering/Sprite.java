package org.example.engine.rendering;

import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.core.Transform;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Sprite component that handles rendering a 2D image with proper Z-ordering.
 * This implementation supports palette swapping, flipping, and lighting.
 */
public class Sprite extends Component implements Renderable {
    // Sprite properties
    private Texture texture;
    private final float width;
    private final float height;
    private final float u0, v0, u1, v1; // UV coordinates

    // Rendering properties
    private Material material;
    private boolean flipX = false;
    private boolean flipY = false;
    private float alpha = 1.0f;
    private boolean isTransparent = false;

    // Buffers and handles
    private int vao;
    private int vbo;
    private int ebo;

    // Palette for palette swapping
    private float[] paletteColors = new float[12]; // 4 colors × 3 components (RGB)

    // Cached matrices to avoid allocation
    private final Matrix4f modelMatrix = new Matrix4f();
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    // Color/tint
    private Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    // Debug flags
    private boolean verboseLogging = false;

    /**
     * Create a sprite with the given texture and dimensions
     */
    public Sprite(Texture texture, float width, float height) {
        this(texture, 0, 0, 1, 1, width, height);

        this.setTransparent(true);
    }

    /**
     * Create a sprite with the given texture, UV coordinates, and dimensions
     */
    public Sprite(Texture texture, float u0, float v0, float u1, float v1, float width, float height) {
        this.texture = texture;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.width = width;
        this.height = height;

        if (verboseLogging) {
            System.out.println("Creating sprite with dimensions: " + width + "x" + height);
            System.out.println("UVs: (" + u0 + "," + v0 + ") to (" + u1 + "," + v1 + ")");
            System.out.println("Texture: " + (texture != null ? texture.getId() : "null"));
        }

        // Default palette (grayscale)
        for (int i = 0; i < 4; i++) {
            float value = i / 3.0f; // 0, 0.33, 0.67, 1.0
            paletteColors[i*3] = value;
            paletteColors[i*3+1] = value;
            paletteColors[i*3+2] = value;
        }

        // Get default shader
        ShaderManager.Shader shader = ShaderManager.getInstance().getShader("sprite");
        if (shader == null) {
            System.err.println("WARNING: Sprite shader not found, will try to create a default one");
            shader = createDefaultShader();
        } else if (verboseLogging) {
            System.out.println("Using sprite shader with ID: " + shader.getProgramId());
        }

        // Create material with the shader
        material = new Material(shader);
        if (verboseLogging) {
            System.out.println("Created material with shader: " + (shader != null ? shader.getProgramId() : "null"));
        }

        // Create the mesh
        createMesh();
    }

    /**
     * Create a default shader if the main one is not available
     */
    private ShaderManager.Shader createDefaultShader() {
        try {
            // VERTEX SHADER
            String vertexSource =
                    "#version 330 core\n" +
                            "layout (location = 0) in vec3 aPos;\n" +
                            "layout (location = 1) in vec2 aTexCoord;\n" +
                            "layout (location = 2) in vec3 aNormal;\n" +
                            "\n" +
                            "out vec2 TexCoord;\n" +
                            "out vec3 FragPos;\n" +
                            "out vec3 Normal;\n" +
                            "\n" +
                            "uniform mat4 u_MVP;\n" +
                            "uniform mat4 u_Model = mat4(1.0);\n" +
                            "\n" +
                            "void main() {\n" +
                            "    gl_Position = u_MVP * vec4(aPos, 1.0);\n" +
                            "    TexCoord = aTexCoord;\n" +
                            "    FragPos = vec3(u_Model * vec4(aPos, 1.0));\n" +
                            "    Normal = mat3(transpose(inverse(u_Model))) * aNormal;\n" +
                            "}\n";

            // FRAGMENT SHADER - SIMPLIFIED VERSION
            String fragmentSource =
                    "#version 330 core\n" +
                            "in vec2 TexCoord;\n" +
                            "in vec3 FragPos;\n" +
                            "in vec3 Normal;\n" +
                            "\n" +
                            "out vec4 FragColor;\n" +
                            "\n" +
                            "// Basic uniforms that all shaders should have\n" +
                            "uniform sampler2D u_Texture;\n" +
                            "uniform vec4 u_Color = vec4(1.0, 1.0, 1.0, 1.0);\n" +
                            "uniform bool u_hasTexture = false;\n" +
                            "uniform int u_flipX = 0;\n" +
                            "uniform int u_flipY = 0;\n" +
                            "uniform vec4 u_texCoords = vec4(0.0, 0.0, 1.0, 1.0);\n" +
                            "\n" +
                            "// Light-related uniforms with defaults\n" +
                            "uniform vec3 u_AmbientColor = vec3(0.3, 0.3, 0.3);\n" +
                            "uniform vec3 u_Specular = vec3(0.5, 0.5, 0.5);\n" +
                            "uniform float u_Shininess = 32.0;\n" +
                            "uniform vec3 u_ViewPos = vec3(0.0, 0.0, 10.0);\n" +
                            "uniform int lightCount = 0;\n" +
                            "\n" +
                            "// Palette for color mapping\n" +
                            "uniform vec3 u_Palette[4] = vec3[4](\n" +
                            "    vec3(0.0, 0.0, 0.0),\n" +
                            "    vec3(0.33, 0.33, 0.33),\n" +
                            "    vec3(0.67, 0.67, 0.67),\n" +
                            "    vec3(1.0, 1.0, 1.0)\n" +
                            ");\n" +
                            "\n" +
                            "void main() {\n" +
                            "    // Calculate texture coordinates with flipping support\n" +
                            "    vec2 texCoords = TexCoord;\n" +
                            "    \n" +
                            "    // Adjust UVs based on texture coordinates uniform\n" +
                            "    texCoords.x = mix(u_texCoords.x, u_texCoords.z, texCoords.x);\n" +
                            "    texCoords.y = mix(u_texCoords.y, u_texCoords.w, texCoords.y);\n" +
                            "    \n" +
                            "    // Apply flipping if needed\n" +
                            "    if (u_flipX > 0) {\n" +
                            "        texCoords.x = u_texCoords.z - (texCoords.x - u_texCoords.x);\n" +
                            "    }\n" +
                            "    if (u_flipY > 0) {\n" +
                            "        texCoords.y = u_texCoords.w - (texCoords.y - u_texCoords.y);\n" +
                            "    }\n" +
                            "    \n" +
                            "    // Sample the texture with the adjusted coordinates\n" +
                            "    vec4 texColor = texture(u_Texture, texCoords);\n" +
                            "    \n" +
                            "    // Early discard for transparent pixels\n" +
                            "    if (u_hasTexture && texColor.a < 0.01) {\n" +
                            "        discard;\n" +
                            "    }\n" +
                            "    \n" +
                            "    // Determine if we have a valid texture\n" +
                            "    bool hasTexture = u_hasTexture && texColor.a > 0.01;\n" +
                            "    \n" +
                            "    // Determine base color (palette or solid color)\n" +
                            "    vec3 baseColor;\n" +
                            "    \n" +
                            "    if (hasTexture) {\n" +
                            "        // Use palette system for textured sprites\n" +
                            "        int i = 0;\n" +
                            "        if(texColor.r >= (0xA0 / 255.0))\n" +
                            "            i = 3;\n" +
                            "        else if(texColor.r >= (0x70 / 255.0))\n" +
                            "            i = 2;\n" +
                            "        else if(texColor.r >= (0x40 / 255.0))\n" +
                            "            i = 1;\n" +
                            "        \n" +
                            "        baseColor = u_Palette[i];\n" +
                            "    } else {\n" +
                            "        // For solid color sprites, use the given color directly\n" +
                            "        baseColor = u_Color.rgb;\n" +
                            "    }\n" +
                            "    \n" +
                            "    // Final lighting is just ambient for the simplified shader\n" +
                            "    vec3 lighting = u_AmbientColor;\n" +
                            "    \n" +
                            "    // Apply lighting\n" +
                            "    vec3 finalColor = baseColor * lighting;\n" +
                            "    \n" +
                            "    // Calculate final alpha\n" +
                            "    float alpha = hasTexture ? texColor.a * u_Color.a : u_Color.a;\n" +
                            "    \n" +
                            "    // Output final color\n" +
                            "    FragColor = vec4(finalColor * u_Color.rgb, alpha);\n" +
                            "}\n";

            ShaderManager.Shader shader = new ShaderManager.Shader(vertexSource, fragmentSource);
            ShaderManager.getInstance().addShader("sprite", shader);
            System.out.println("Created simplified sprite shader with fixed uniform types");
            return shader;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create default shader: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create the sprite mesh (quad)
     */
    private void createMesh() {
        if (verboseLogging) {
            System.out.println("Creating sprite mesh");
        }

        try {
            // Vertex data: positions, UVs, and normals
            float[] vertices = {
                    // Position (x,y,z), UV (u,v), Normal (nx,ny,nz)
                    0.0f,       0.0f,        0.0f, u0, v0, 0.0f, 0.0f, 1.0f,
                    width,      0.0f,        0.0f, u1, v0, 0.0f, 0.0f, 1.0f,
                    width,      height,      0.0f, u1, v1, 0.0f, 0.0f, 1.0f,
                    0.0f,       height,      0.0f, u0, v1, 0.0f, 0.0f, 1.0f
            };

            // Index data (for two triangles forming a quad)
            int[] indices = {
                    0, 1, 2,  // First triangle
                    2, 3, 0   // Second triangle
            };

            // Create and bind VAO
            vao = glGenVertexArrays();
            glBindVertexArray(vao);

            // Create and bind VBO
            vbo = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertices.length);
            vertexBuffer.put(vertices).flip();
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

            // Create and bind EBO
            ebo = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
            indexBuffer.put(indices).flip();
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

            // Set up vertex attributes
            // Position (3 floats)
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 8 * Float.BYTES, 0);
            glEnableVertexAttribArray(0);

            // Texture coordinates (2 floats)
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
            glEnableVertexAttribArray(1);

            // Normal (3 floats)
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 8 * Float.BYTES, 5 * Float.BYTES);
            glEnableVertexAttribArray(2);

            // Unbind VAO
            glBindVertexArray(0);

            if (verboseLogging) {
                System.out.println("Sprite mesh created with VAO: " + vao + ", VBO: " + vbo + ", EBO: " + ebo);
            }
        } catch (Exception e) {
            System.err.println("ERROR creating sprite mesh: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void render(RenderSystem renderSystem, Matrix4f viewProjectionMatrix) {
        if (!isActive()) {
            if (verboseLogging) {
                System.out.println("Sprite not active, skipping render");
            }
            return;
        }

        try {
            if (verboseLogging) {
                System.out.println("Rendering sprite with texture: " + (texture != null ? texture.getId() : "null"));
            }

            // Calculate model matrix based on transform
            Transform transform = getGameObject().getTransform();
            if (transform == null) {
                System.err.println("ERROR: Transform is null for GameObject: " +
                        (getGameObject() != null ? getGameObject().getName() : "null"));
                return;
            }
            // Log transform data for debugging
            if (verboseLogging) {
                Vector3f position = transform.getPosition();
                System.out.println("Transform position: " + position.x + ", " + position.y + ", " + position.z);
                System.out.println("Transform rotation: " + transform.getRotation());
                System.out.println("Transform scale: " + transform.getScale().x + ", " + transform.getScale().y);
            }

            modelMatrix.identity()
                    .translate(transform.getPosition())
                    .rotateZ(transform.getRotation())
                    .scale(transform.getScale().x, transform.getScale().y, 1.0f);

            // Combine with view-projection matrix
            Matrix4f mvpMatrix = new Matrix4f(viewProjectionMatrix).mul(modelMatrix);

            // Bind shader and set uniforms
            ShaderManager.Shader shader = material.getShader();
            if (shader == null) {
                System.err.println("ERROR: Shader is null for sprite material");
                return;
            }

            shader.use();

            // Set MVP matrix
            mvpMatrix.get(matrixBuffer);
            shader.setUniformMatrix4fv("u_MVP", matrixBuffer);

            // Set model matrix
            modelMatrix.get(matrixBuffer);
            shader.setUniformMatrix4fv("u_Model", matrixBuffer);

            // Set texture
            if (texture != null) {
                texture.bind(0);
                shader.setUniform1i("u_Texture", 0);
            } else {
                // Use a default texture or handle null texture case
                // This is handled in the shader
                if (verboseLogging) {
                    System.out.println("Using a null texture (solid color sprite)");
                }
            }

            // Set palette colors
            shader.setUniform3fv("u_Palette", paletteColors);

            // Set flip flags
            shader.setUniform1i("u_flipX", flipX ? 1 : 0);
            shader.setUniform1i("u_flipY", flipY ? 1 : 0);
            shader.setUniform1i("u_hasTexture", texture != null ? 1 : 0);


            // Set texture coordinates
            shader.setUniform4f("u_texCoords", u0, v0, u1, v1);

            // Set color/tint
            shader.setUniform4f("u_Color", color.x, color.y, color.z, color.w * alpha);

            // Set view position for lighting calculations
            Vector3f viewPos = renderSystem.getCamera().getPosition();
            shader.setUniform3f("u_ViewPos", viewPos.x, viewPos.y, viewPos.z);

            // Draw the sprite
            glBindVertexArray(vao);
            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

            if (verboseLogging) {
                System.out.println("Draw call executed with VAO: " + vao + ", 6 vertices");
            }

            glBindVertexArray(0);

            // Unbind shader
            glUseProgram(0);

            if (verboseLogging) {
                System.out.println("Sprite rendering complete");
            }
        } catch (Exception e) {
            System.err.println("ERROR in sprite rendering: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        // Clean up resources
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteVertexArrays(vao);
    }

    @Override
    public float getZ() {
        if (getGameObject() == null || getGameObject().getTransform() == null) {
            return 0; // Default Z if transform is not available
        }
        return getGameObject().getTransform().getPosition().z;
    }

    @Override
    public float getWidth() {
        if (getGameObject() == null || getGameObject().getTransform() == null) {
            return width; // Default width if transform is not available
        }
        return width * getGameObject().getTransform().getScale().x;
    }

    @Override
    public float getHeight() {
        if (getGameObject() == null || getGameObject().getTransform() == null) {
            return height; // Default height if transform is not available
        }
        return height * getGameObject().getTransform().getScale().y;
    }

    @Override
    public Transform getTransform() {
        if (getGameObject() == null) {
            return null;
        }
        return getGameObject().getTransform();
    }

    @Override
    public boolean isTransparent() {
        return isTransparent || color.w * alpha < 0.99f;
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    /**
     * Set the palette colors for palette swapping
     * @param paletteCodes Array of 4 color codes (as "000" to "555")
     */
    public void setPaletteFromCodes(String[] paletteCodes) {
        if (paletteCodes == null || paletteCodes.length != 4) {
            throw new IllegalArgumentException("Expected exactly 4 palette codes");
        }

        for (int i = 0; i < 4; i++) {
            String code = paletteCodes[i].trim();
            if (code.length() != 3) {
                throw new IllegalArgumentException("Palette code at index " + i + " must be exactly 3 characters long");
            }

            // Convert from "012" format to RGB [0.0-1.0] values
            paletteColors[i*3] = (code.charAt(0) - '0') / 5.0f;
            paletteColors[i*3+1] = (code.charAt(1) - '0') / 5.0f;
            paletteColors[i*3+2] = (code.charAt(2) - '0') / 5.0f;
        }
    }

    /**
     * Set the sprite's color/tint
     * @param color Color in RGB format (0xRRGGBB)
     * @param alpha Alpha value [0.0-1.0]
     */
    public void setColor(int color, float alpha) {
        // Extract RGB components from the color int
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        this.color.set(r, g, b, 1.0f);
        this.alpha = alpha;

        if (verboseLogging) {
            System.out.println("Set sprite color: RGB(" + r + ", " + g + ", " + b + "), alpha: " + alpha);
        }
    }

    /**
     * Set whether the sprite should be flipped horizontally
     */
    public void setFlipX(boolean flip) {
        this.flipX = flip;
    }

    /**
     * Set whether the sprite should be flipped vertically
     */
    public void setFlipY(boolean flip) {
        this.flipY = flip;
    }

    /**
     * Set the sprite's transparency (alpha value)
     */
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    /**
     * Mark this sprite as using transparency
     */
    public void setTransparent(boolean transparent) {
        this.isTransparent = transparent;
    }

    /**
     * Get the texture's normalized UV coordinates
     */
    public float getU0() { return u0; }
    public float getV0() { return v0; }
    public float getU1() { return u1; }
    public float getV1() { return v1; }

    /**
     * Create a copy of this sprite
     */
    public Sprite copy() {
        Sprite copy = new Sprite(texture, u0, v0, u1, v1, width, height);
        copy.setPaletteColors(paletteColors.clone());
        copy.setColor((int)(color.x * 255) << 16 | (int)(color.y * 255) << 8 | (int)(color.z * 255), alpha);
        copy.setFlipX(flipX);
        copy.setFlipY(flipY);
        copy.setTransparent(isTransparent);
        return copy;
    }

    // Getters and setters

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public void setPaletteColors(float[] paletteColors) {
        if (paletteColors.length != 12) {
            throw new IllegalArgumentException("Palette must have exactly 12 values (4 colors × RGB)");
        }
        System.arraycopy(paletteColors, 0, this.paletteColors, 0, 12);
    }

    public float[] getPaletteColors() {
        return paletteColors.clone();
    }

    public boolean isFlipX() {
        return flipX;
    }

    public boolean isFlipY() {
        return flipY;
    }

    public float getAlpha() {
        return alpha;
    }

    public Vector4f getColor() {
        return new Vector4f(color);
    }

    public void setVerboseLogging(boolean verbose) {
        this.verboseLogging = verbose;
    }
}