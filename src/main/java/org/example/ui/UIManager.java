// src/main/java/org/example/ui/UIManager.java
package org.example.ui;

import org.example.gfx.Renderer2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Manages all UI components and handles input
 *
 * SOLID Principles:
 * - Single Responsibility: Manages UI lifecycle and input routing
 * - Open/Closed: Easy to extend with new component types
 * - Dependency Inversion: Depends on UIComponent abstraction
 */
public final class UIManager {
    private final List<UIComponent> components = new ArrayList<>();
    private final List<UIPanel> layers = new ArrayList<>();

    // Screen dimensions
    private int screenWidth;
    private int screenHeight;

    // Mouse state
    private float mouseX = 0;
    private float mouseY = 0;
    private boolean mousePressed = false;
    private boolean mouseReleased = false;

    public UIManager(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    /**
     * Add a standalone component
     */
    public void addComponent(UIComponent component) {
        components.add(component);
        component.setScreenDimensions(screenWidth, screenHeight);
    }

    /**
     * Remove a component
     */
    public void removeComponent(UIComponent component) {
        components.remove(component);
    }

    /**
     * Add a layer (panel that acts as a container)
     */
    public void addLayer(UIPanel layer) {
        layers.add(layer);
        layer.setScreenDimensions(screenWidth, screenHeight);
    }

    /**
     * Remove a layer
     */
    public void removeLayer(UIPanel layer) {
        layers.remove(layer);
    }

    /**
     * Clear all components
     */
    public void clear() {
        components.clear();
        layers.clear();
    }

    /**
     * Update screen dimensions (call when window resizes)
     */
    public void setScreenDimensions(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;

        // Update all components
        for (UIComponent component : components) {
            component.setScreenDimensions(width, height);
        }

        for (UIPanel layer : layers) {
            layer.setScreenDimensions(width, height);
        }
    }

    /**
     * Update all components
     */
    public void update(double dt) {
        // Update layers
        for (UIPanel layer : layers) {
            layer.update(dt);
        }

        // Update standalone components
        for (UIComponent component : components) {
            component.update(dt);
        }

        // Update button hover states
        updateHoverStates();

        // Handle clicks if mouse was released this frame
        if (mouseReleased) {
            handleClicks();
            mouseReleased = false;
        }

        mousePressed = false;
    }

    /**
     * Render all components (sorted by z-order)
     */
    public void render(Renderer2D renderer) {
        // Collect all components with z-order
        List<UIComponent> allComponents = new ArrayList<>();
        allComponents.addAll(layers);
        allComponents.addAll(components);

        // Sort by z-order
        allComponents.sort(Comparator.comparingInt(UIComponent::getZOrder));

        // Render in order
        for (UIComponent component : allComponents) {
            component.render(renderer);
        }
    }

    /**
     * Update hover states for buttons
     */
    private void updateHoverStates() {
        // Check layers
        for (UIPanel layer : layers) {
            updateHoverInPanel(layer);
        }

        // Check standalone components
        for (UIComponent component : components) {
            if (component instanceof UIButton button) {
                button.onMouseMove(mouseX, mouseY);
            }
        }
    }

    private void updateHoverInPanel(UIPanel panel) {
        for (UIComponent child : panel.getChildren()) {
            if (child instanceof UIButton button) {
                button.onMouseMove(mouseX, mouseY);
            } else if (child instanceof UIPanel childPanel) {
                updateHoverInPanel(childPanel);
            }
        }
    }

    /**
     * Handle click events (front to back)
     */
    private void handleClicks() {
        // Try layers first (front to back based on z-order)
        List<UIPanel> sortedLayers = new ArrayList<>(layers);
        sortedLayers.sort(Comparator.comparingInt(UIComponent::getZOrder).reversed());

        for (UIPanel layer : sortedLayers) {
            if (layer.onClick(mouseX, mouseY)) {
                return; // Click consumed
            }
        }

        // Try standalone components (front to back)
        List<UIComponent> sortedComponents = new ArrayList<>(components);
        sortedComponents.sort(Comparator.comparingInt(UIComponent::getZOrder).reversed());

        for (UIComponent component : sortedComponents) {
            if (component.onClick(mouseX, mouseY)) {
                return; // Click consumed
            }
        }
    }

    /**
     * Convert window coordinates to UI coordinates
     * In our case, they're the same since UI uses screen space
     * But this method is here for future coordinate system changes
     */
    private float convertMouseX(float windowX) {
        return windowX;
    }

    private float convertMouseY(float windowY) {
        // If using OpenGL coordinates (0,0 at bottom-left), flip Y
        // return screenHeight - windowY;

        // For now, assume UI coordinates match window coordinates
        return windowY;
    }

    // ===== INPUT HANDLING =====

    /**
     * Call when mouse moves
     */
    public void onMouseMove(float x, float y) {
        this.mouseX = convertMouseX(x);
        this.mouseY = convertMouseY(y);
    }

    /**
     * Call when mouse button is pressed
     */
    public void onMousePress(float x, float y) {
        this.mouseX = convertMouseX(x);
        this.mouseY = convertMouseY(y);
        this.mousePressed = true;

        // Notify buttons of press
        for (UIPanel layer : layers) {
            notifyPress(layer, mouseX, mouseY);
        }

        for (UIComponent component : components) {
            if (component instanceof UIButton button) {
                button.onMousePress(mouseX, mouseY);
            }
        }
    }

    private void notifyPress(UIPanel panel, float x, float y) {
        for (UIComponent child : panel.getChildren()) {
            if (child instanceof UIButton button) {
                button.onMousePress(x, y);
            } else if (child instanceof UIPanel childPanel) {
                notifyPress(childPanel, x, y);
            }
        }
    }

    /**
     * Call when mouse button is released
     */
    public void onMouseRelease(float x, float y) {
        this.mouseX = convertMouseX(x);
        this.mouseY = convertMouseY(y);
        this.mouseReleased = true;

        // Notify buttons of release
        for (UIPanel layer : layers) {
            notifyRelease(layer, mouseX, mouseY);
        }

        for (UIComponent component : components) {
            if (component instanceof UIButton button) {
                button.onMouseRelease(mouseX, mouseY);
            }
        }
    }

    private void notifyRelease(UIPanel panel, float x, float y) {
        for (UIComponent child : panel.getChildren()) {
            if (child instanceof UIButton button) {
                button.onMouseRelease(x, y);
            } else if (child instanceof UIPanel childPanel) {
                notifyRelease(childPanel, x, y);
            }
        }
    }

    // ===== COMPONENT SEARCH =====

    /**
     * Find component by type
     */
    public <T extends UIComponent> T findComponent(Class<T> type) {
        for (UIComponent component : components) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }
        }

        for (UIPanel layer : layers) {
            T found = layer.findChild(type);
            if (found != null) return found;
        }

        return null;
    }

    /**
     * Find all components of type
     */
    public <T extends UIComponent> List<T> findAllComponents(Class<T> type) {
        List<T> results = new ArrayList<>();

        for (UIComponent component : components) {
            if (type.isInstance(component)) {
                results.add(type.cast(component));
            }
        }

        for (UIPanel layer : layers) {
            results.addAll(layer.findAllChildren(type));
        }

        return results;
    }

    // ===== GETTERS =====

    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    public float getMouseX() { return mouseX; }
    public float getMouseY() { return mouseY; }
}