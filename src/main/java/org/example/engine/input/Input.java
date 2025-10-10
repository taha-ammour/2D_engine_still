// src/main/java/org/example/engine/input/Input.java
package org.example.engine.input;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Low-level input wrapper
 * Fixed to only poll valid GLFW key codes (32 onwards)
 */
public final class Input {
    private final long window;
    private final boolean[] keysPressed = new boolean[GLFW_KEY_LAST + 1];
    private final boolean[] keysPressedLast = new boolean[GLFW_KEY_LAST + 1];

    public Input(long window) {
        this.window = window;
    }

    public void update() {
        // Copy current to last
        System.arraycopy(keysPressed, 0, keysPressedLast, 0, keysPressed.length);

        // Update current - start from GLFW_KEY_SPACE (32) to avoid invalid keys 0-31
        // Also check special keys like ESC (256+)

        // Poll printable keys (32-96) and special keys (256-348)
        for (int key = GLFW_KEY_SPACE; key <= GLFW_KEY_GRAVE_ACCENT; key++) {
            keysPressed[key] = glfwGetKey(window, key) == GLFW_PRESS;
        }

        // Poll function keys and special keys (ESC, arrows, etc.)
        for (int key = GLFW_KEY_ESCAPE; key <= GLFW_KEY_LAST; key++) {
            keysPressed[key] = glfwGetKey(window, key) == GLFW_PRESS;
        }
    }

    public boolean isKeyDown(int key) {
        if (key < 0 || key >= keysPressed.length) return false;
        return keysPressed[key];
    }

    public boolean isKeyPressed(int key) {
        if (key < 0 || key >= keysPressed.length) return false;
        return keysPressed[key] && !keysPressedLast[key];
    }

    public boolean isKeyReleased(int key) {
        if (key < 0 || key >= keysPressed.length) return false;
        return !keysPressed[key] && keysPressedLast[key];
    }
}