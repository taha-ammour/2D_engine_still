package org.example.engine.core;

/**
 * Base class for all components that can be attached to GameObjects.
 * Components contain the actual implementation logic for behaviors.
 */
public abstract class Component {
    private GameObject gameObject;
    private boolean active = true;
    private boolean initialized = false;

    /**
     * Called when the component is first attached to a GameObject
     */
    protected void onInit() {
        initialized = true;
    }

    /**
     * Called every frame during the update loop
     */
    protected void onUpdate(float deltaTime) {
        // Override in subclasses
    }

    /**
     * Called after all regular updates are processed
     */
    protected void onLateUpdate(float deltaTime) {
        // Override in subclasses
    }

    /**
     * Called when the component is enabled
     */
    protected void onEnable() {
        // Override in subclasses
    }

    /**
     * Called when the component is disabled
     */
    protected void onDisable() {
        // Override in subclasses
    }

    /**
     * Called when the component or its GameObject is destroyed
     */
    protected void onDestroy() {
        // Override in subclasses
    }

    /**
     * Set the GameObject this component is attached to
     */
    void setGameObject(GameObject gameObject) {
        this.gameObject = gameObject;
    }

    /**
     * Get the GameObject this component is attached to
     */
    public GameObject getGameObject() {
        return gameObject;
    }

    /**
     * Check if this component is active
     */
    public boolean isActive() {
        return active && (gameObject == null || gameObject.isActive());
    }

    /**
     * Enable or disable this component
     */
    public void setActive(boolean active) {
        if (this.active == active) return;

        this.active = active;

        if (initialized) {
            if (active) {
                onEnable();
            } else {
                onDisable();
            }
        }
    }

    /**
     * Get a component from the attached GameObject
     */
    protected <T extends Component> T getComponent(Class<T> componentClass) {
        if (gameObject == null) return null;
        return gameObject.getComponent(componentClass);
    }

    /**
     * Add a component to the attached GameObject
     */
    protected <T extends Component> T addComponent(T component) {
        if (gameObject == null) return null;
        return gameObject.addComponent(component);
    }

    /**
     * Check if the attached GameObject has a component
     */
    protected boolean hasComponent(Class<? extends Component> componentClass) {
        if (gameObject == null) return false;
        return gameObject.hasComponent(componentClass);
    }

    /**
     * Remove a component from the attached GameObject
     */
    protected <T extends Component> boolean removeComponent(Class<T> componentClass) {
        if (gameObject == null) return false;
        return gameObject.removeComponent(componentClass);
    }

    /**
     * Get the Transform component of the attached GameObject
     * Changed from protected to public to match Renderable interface
     */
    public Transform getTransform() {
        if (gameObject == null) return null;
        return gameObject.getTransform();
    }
}