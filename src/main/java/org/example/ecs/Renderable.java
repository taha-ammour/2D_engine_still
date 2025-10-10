// src/main/java/org/example/ecs/Renderable.java
package org.example.ecs;

/**
 * Interface Segregation: Only renderable components implement this
 */
public interface Renderable {
    void render();
}