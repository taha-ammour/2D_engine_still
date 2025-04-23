package org.example.engine.rendering;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

/**
 * Represents an OpenGL texture.
 */
public class Texture {
    private final int id;
    private final int width;
    private final int height;
    private final int channels;
    private String filePath;

    /**
     * Create a texture from a file path
     */
    public Texture(String filePath) {
        this.filePath = filePath;

        // Generate texture ID
        id = glGenTextures();

        // Adjust the file path to look in resources directory
        // This allows for both absolute paths and relative paths
        String adjustedPath = filePath;
        if (filePath.startsWith("/")) {
            adjustedPath = filePath.substring(1);
        }

        // First try to load from the original path
        File file = new File(adjustedPath);
        if (!file.exists()) {
            // If file doesn't exist, look in the current directory
            file = new File(System.getProperty("user.dir") + File.separator + adjustedPath);
            if (!file.exists()) {
                // If still doesn't exist, try resource path
                String resourcePath = "src/main/resources/" + adjustedPath;
                file = new File(resourcePath);
                if (!file.exists()) {
                    throw new RuntimeException("Failed to load texture: Unable to find file " + filePath +
                            " (tried " + adjustedPath + " and " + resourcePath + ")");
                }
                adjustedPath = resourcePath;
            } else {
                adjustedPath = file.getAbsolutePath();
            }
        }

        // Debug output to show the file being loaded
        System.out.println("Loading texture from: " + adjustedPath);

        // Load texture data using STB
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelsBuffer = stack.mallocInt(1);

            // Load image
            //STBImage.stbi_set_flip_vertically_on_load(false);
            ByteBuffer imageData = STBImage.stbi_load(adjustedPath, widthBuffer, heightBuffer, channelsBuffer, 0);

            if (imageData == null) {
                throw new RuntimeException("Failed to load texture: " + STBImage.stbi_failure_reason());
            }

            // Get dimensions
            width = widthBuffer.get(0);
            height = heightBuffer.get(0);
            channels = channelsBuffer.get(0);

            // Determine format
            int format;
            if (channels == 4) {
                format = GL_RGBA;
            } else if (channels == 3) {
                format = GL_RGB;
            } else if (channels == 1) {
                format = GL_RED;
            } else {
                throw new RuntimeException("Unsupported number of channels: " + channels);
            }

            // Bind texture
            glBindTexture(GL_TEXTURE_2D, id);

            // Set texture parameters
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            // Upload texture data
            glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, imageData);

            // Generate mipmaps
            glGenerateMipmap(GL_TEXTURE_2D);

            // Free image data
            STBImage.stbi_image_free(imageData);
        }
    }

    /**
     * Create a texture from raw data
     */
    public Texture(ByteBuffer data, int width, int height, int channels) {
        this.width = width;
        this.height = height;
        this.channels = channels;

        // Generate texture ID
        id = glGenTextures();

        // Determine format
        int format;
        if (channels == 4) {
            format = GL_RGBA;
        } else if (channels == 3) {
            format = GL_RGB;
        } else if (channels == 1) {
            format = GL_RED;
        } else {
            throw new RuntimeException("Unsupported number of channels: " + channels);
        }

        // Bind texture
        glBindTexture(GL_TEXTURE_2D, id);

        // Set texture parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // Upload texture data
        glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, data);

        // Generate mipmaps
        glGenerateMipmap(GL_TEXTURE_2D);
    }

    /**
     * Bind this texture to the specified texture unit
     */
    public void bind(int textureUnit) {
        glActiveTexture(GL_TEXTURE0 + textureUnit);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    /**
     * Unbind any texture
     */
    public static void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /**
     * Clean up resources
     */
    public void dispose() {
        glDeleteTextures(id);
    }

    /**
     * Get the texture ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get the texture width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the texture height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the number of channels
     */
    public int getChannels() {
        return channels;
    }

    /**
     * Get the file path
     */
    public String getFilePath() {
        return filePath;
    }
}