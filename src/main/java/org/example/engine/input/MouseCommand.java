// src/main/java/org/example/engine/input/MouseCommand.java
package org.example.engine.input;

/**
 * Command Pattern for mouse input
 * Allows binding mouse actions to game commands
 */
@FunctionalInterface
public interface MouseCommand {
    /**
     * Execute command with mouse position
     * @param mouseX Mouse X position (or scroll X for scroll commands)
     * @param mouseY Mouse Y position (or scroll Y for scroll commands)
     * @param dt Delta time
     */
    void execute(double mouseX, double mouseY, double dt);
}