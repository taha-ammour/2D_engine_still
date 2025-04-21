package org.example.engine.ui;

import org.example.engine.core.GameObject;
import org.example.engine.rendering.RenderSystem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Central management system for UI elements.
 * Handles rendering, layout, and input forwarding to UI elements.
 */
public class UISystem {
    private static UISystem instance;

    // UI elements at the root level (not children of other UI elements)
    private final List<UIElement> rootElements = new ArrayList<>();

    // Current focused element for keyboard input
    private UIElement focusedElement;

    // The UI root game object (container for all UI elements)
    private GameObject uiRoot;

    // The rendering system reference
    private RenderSystem renderSystem;

    /**
     * Get the singleton instance
     */
    public static UISystem getInstance() {
        if (instance == null) {
            instance = new UISystem();
        }
        return instance;
    }

    /**
     * Private constructor for singleton pattern
     */
    private UISystem() {
    }

    /**
     * Initialize the UI system
     * @param renderSystem The render system to use
     */
    public void init(RenderSystem renderSystem) {
        this.renderSystem = renderSystem;

        // Create a root GameObject for all UI elements
        uiRoot = new GameObject("UI_Root");
    }

    /**
     * Update all UI elements
     * @param deltaTime Time since last frame
     */
    public void update(float deltaTime) {
        // Update root elements
        for (UIElement element : rootElements) {
            if (element.isActive()) {
                element.onUpdate(deltaTime);
            }
        }
    }

    /**
     * Render all UI elements
     */
    public void render() {
        if (renderSystem == null || uiRoot == null) return;

        // Submit the UI root for rendering
        // This will recursively render all UI elements
        renderSystem.submitGameObject(uiRoot);
    }

    /**
     * Add a UI element to the system
     * @param element UI element to add
     */
    public void addElement(UIElement element) {
        if (element == null || rootElements.contains(element)) return;

        // Add to root elements list
        rootElements.add(element);

        // Create GameObject and attach this element
        GameObject elementObj = new GameObject("UI_" + element.getClass().getSimpleName());
        elementObj.addComponent(element);

        // Add to UI root
        uiRoot.addChild(elementObj);
    }

    /**
     * Remove a UI element from the system
     * @param element UI element to remove
     */
    public void removeElement(UIElement element) {
        if (element == null) return;

        // Remove from root elements
        rootElements.remove(element);

        // If this was the focused element, clear focus
        if (element == focusedElement) {
            focusedElement = null;
        }

        // Find and remove the GameObject
        GameObject elementObj = element.getGameObject();
        if (elementObj != null) {
            uiRoot.removeChild(elementObj);
            elementObj.destroy();
        }
    }

    /**
     * Clear all UI elements
     */
    public void clearElements() {
        // Clear focused element
        focusedElement = null;

        // Destroy all UI game objects
        for (UIElement element : rootElements) {
            GameObject obj = element.getGameObject();
            if (obj != null) {
                obj.destroy();
            }
        }

        // Clear the list
        rootElements.clear();
    }

    /**
     * Set the currently focused UI element
     * @param element Element to focus (null to clear focus)
     */
    public void setFocusedElement(UIElement element) {
        // Clear focus on current element
        if (focusedElement != null && focusedElement != element) {
            focusedElement.setFocus(false);
        }

        // Set new focused element
        focusedElement = element;
    }

    /**
     * Get the currently focused UI element
     */
    public UIElement getFocusedElement() {
        return focusedElement;
    }

    /**
     * Find the UI element at the specified screen coordinates
     * @param screenX X coordinate
     * @param screenY Y coordinate
     * @return The topmost UI element at the position, or null if none
     */
    public UIElement findElementAt(float screenX, float screenY) {
        // Sort elements by Z index (descending) to get topmost first
        List<UIElement> sortedElements = new ArrayList<>(rootElements);
        sortedElements.sort(Comparator.comparingInt((UIElement e) -> e.zIndex).reversed());

        // Find first element that contains the point
        for (UIElement element : sortedElements) {
            if (element.isVisible()) {
                UIElement result = findElementAtRecursive(element, screenX, screenY);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * Recursively search for an element at the specified position
     */
    private UIElement findElementAtRecursive(UIElement element, float screenX, float screenY) {
        // Get element bounds
        UIElement result = null;

        // Check children first (they're on top)
        List<UIElement> sortedChildren = new ArrayList<>(element.children);
        sortedChildren.sort(Comparator.comparingInt((UIElement e) -> e.zIndex).reversed());

        for (UIElement child : sortedChildren) {
            if (child.isVisible()) {
                result = findElementAtRecursive(child, screenX, screenY);
                if (result != null) {
                    return result;
                }
            }
        }

        // Check if point is within this element's bounds
        if (isPointInElement(element, screenX, screenY)) {
            return element;
        }

        return null;
    }

    /**
     * Check if a point is within an element's bounds
     */
    private boolean isPointInElement(UIElement element, float x, float y) {
        if (!element.isVisible()) return false;

        org.joml.Vector2f pos = element.getGlobalPosition();
        return x >= pos.x && x <= pos.x + element.getWidth() &&
                y >= pos.y && y <= pos.y + element.getHeight();
    }

    /**
     * Get the UI root GameObject
     */
    public GameObject getUIRoot() {
        return uiRoot;
    }
}