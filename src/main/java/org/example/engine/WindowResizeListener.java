// src/main/java/org/example/engine/WindowResizeListener.java
package org.example.engine;

/**
 * Interface Segregation Principle: Components that need to respond to window resize
 * Open/Closed Principle: New resize listeners can be added without modifying Window
 */
public interface WindowResizeListener {
    /**
     * Called when the window is resized
     * @param newWidth The new window width
     * @param newHeight The new window height
     */
    void onWindowResize(int newWidth, int newHeight);
}