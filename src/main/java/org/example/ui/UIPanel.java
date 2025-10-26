// src/main/java/org/example/ui/UIPanel.java
package org.example.ui;

import org.example.gfx.Renderer2D;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

/**
 * Container UI component for grouping other components
 * Composite Pattern: Can contain other UI components
 */
public final class UIPanel extends UIComponent {
    private final List<UIComponent> children = new ArrayList<>();
    private boolean drawBorder = true;
    private boolean clipChildren = false;

    public UIPanel(float x, float y, float width, float height) {
        super(x, y, width, height);
        backgroundColor.set(0.1f, 0.1f, 0.15f, 0.85f);
        borderColor.set(0.8f, 0.8f, 0.9f, 1f);
    }

    /**
     * Add a child component
     */
    public void addChild(UIComponent child) {
        children.add(child);
        child.setParent(this);
        child.setScreenDimensions(screenWidth, screenHeight);
    }

    /**
     * Remove a child component
     */
    public void removeChild(UIComponent child) {
        children.remove(child);
        child.setParent(null);
    }

    /**
     * Clear all children
     */
    public void clearChildren() {
        for (UIComponent child : children) {
            child.setParent(null);
        }
        children.clear();
    }

    @Override
    protected void onUpdate(double dt) {
        for (UIComponent child : children) {
            child.update(dt);
        }
    }

    @Override
    protected void onRender(Renderer2D renderer) {
        Vector2f renderPos = getRenderPosition();

        // Draw background
        if (backgroundColor.w > 0) {
            drawRect(renderer, renderPos.x, renderPos.y, size.x, size.y, backgroundColor);
        }

        // Draw border
        if (drawBorder) {
            drawBorder(renderer, renderPos.x, renderPos.y, size.x, size.y);
        }

        // TODO: Implement clipping if clipChildren is true
        // This would require OpenGL scissor test

        // Render children
        for (UIComponent child : children) {
            child.render(renderer);
        }
    }

    @Override
    protected void onClickInternal(float mouseX, float mouseY) {
        // Try children first (front to back)
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).onClick(mouseX, mouseY)) {
                return; // Click consumed
            }
        }
    }

    @Override
    public boolean onClick(float mouseX, float mouseY) {
        if (!visible || !enabled) return false;

        // Try children first
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i).onClick(mouseX, mouseY)) {
                return true;
            }
        }

        // Then check panel itself
        return super.onClick(mouseX, mouseY);
    }

    @Override
    public void setScreenDimensions(int width, int height) {
        super.setScreenDimensions(width, height);

        // Propagate to children
        for (UIComponent child : children) {
            child.setScreenDimensions(width, height);
        }
    }

    // ===== SETTERS =====

    public void setDrawBorder(boolean draw) {
        this.drawBorder = draw;
    }

    public void setClipChildren(boolean clip) {
        this.clipChildren = clip;
    }

    // ===== GETTERS =====

    public List<UIComponent> getChildren() {
        return new ArrayList<>(children);
    }

    public int getChildCount() {
        return children.size();
    }

    /**
     * Find child by type
     */
    public <T extends UIComponent> T findChild(Class<T> type) {
        for (UIComponent child : children) {
            if (type.isInstance(child)) {
                return type.cast(child);
            }
            if (child instanceof UIPanel panel) {
                T found = panel.findChild(type);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Find all children of type
     */
    public <T extends UIComponent> List<T> findAllChildren(Class<T> type) {
        List<T> results = new ArrayList<>();
        findAllChildrenRecursive(type, results);
        return results;
    }

    private <T extends UIComponent> void findAllChildrenRecursive(Class<T> type, List<T> results) {
        for (UIComponent child : children) {
            if (type.isInstance(child)) {
                results.add(type.cast(child));
            }
            if (child instanceof UIPanel panel) {
                panel.findAllChildrenRecursive(type, results);
            }
        }
    }
}