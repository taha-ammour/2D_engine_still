// src/main/java/org/example/ecs/GameObject.java
package org.example.ecs;

import java.util.*;

/**
 * Composite Pattern: GameObject composed of Components
 * Single Responsibility: Manages components, delegates behavior
 */
public class GameObject {
    private final String name;
    private final Map<Class<? extends Component>, Component> components = new HashMap<>();
    private boolean active = true;

    public GameObject(String name) {
        this.name = name;
    }

    public <T extends Component> void addComponent(T component) {
        components.put(component.getClass(), component);
        component.setOwner(this);
        component.onAttach();
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(Class<T> type) {
        return (T) components.get(type);
    }

    public <T extends Component> boolean hasComponent(Class<T> type) {
        return components.containsKey(type);
    }

    public <T extends Component> void removeComponent(Class<T> type) {
        Component component = components.remove(type);
        if (component != null) {
            component.onDetach();
        }
    }

    // ✅ NEW: Expose components for collision system
    public Collection<Component> getComponents() {
        return components.values();
    }

    public void update(double dt) {
        if (!active) return;
        for (Component c : components.values()) {
            if (c.isEnabled()) {
                c.update(dt);
            }
        }
    }

    public void render() {
        if (!active) return;
        for (Component c : components.values()) {
            if (c.isEnabled() && c instanceof Renderable r) {
                r.render();
            }
        }
    }

    public String getName() { return name; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}