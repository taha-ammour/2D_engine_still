package org.example.engine.core;

import org.example.engine.events.EventListener;
import org.joml.Vector3f;

import java.util.*;

/**
 * Core GameObject class using a component-based architecture.
 * GameObjects are containers for Components which define behavior.
 */
public class GameObject implements EventListener {
    // Identity
    private UUID id = UUID.randomUUID();
    private String name = "GameObject";
    private String tag = "";

    // Hierarchy
    private GameObject parent;
    private final List<GameObject> children = new ArrayList<>();

    // Components
    private final Map<Class<? extends Component>, Component> components = new HashMap<>();
    private final List<Component> componentList = new ArrayList<>(); // For ordered iteration

    // State
    private boolean active = true;
    private boolean destroyed = false;

    // Core components that all GameObjects have
    private final Transform transform;

    /**
     * Create a new GameObject
     */
    public GameObject() {
        // Add transform component by default
        transform = new Transform();
        addComponent(transform);
    }

    /**
     * Create a new GameObject with a name
     */
    public GameObject(String name) {
        this();
        this.name = name;
    }

    /**
     * Initialize the GameObject and all its components
     */
    public void init() {
        for (Component component : componentList) {
            component.onInit();
        }

        // Initialize children
        for (GameObject child : children) {
            child.init();
        }
    }

    /**
     * Update the GameObject and all its components
     */
    public void update(float deltaTime) {
        if (!active) return;

        // Update components
        for (Component component : componentList) {
            if (component.isActive()) {
                component.onUpdate(deltaTime);
            }
        }

        // Update children
        for (GameObject child : new ArrayList<>(children)) {
            child.update(deltaTime);
        }
    }

    /**
     * Late update is called after all regular updates
     */
    public void lateUpdate(float deltaTime) {
        if (!active) return;

        // Late update components
        for (Component component : componentList) {
            if (component.isActive()) {
                component.onLateUpdate(deltaTime);
            }
        }

        // Late update children
        for (GameObject child : new ArrayList<>(children)) {
            child.lateUpdate(deltaTime);
        }
    }

    /**
     * Add a component to this GameObject
     */
    public <T extends Component> T addComponent(T component) {
        if (component == null) return null;

        // Set the GameObject reference
        component.setGameObject(this);

        // Add to the component map
        components.put(component.getClass(), component);
        componentList.add(component);

        // Call the component's initialize method
        if (active) {
            component.onInit();
        }

        return component;
    }

    /**
     * Get a component by type
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return (T) components.get(componentClass);
    }

    /**
     * Check if this GameObject has a component of the specified type
     */
    public boolean hasComponent(Class<? extends Component> componentClass) {
        return components.containsKey(componentClass);
    }

    /**
     * Remove a component by type
     */
    public <T extends Component> boolean removeComponent(Class<T> componentClass) {
        Component component = components.remove(componentClass);
        if (component != null) {
            componentList.remove(component);
            component.onDestroy();
            return true;
        }
        return false;
    }

    /**
     * Get all components
     */
    public List<Component> getComponents() {
        return Collections.unmodifiableList(componentList);
    }

    /**
     * Add a child GameObject
     */
    public void addChild(GameObject child) {
        if (child == null || children.contains(child) || child == this) return;

        // Remove from previous parent
        if (child.parent != null) {
            child.parent.children.remove(child);
        }

        // Add to children
        children.add(child);
        child.parent = this;

        // Update transform hierarchy
        child.transform.setParent(transform);
    }

    /**
     * Remove a child GameObject
     */
    public boolean removeChild(GameObject child) {
        if (child != null && children.remove(child)) {
            child.parent = null;
            child.transform.setParent(null);
            return true;
        }
        return false;
    }

    /**
     * Get all children
     */
    public List<GameObject> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Set the parent of this GameObject
     */
    public void setParent(GameObject parent) {
        if (parent == null) {
            // Remove from current parent
            if (this.parent != null) {
                this.parent.removeChild(this);
            }
        } else {
            parent.addChild(this);
        }
    }

    /**
     * Get the parent of this GameObject
     */
    public GameObject getParent() {
        return parent;
    }

    /**
     * Destroy this GameObject and all its components
     */
    public void destroy() {
        if (destroyed) return;

        // Mark as destroyed
        destroyed = true;
        active = false;

        // Destroy all components
        for (Component component : new ArrayList<>(componentList)) {
            component.onDestroy();
        }

        // Destroy all children
        for (GameObject child : new ArrayList<>(children)) {
            child.destroy();
        }

        // Remove from parent
        if (parent != null) {
            parent.removeChild(this);
        }

        // Clear collections
        components.clear();
        componentList.clear();
        children.clear();
    }

    // Getters and setters

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isActive() {
        return active && !destroyed && (parent == null || parent.isActive());
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public Transform getTransform() {
        return transform;
    }

    /**
     * Set position in world space
     */
    public void setPosition(float x, float y, float z) {
        transform.setPosition(x, y, z);
    }

    /**
     * Set position in world space
     */
    public void setPosition(Vector3f position) {
        transform.setPosition(position);
    }

    /**
     * Set scale
     */
    public void setScale(float x, float y, float z) {
        transform.setScale(x, y, z);
    }

    /**
     * Set rotation (in radians)
     */
    public void setRotation(float rotation) {
        transform.setRotation(rotation);
    }

    /**
     * Handle events that this GameObject receives
     */
    @Override
    public void onEvent(Object event) {
        // Propagate event to all components
        for (Component component : componentList) {
            if (component.isActive() && component instanceof EventListener) {
                ((EventListener) component).onEvent(event);
            }
        }
    }

    @Override
    public String toString() {
        return "GameObject[name=" + name + ", id=" + id + "]";
    }
}