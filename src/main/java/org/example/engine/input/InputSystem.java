package org.example.engine.input;

import static org.lwjgl.glfw.GLFW.*;

import java.util.HashMap;
import java.util.Map;

import org.example.engine.rendering.Camera;
import org.example.engine.scene.SceneManager;
import org.joml.Vector2f;

/**
 * System that handles user input from keyboard, mouse, and gamepads.
 * Provides easy access to current input state and events.
 */
public class InputSystem {
    private static InputSystem instance;

    // Keyboard state
    private final boolean[] keysDown = new boolean[GLFW_KEY_LAST + 1];
    private final boolean[] keysPressed = new boolean[GLFW_KEY_LAST + 1];
    private final boolean[] keysReleased = new boolean[GLFW_KEY_LAST + 1];
    private final boolean[] previousKeyState = new boolean[GLFW_KEY_LAST + 1];

    // Mouse state
    private double mouseX, mouseY;
    private double scrollX, scrollY;
    private final boolean[] mouseButtonsDown = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
    private final boolean[] mouseButtonsPressed = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
    private final boolean[] mouseButtonsReleased = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];
    private final boolean[] previousMouseButtonState = new boolean[GLFW_MOUSE_BUTTON_LAST + 1];

    // Virtual axes and buttons (for custom mappings and gamepads)
    private final Map<String, Float> virtualAxes = new HashMap<>();
    private final Map<String, Boolean> virtualButtons = new HashMap<>();

    // Singleton pattern
    public static InputSystem getInstance() {
        if (instance == null) {
            instance = new InputSystem();
        }
        return instance;
    }

    /**
     * Initialize the input system with GLFW callbacks
     * @param windowHandle GLFW window handle
     */
    public void init(long windowHandle) {
        // Set up keyboard callback
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if (key >= 0 && key <= GLFW_KEY_LAST) {
                boolean state = action != GLFW_RELEASE;
                keysDown[key] = state;

                if (action == GLFW_PRESS) {
                    keysPressed[key] = true;
                } else if (action == GLFW_RELEASE) {
                    keysReleased[key] = true;
                }
            }
        });

        // Set up mouse position callback
        glfwSetCursorPosCallback(windowHandle, (window, xpos, ypos) -> {
            mouseX = xpos;
            mouseY = ypos;
        });

        // Set up mouse button callback
        glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
            if (button >= 0 && button <= GLFW_MOUSE_BUTTON_LAST) {
                boolean state = action != GLFW_RELEASE;
                mouseButtonsDown[button] = state;

                if (action == GLFW_PRESS) {
                    mouseButtonsPressed[button] = true;
                } else if (action == GLFW_RELEASE) {
                    mouseButtonsReleased[button] = true;
                }
            }
        });

        // Set up scroll callback
        glfwSetScrollCallback(windowHandle, (window, xoffset, yoffset) -> {
            scrollX = xoffset;
            scrollY = yoffset;
        });
    }

    /**
     * Update the input state - must be called each frame
     */
    public void update() {
        // Reset per-frame states
        for (int i = 0; i < keysPressed.length; i++) {
            keysPressed[i] = false;
            keysReleased[i] = false;
            previousKeyState[i] = keysDown[i];
        }

        for (int i = 0; i < mouseButtonsPressed.length; i++) {
            mouseButtonsPressed[i] = false;
            mouseButtonsReleased[i] = false;
            previousMouseButtonState[i] = mouseButtonsDown[i];
        }

        // Reset scroll
        scrollX = 0;
        scrollY = 0;
    }

    /**
     * Check if a key is currently down
     */
    public boolean isKeyDown(int key) {
        if (key < 0 || key > GLFW_KEY_LAST) return false;
        return keysDown[key];
    }

    /**
     * Check if a key was pressed this frame
     */
    public boolean isKeyPressed(int key) {
        if (key < 0 || key > GLFW_KEY_LAST) return false;
        return keysPressed[key];
    }

    /**
     * Check if a key was released this frame
     */
    public boolean isKeyReleased(int key) {
        if (key < 0 || key > GLFW_KEY_LAST) return false;
        return keysReleased[key];
    }

    /**
     * Check if a mouse button is currently down
     */
    public boolean isMouseButtonDown(int button) {
        if (button < 0 || button > GLFW_MOUSE_BUTTON_LAST) return false;
        return mouseButtonsDown[button];
    }

    /**
     * Check if a mouse button was pressed this frame
     */
    public boolean isMouseButtonPressed(int button) {
        if (button < 0 || button > GLFW_MOUSE_BUTTON_LAST) return false;
        return mouseButtonsPressed[button];
    }

    /**
     * Check if a mouse button was released this frame
     */
    public boolean isMouseButtonReleased(int button) {
        if (button < 0 || button > GLFW_MOUSE_BUTTON_LAST) return false;
        return mouseButtonsReleased[button];
    }

    /**
     * Get the current horizontal position of the mouse
     */
    public double getMouseX() {
        return mouseX;
    }

    /**
     * Get the current vertical position of the mouse
     */
    public double getMouseY() {
        return mouseY;
    }

    /**
     * Get the adjusted mouse X position that accounts for virtual viewport
     * when aspect ratio maintenance is enabled
     */
    public double getAdjustedMouseX() {
        Camera camera = getActiveCamera();
        if (camera != null && camera.getMaintainAspectRatio()) {
            // Adjust mouse coordinates to account for the virtual viewport
            return mouseX - camera.getVirtualViewportX();
        }
        return mouseX;
    }

    /**
     * Get the adjusted mouse Y position that accounts for virtual viewport
     * when aspect ratio maintenance is enabled
     */
    public double getAdjustedMouseY() {
        Camera camera = getActiveCamera();
        if (camera != null && camera.getMaintainAspectRatio()) {
            // Adjust mouse coordinates to account for the virtual viewport
            return mouseY - camera.getVirtualViewportY();
        }
        return mouseY;
    }

    /**
     * Convert screen coordinates to world coordinates
     */
    public Vector2f screenToWorldCoordinates(double screenX, double screenY) {
        Camera camera = getActiveCamera();
        if (camera != null) {
            return camera.screenToWorld((float)screenX, (float)screenY);
        }
        return new Vector2f((float)screenX, (float)screenY);
    }

    /**
     * Get the current mouse position in world coordinates
     */
    public Vector2f getMouseWorldCoordinates() {
        return screenToWorldCoordinates(mouseX, mouseY);
    }

    /**
     * Get the active camera from the current scene
     */
    private Camera getActiveCamera() {
        if (SceneManager.getInstance() != null &&
                SceneManager.getInstance().getActiveScene() != null) {
            return SceneManager.getInstance().getActiveScene().getMainCamera();
        }
        return null;
    }

    /**
     * Get the horizontal scroll offset
     */
    public double getScrollX() {
        return scrollX;
    }

    /**
     * Get the vertical scroll offset
     */
    public double getScrollY() {
        return scrollY;
    }

    /**
     * Set a virtual axis value (for custom mappings or gamepads)
     */
    public void setVirtualAxis(String name, float value) {
        virtualAxes.put(name, value);
    }

    /**
     * Get a virtual axis value (for custom mappings or gamepads)
     */
    public float getVirtualAxis(String name) {
        return virtualAxes.getOrDefault(name, 0.0f);
    }

    /**
     * Set a virtual button state (for custom mappings or gamepads)
     */
    public void setVirtualButton(String name, boolean value) {
        virtualButtons.put(name, value);
    }

    /**
     * Get a virtual button state (for custom mappings or gamepads)
     */
    public boolean getVirtualButton(String name) {
        return virtualButtons.getOrDefault(name, false);
    }

    /**
     * Convert a key code to a string representation
     */
    public static String keyToString(int key) {
        switch (key) {
            case GLFW_KEY_SPACE: return "SPACE";
            case GLFW_KEY_APOSTROPHE: return "'";
            case GLFW_KEY_COMMA: return ",";
            case GLFW_KEY_MINUS: return "-";
            case GLFW_KEY_PERIOD: return ".";
            case GLFW_KEY_SLASH: return "/";
            case GLFW_KEY_0: return "0";
            case GLFW_KEY_1: return "1";
            case GLFW_KEY_2: return "2";
            case GLFW_KEY_3: return "3";
            case GLFW_KEY_4: return "4";
            case GLFW_KEY_5: return "5";
            case GLFW_KEY_6: return "6";
            case GLFW_KEY_7: return "7";
            case GLFW_KEY_8: return "8";
            case GLFW_KEY_9: return "9";
            case GLFW_KEY_SEMICOLON: return ";";
            case GLFW_KEY_EQUAL: return "=";
            case GLFW_KEY_A: return "A";
            case GLFW_KEY_B: return "B";
            case GLFW_KEY_C: return "C";
            case GLFW_KEY_D: return "D";
            case GLFW_KEY_E: return "E";
            case GLFW_KEY_F: return "F";
            case GLFW_KEY_G: return "G";
            case GLFW_KEY_H: return "H";
            case GLFW_KEY_I: return "I";
            case GLFW_KEY_J: return "J";
            case GLFW_KEY_K: return "K";
            case GLFW_KEY_L: return "L";
            case GLFW_KEY_M: return "M";
            case GLFW_KEY_N: return "N";
            case GLFW_KEY_O: return "O";
            case GLFW_KEY_P: return "P";
            case GLFW_KEY_Q: return "Q";
            case GLFW_KEY_R: return "R";
            case GLFW_KEY_S: return "S";
            case GLFW_KEY_T: return "T";
            case GLFW_KEY_U: return "U";
            case GLFW_KEY_V: return "V";
            case GLFW_KEY_W: return "W";
            case GLFW_KEY_X: return "X";
            case GLFW_KEY_Y: return "Y";
            case GLFW_KEY_Z: return "Z";
            case GLFW_KEY_LEFT_BRACKET: return "[";
            case GLFW_KEY_BACKSLASH: return "\\";
            case GLFW_KEY_RIGHT_BRACKET: return "]";
            case GLFW_KEY_ESCAPE: return "ESC";
            case GLFW_KEY_ENTER: return "ENTER";
            case GLFW_KEY_TAB: return "TAB";
            case GLFW_KEY_BACKSPACE: return "BACKSPACE";
            case GLFW_KEY_INSERT: return "INSERT";
            case GLFW_KEY_DELETE: return "DELETE";
            case GLFW_KEY_RIGHT: return "RIGHT";
            case GLFW_KEY_LEFT: return "LEFT";
            case GLFW_KEY_DOWN: return "DOWN";
            case GLFW_KEY_UP: return "UP";
            case GLFW_KEY_PAGE_UP: return "PAGE_UP";
            case GLFW_KEY_PAGE_DOWN: return "PAGE_DOWN";
            case GLFW_KEY_HOME: return "HOME";
            case GLFW_KEY_END: return "END";
            case GLFW_KEY_CAPS_LOCK: return "CAPS_LOCK";
            case GLFW_KEY_SCROLL_LOCK: return "SCROLL_LOCK";
            case GLFW_KEY_NUM_LOCK: return "NUM_LOCK";
            case GLFW_KEY_PRINT_SCREEN: return "PRINT_SCREEN";
            case GLFW_KEY_PAUSE: return "PAUSE";
            case GLFW_KEY_F1: return "F1";
            case GLFW_KEY_F2: return "F2";
            case GLFW_KEY_F3: return "F3";
            case GLFW_KEY_F4: return "F4";
            case GLFW_KEY_F5: return "F5";
            case GLFW_KEY_F6: return "F6";
            case GLFW_KEY_F7: return "F7";
            case GLFW_KEY_F8: return "F8";
            case GLFW_KEY_F9: return "F9";
            case GLFW_KEY_F10: return "F10";
            case GLFW_KEY_F11: return "F11";
            case GLFW_KEY_F12: return "F12";
            case GLFW_KEY_LEFT_SHIFT: return "LEFT_SHIFT";
            case GLFW_KEY_LEFT_CONTROL: return "LEFT_CONTROL";
            case GLFW_KEY_LEFT_ALT: return "LEFT_ALT";
            case GLFW_KEY_LEFT_SUPER: return "LEFT_SUPER";
            case GLFW_KEY_RIGHT_SHIFT: return "RIGHT_SHIFT";
            case GLFW_KEY_RIGHT_CONTROL: return "RIGHT_CONTROL";
            case GLFW_KEY_RIGHT_ALT: return "RIGHT_ALT";
            case GLFW_KEY_RIGHT_SUPER: return "RIGHT_SUPER";
            case GLFW_KEY_MENU: return "MENU";
            default: return "UNKNOWN";
        }
    }

    /**
     * Convert a mouse button code to a string representation
     */
    public static String mouseButtonToString(int button) {
        switch (button) {
            case GLFW_MOUSE_BUTTON_LEFT: return "LEFT_MOUSE";
            case GLFW_MOUSE_BUTTON_RIGHT: return "RIGHT_MOUSE";
            case GLFW_MOUSE_BUTTON_MIDDLE: return "MIDDLE_MOUSE";
            default: return "MOUSE_" + button;
        }
    }
}