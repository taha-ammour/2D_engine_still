// src/main/java/org/example/engine/input/InputManager.java
package org.example.engine.input;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Command Pattern: Maps keys and mouse actions to commands
 * Single Responsibility: Manages input bindings
 * Open/Closed: Extensible through new command types, closed for modification
 */
public final class InputManager {
    private final Input input;
    private final Mouse mouse;

    // Keyboard bindings
    private final Map<Integer, Command> keyBindings = new HashMap<>();
    private final Map<Integer, Command> keyPressBindings = new HashMap<>();

    // Mouse button bindings
    private final Map<Integer, MouseCommand> mouseButtonBindings = new HashMap<>();
    private final Map<Integer, MouseCommand> mouseButtonPressBindings = new HashMap<>();

    // Mouse movement bindings
    private MouseCommand mouseMoveCommand;
    private MouseCommand mouseScrollCommand;

    public InputManager(Input input, Mouse mouse) {
        this.input = input;
        this.mouse = mouse;
    }

    // ===== KEYBOARD BINDINGS =====

    /**
     * Bind a key to a command that executes while held
     */
    public void bindKey(int key, Command command) {
        keyBindings.put(key, command);
    }

    /**
     * Bind a key to a command that executes once on press
     */
    public void bindKeyPress(int key, Command command) {
        keyPressBindings.put(key, command);
    }

    /**
     * Unbind a key
     */
    public void unbindKey(int key) {
        keyBindings.remove(key);
        keyPressBindings.remove(key);
    }

    // ===== MOUSE BUTTON BINDINGS =====

    /**
     * Bind a mouse button to a command that executes while held
     */
    public void bindMouseButton(int button, MouseCommand command) {
        mouseButtonBindings.put(button, command);
    }

    /**
     * Bind a mouse button to a command that executes once on press
     */
    public void bindMouseButtonPress(int button, MouseCommand command) {
        mouseButtonPressBindings.put(button, command);
    }

    /**
     * Unbind a mouse button
     */
    public void unbindMouseButton(int button) {
        mouseButtonBindings.remove(button);
        mouseButtonPressBindings.remove(button);
    }

    // ===== MOUSE MOVEMENT BINDINGS =====

    /**
     * Bind a command to mouse movement
     */
    public void bindMouseMove(MouseCommand command) {
        this.mouseMoveCommand = command;
    }

    /**
     * Bind a command to mouse scroll
     */
    public void bindMouseScroll(MouseCommand command) {
        this.mouseScrollCommand = command;
    }

    /**
     * Unbind mouse movement command
     */
    public void unbindMouseMove() {
        this.mouseMoveCommand = null;
    }

    /**
     * Unbind mouse scroll command
     */
    public void unbindMouseScroll() {
        this.mouseScrollCommand = null;
    }

    // ===== INPUT PROCESSING =====

    /**
     * Process all input (call once per frame)
     */
    public void processInput(double dt) {
        // Process held keys
        for (Map.Entry<Integer, Command> entry : keyBindings.entrySet()) {
            if (input.isKeyDown(entry.getKey())) {
                entry.getValue().execute(dt);
            }
        }

        // Process key presses
        for (Map.Entry<Integer, Command> entry : keyPressBindings.entrySet()) {
            if (input.isKeyPressed(entry.getKey())) {
                entry.getValue().execute(dt);
            }
        }

        // Process held mouse buttons
        for (Map.Entry<Integer, MouseCommand> entry : mouseButtonBindings.entrySet()) {
            if (mouse.isButtonDown(entry.getKey())) {
                entry.getValue().execute(mouse.getX(), mouse.getY(), dt);
            }
        }

        // Process mouse button presses
        for (Map.Entry<Integer, MouseCommand> entry : mouseButtonPressBindings.entrySet()) {
            if (mouse.isButtonPressed(entry.getKey())) {
                entry.getValue().execute(mouse.getX(), mouse.getY(), dt);
            }
        }

        // Process mouse movement
        if (mouseMoveCommand != null && mouse.hasMoved()) {
            mouseMoveCommand.execute(mouse.getX(), mouse.getY(), dt);
        }

        // Process mouse scroll
        if (mouseScrollCommand != null && mouse.hasScrolled()) {
            mouseScrollCommand.execute(mouse.getScrollX(), mouse.getScrollY(), dt);
        }
    }

    /**
     * Clear all bindings
     */
    public void clear() {
        keyBindings.clear();
        keyPressBindings.clear();
        mouseButtonBindings.clear();
        mouseButtonPressBindings.clear();
        mouseMoveCommand = null;
        mouseScrollCommand = null;
    }

    // ===== CONVENIENCE METHODS =====

    /**
     * Bind left mouse button
     */
    public void bindLeftClick(MouseCommand command) {
        bindMouseButtonPress(GLFW_MOUSE_BUTTON_LEFT, command);
    }

    /**
     * Bind right mouse button
     */
    public void bindRightClick(MouseCommand command) {
        bindMouseButtonPress(GLFW_MOUSE_BUTTON_RIGHT, command);
    }

    /**
     * Bind middle mouse button
     */
    public void bindMiddleClick(MouseCommand command) {
        bindMouseButtonPress(GLFW_MOUSE_BUTTON_MIDDLE, command);
    }

    // Getters for direct access if needed
    public Input getInput() {
        return input;
    }

    public Mouse getMouse() {
        return mouse;
    }
}