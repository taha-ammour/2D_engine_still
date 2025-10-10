// src/main/java/org/example/engine/Window.java
package org.example.engine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL43.*;

public final class Window implements AutoCloseable {
    private long handle;
    private int width, height;
    private final String title;
    private boolean resized = false;
    private int newWidth, newHeight;

    public Window(int width, int height, String title) {
        this.width = width; this.height = height; this.title = title;
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("GLFW init failed");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        // macOS needs this:
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        handle = glfwCreateWindow(width, height, title, 0, 0);
        if (handle == 0) throw new IllegalStateException("Window creation failed");
        glfwMakeContextCurrent(handle);
        glfwSwapInterval(1);
        GL.createCapabilities();

        glfwSetFramebufferSizeCallback(handle, (window, w, h) -> {
            this.newWidth = w;
            this.newHeight = h;
            this.resized = true;
        });

        glfwShowWindow(handle);
        glViewport(0, 0, width, height);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public boolean wasResized() {
        if (resized) {
            width = newWidth;
            height = newHeight;
            glViewport(0, 0, width, height);
            resized = false;
            return true;
        }
        return false;
    }

    public boolean shouldClose() { return glfwWindowShouldClose(handle); }
    public void pollEvents() { glfwPollEvents(); }
    public void swap() { glfwSwapBuffers(handle); }
    public long handle() { return handle; }
    public int width() { return width; }
    public int height() { return height; }

    @Override public void close() {
        glfwDestroyWindow(handle);
        glfwTerminate();
    }
}
