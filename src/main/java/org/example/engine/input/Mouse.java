// src/main/java/org/example/engine/input/Mouse.java
package org.example.engine.input;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Mouse input system with position, buttons, and scroll tracking
 * Single Responsibility: Handles all mouse input state
 */
public final class Mouse {
    private final long window;

    // Mouse position
    private double x, y;
    private double lastX, lastY;
    private double deltaX, deltaY;

    // Mouse buttons (support up to 8 mouse buttons)
    private final boolean[] buttonsPressed = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
    private final boolean[] buttonsPressedLast = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];

    // Mouse scroll
    private double scrollX, scrollY;
    private boolean scrolledThisFrame = false;

    // Cursor state
    private boolean cursorHidden = false;
    private boolean cursorLocked = false;

    public Mouse(long window) {
        this.window = window;

        // Get initial mouse position
        double[] xPos = new double[1];
        double[] yPos = new double[1];
        glfwGetCursorPos(window, xPos, yPos);
        this.x = xPos[0];
        this.y = yPos[0];
        this.lastX = x;
        this.lastY = y;

        // Setup scroll callback
        glfwSetScrollCallback(window, (win, xOffset, yOffset) -> {
            scrollX = xOffset;
            scrollY = yOffset;
            scrolledThisFrame = true;
        });
    }

    /**
     * Update mouse state (call once per frame)
     */
    public void update() {
        // Save last position
        lastX = x;
        lastY = y;

        // Get current position
        double[] xPos = new double[1];
        double[] yPos = new double[1];
        glfwGetCursorPos(window, xPos, yPos);
        x = xPos[0];
        y = yPos[0];

        // Calculate delta
        deltaX = x - lastX;
        deltaY = y - lastY;

        // Copy current to last for buttons
        System.arraycopy(buttonsPressed, 0, buttonsPressedLast, 0, buttonsPressed.length);

        // Update button states
        for (int button = 0; button <= GLFW_MOUSE_BUTTON_LAST; button++) {
            buttonsPressed[button] = glfwGetMouseButton(window, button) == GLFW_PRESS;
        }

        // Reset scroll if it wasn't used this frame
        if (!scrolledThisFrame) {
            scrollX = 0;
            scrollY = 0;
        }
        scrolledThisFrame = false;
    }

    // ===== POSITION =====

    /**
     * Get current mouse X position in screen coordinates
     */
    public double getX() {
        return x;
    }

    /**
     * Get current mouse Y position in screen coordinates
     */
    public double getY() {
        return y;
    }

    /**
     * Get mouse movement since last frame (X)
     */
    public double getDeltaX() {
        return deltaX;
    }

    /**
     * Get mouse movement since last frame (Y)
     */
    public double getDeltaY() {
        return deltaY;
    }

    /**
     * Check if mouse moved this frame
     */
    public boolean hasMoved() {
        return deltaX != 0 || deltaY != 0;
    }

    // ===== BUTTONS =====

    /**
     * Check if mouse button is currently held down
     */
    public boolean isButtonDown(int button) {
        if (button < 0 || button >= buttonsPressed.length) return false;
        return buttonsPressed[button];
    }

    /**
     * Check if mouse button was just pressed this frame
     */
    public boolean isButtonPressed(int button) {
        if (button < 0 || button >= buttonsPressed.length) return false;
        return buttonsPressed[button] && !buttonsPressedLast[button];
    }

    /**
     * Check if mouse button was just released this frame
     */
    public boolean isButtonReleased(int button) {
        if (button < 0 || button >= buttonsPressed.length) return false;
        return !buttonsPressed[button] && buttonsPressedLast[button];
    }

    // Convenience methods for common buttons
    public boolean isLeftButtonDown() { return isButtonDown(GLFW_MOUSE_BUTTON_LEFT); }
    public boolean isLeftButtonPressed() { return isButtonPressed(GLFW_MOUSE_BUTTON_LEFT); }
    public boolean isLeftButtonReleased() { return isButtonReleased(GLFW_MOUSE_BUTTON_LEFT); }

    public boolean isRightButtonDown() { return isButtonDown(GLFW_MOUSE_BUTTON_RIGHT); }
    public boolean isRightButtonPressed() { return isButtonPressed(GLFW_MOUSE_BUTTON_RIGHT); }
    public boolean isRightButtonReleased() { return isButtonReleased(GLFW_MOUSE_BUTTON_RIGHT); }

    public boolean isMiddleButtonDown() { return isButtonDown(GLFW_MOUSE_BUTTON_MIDDLE); }
    public boolean isMiddleButtonPressed() { return isButtonPressed(GLFW_MOUSE_BUTTON_MIDDLE); }
    public boolean isMiddleButtonReleased() { return isButtonReleased(GLFW_MOUSE_BUTTON_MIDDLE); }

    // ===== SCROLL =====

    /**
     * Get horizontal scroll offset this frame
     */
    public double getScrollX() {
        return scrollX;
    }

    /**
     * Get vertical scroll offset this frame
     */
    public double getScrollY() {
        return scrollY;
    }

    /**
     * Check if user scrolled this frame
     */
    public boolean hasScrolled() {
        return scrollX != 0 || scrollY != 0;
    }

    // ===== CURSOR CONTROL =====

    /**
     * Hide/show cursor
     */
    public void setCursorVisible(boolean visible) {
        if (visible && cursorHidden) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            cursorHidden = false;
        } else if (!visible && !cursorHidden) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
            cursorHidden = true;
        }
    }

    /**
     * Lock/unlock cursor (for FPS camera controls)
     */
    public void setCursorLocked(boolean locked) {
        if (locked && !cursorLocked) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            cursorLocked = true;
        } else if (!locked && cursorLocked) {
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            cursorLocked = false;
        }
    }

    /**
     * Set cursor position
     */
    public void setCursorPosition(double x, double y) {
        glfwSetCursorPos(window, x, y);
        this.x = x;
        this.y = y;
        this.lastX = x;
        this.lastY = y;
        this.deltaX = 0;
        this.deltaY = 0;
    }

    public boolean isCursorHidden() {
        return cursorHidden;
    }

    public boolean isCursorLocked() {
        return cursorLocked;
    }
}