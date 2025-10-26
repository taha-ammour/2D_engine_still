// src/main/java/org/example/engine/Window.java
package org.example.engine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL43.*;

/**
 * Window management with resize event support
 * Open/Closed Principle: Extensible through listeners, closed for modification
 */
public final class Window implements AutoCloseable {
    private long handle;
    private int width, height;
    private final String title;
    private boolean resized = false;
    private int newWidth, newHeight;

    // Observer Pattern: Listeners for resize events
    private final List<WindowResizeListener> resizeListeners = new ArrayList<>();

    public Window(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
    }

    public void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("GLFW init failed");

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);  // ✅ Resizable enabled

        handle = glfwCreateWindow(width, height, title, 0, 0);
        if (handle == 0) throw new IllegalStateException("Window creation failed");

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(1);
        GL.createCapabilities();

        // ✅ Setup resize callback
        glfwSetFramebufferSizeCallback(handle, (window, w, h) -> {
            this.newWidth = w;
            this.newHeight = h;
            this.resized = true;
        });

        glfwShowWindow(handle);
        glViewport(0, 0, width, height);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        System.out.println("✅ Window initialized: " + width + "x" + height + " (Resizable)");
    }

    /**
     * Check if window was resized and notify listeners
     * Open/Closed: New listeners can be added without modifying this method
     */
    public boolean wasResized() {
        if (resized) {
            width = newWidth;
            height = newHeight;
            glViewport(0, 0, width, height);

            // ✅ Notify all listeners
            notifyResizeListeners(width, height);

            resized = false;
            return true;
        }
        return false;
    }

    /**
     * Add a resize listener
     * Open/Closed: Extend functionality by adding new listeners
     */
    public void addResizeListener(WindowResizeListener listener) {
        if (!resizeListeners.contains(listener)) {
            resizeListeners.add(listener);
            System.out.println("✅ Resize listener added: " + listener.getClass().getSimpleName());
        }
    }

    /**
     * Remove a resize listener
     */
    public void removeResizeListener(WindowResizeListener listener) {
        resizeListeners.remove(listener);
    }

    /**
     * Notify all resize listeners
     */
    private void notifyResizeListeners(int newWidth, int newHeight) {
        System.out.println("🔄 Window resized: " + newWidth + "x" + newHeight +
                " (notifying " + resizeListeners.size() + " listeners)");

        for (WindowResizeListener listener : resizeListeners) {
            try {
                listener.onWindowResize(newWidth, newHeight);
            } catch (Exception e) {
                System.err.println("⚠️ Error in resize listener " +
                        listener.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Manually trigger resize (useful for fullscreen toggle, etc.)
     */
    public void requestResize(int newWidth, int newHeight) {
        glfwSetWindowSize(handle, newWidth, newHeight);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    public void swap() {
        glfwSwapBuffers(handle);
    }

    public long handle() {
        return handle;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    @Override
    public void close() {
        resizeListeners.clear();
        glfwDestroyWindow(handle);
        glfwTerminate();
    }
}