package org.example.engine.rendering;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;
import org.example.engine.rendering.ShaderManager.Shader;

/**
 * Material encapsulates shaders and rendering properties.
 * It defines how a renderable object should be drawn.
 */
public class Material {
    private Shader shader;
    private final Map<String, Object> properties = new HashMap<>();

    // Common material properties
    private Texture mainTexture;
    private Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private float shininess = 32.0f;
    private Vector3f specular = new Vector3f(0.5f, 0.5f, 0.5f);
    private boolean transparent = false;

    /**
     * Create a material with the specified shader
     */
    public Material(Shader shader) {
        this.shader = shader;
    }

    /**
     * Create a copy of this material
     */
    public Material copy() {
        Material copy = new Material(shader);

        // Copy properties
        copy.properties.putAll(properties);

        // Copy common properties
        copy.mainTexture = mainTexture;
        copy.color.set(color);
        copy.shininess = shininess;
        copy.specular.set(specular);
        copy.transparent = transparent;

        return copy;
    }

    /**
     * Bind this material for rendering
     */
    public void bind() {
        if (shader == null) return;

        shader.use();

        // Set main texture if available
        if (mainTexture != null) {
            mainTexture.bind(0);
            shader.setUniform1i("u_Texture", 0);
        }

        // Set color/tint
        shader.setUniform4f("u_Color", color.x, color.y, color.z, color.w);

        // Set lighting properties
        shader.setUniform1f("u_Shininess", shininess);
        shader.setUniform3f("u_Specular", specular.x, specular.y, specular.z);

        // Set all other properties
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            setUniform(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Unbind this material
     */
    public void unbind() {
        // Nothing specific to unbind in OpenGL 3.3+
    }

    /**
     * Set a float uniform
     */
    public void setFloat(String name, float value) {
        properties.put(name, value);
    }

    /**
     * Set an integer uniform
     */
    public void setInt(String name, int value) {
        properties.put(name, value);
    }

    /**
     * Set a boolean uniform
     */
    public void setBoolean(String name, boolean value) {
        properties.put(name, value ? 1 : 0);
    }

    /**
     * Set a Vector2f uniform
     */
    public void setVector2(String name, Vector2f value) {
        properties.put(name, new Vector2f(value));
    }

    /**
     * Set a Vector3f uniform
     */
    public void setVector3(String name, Vector3f value) {
        properties.put(name, new Vector3f(value));
    }

    /**
     * Set a Vector4f uniform
     */
    public void setVector4(String name, Vector4f value) {
        properties.put(name, new Vector4f(value));
    }

    /**
     * Set a texture uniform
     */
    public void setTexture(String name, Texture texture) {
        properties.put(name, texture);
    }

    /**
     * Set the main texture
     */
    public void setMainTexture(Texture texture) {
        this.mainTexture = texture;
    }

    /**
     * Get the main texture
     */
    public Texture getMainTexture() {
        return mainTexture;
    }

    /**
     * Set the material color/tint
     */
    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
        this.transparent = a < 0.99f;
    }

    /**
     * Set the material color/tint
     */
    public void setColor(Vector4f color) {
        this.color.set(color);
        this.transparent = color.w < 0.99f;
    }

    /**
     * Get the material color/tint
     */
    public Vector4f getColor() {
        return new Vector4f(color);
    }

    /**
     * Set the material shininess
     */
    public void setShininess(float shininess) {
        this.shininess = shininess;
    }

    /**
     * Get the material shininess
     */
    public float getShininess() {
        return shininess;
    }

    /**
     * Set the material specular color
     */
    public void setSpecular(float r, float g, float b) {
        this.specular.set(r, g, b);
    }

    /**
     * Get the material specular color
     */
    public Vector3f getSpecular() {
        return new Vector3f(specular);
    }

    /**
     * Set whether this material uses transparency
     */
    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }

    /**
     * Check if this material uses transparency
     */
    public boolean isTransparent() {
        return transparent || color.w < 0.99f;
    }

    /**
     * Get the shader used by this material
     */
    public Shader getShader() {
        return shader;
    }

    /**
     * Set the shader used by this material
     */
    public void setShader(Shader shader) {
        this.shader = shader;
    }

    /**
     * Helper to set the actual uniform in the shader
     */
    private void setUniform(String name, Object value) {
        if (shader == null) return;

        if (value instanceof Float) {
            shader.setUniform1f(name, (Float) value);
        } else if (value instanceof Integer) {
            shader.setUniform1i(name, (Integer) value);
        } else if (value instanceof Vector2f) {
            Vector2f vec = (Vector2f) value;
            shader.setUniform2f(name, vec.x, vec.y);
        } else if (value instanceof Vector3f) {
            Vector3f vec = (Vector3f) value;
            shader.setUniform3f(name, vec.x, vec.y, vec.z);
        } else if (value instanceof Vector4f) {
            Vector4f vec = (Vector4f) value;
            shader.setUniform4f(name, vec.x, vec.y, vec.z, vec.w);
        } else if (value instanceof Texture) {
            // Textures are handled separately in bind()
        }
    }
}