// src/main/java/org/example/gfx/Framebuffer.java
package org.example.gfx;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Framebuffer for rendering to textures
 * ✅ FIXED: Now supports resizing!
 */
public final class Framebuffer implements AutoCloseable {
    private int fbo;
    private int colorTexture;
    private int width;
    private int height;

    public Framebuffer(int width, int height) {
        this.width = width;
        this.height = height;
        create();
    }

    /**
     * Create/recreate the framebuffer with current dimensions
     */
    private void create() {
        // If this is a recreation, delete old resources first
        if (fbo != 0) {
            glDeleteTextures(colorTexture);
            glDeleteFramebuffers(fbo);
        }

        // Create framebuffer
        fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        // Create color texture
        colorTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, colorTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, width, height, 0, GL_RGBA, GL_FLOAT, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        // Attach to framebuffer
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTexture, 0);

        // Check framebuffer status
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer is not complete!");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    /**
     * ✅ FIXED: Resize the framebuffer
     * Recreates the framebuffer and texture with new dimensions
     */
    public void resize(int newWidth, int newHeight) {
        if (newWidth > 0 && newHeight > 0 && (newWidth != width || newHeight != height)) {
            this.width = newWidth;
            this.height = newHeight;
            create();
            System.out.println("🖼️  Framebuffer resized to: " + width + "x" + height);
        }
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glViewport(0, 0, width, height);
    }

    public static void unbind(int screenWidth, int screenHeight) {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, screenWidth, screenHeight);
    }

    public void bindTexture(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, colorTexture);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getColorTexture() { return colorTexture; }

    @Override
    public void close() {
        if (colorTexture != 0) glDeleteTextures(colorTexture);
        if (fbo != 0) glDeleteFramebuffers(fbo);
        colorTexture = 0;
        fbo = 0;
    }
}