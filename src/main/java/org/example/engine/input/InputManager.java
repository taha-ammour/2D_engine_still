// src/main/java/org/example/engine/input/InputManager.java
package org.example.engine.input;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Command Pattern: Maps keys to commands
 * Single Responsibility: Manages input bindings
 */
public final class InputManager {
    private final Input input;
    private final Map<Integer, Command> keyBindings = new HashMap<>();
    private final Map<Integer, Command> keyPressBindings = new HashMap<>();

    public InputManager(Input input) {
        this.input = input;
    }

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

    public void unbindKey(int key) {
        keyBindings.remove(key);
        keyPressBindings.remove(key);
    }

    public void processInput(double dt) {
        // Process held keys
        for (Map.Entry<Integer, Command> entry : keyBindings.entrySet()) {
            if (input.isKeyDown(entry.getKey())) {
                entry.getValue().execute(dt);
            }
        }

        // Process key presses (triggers once per press)
        for (Map.Entry<Integer, Command> entry : keyPressBindings.entrySet()) {
            if (input.isKeyPressed(entry.getKey())) {
                entry.getValue().execute(dt);
            }
        }
    }

    public void clear() {
        keyBindings.clear();
        keyPressBindings.clear();
    }
}