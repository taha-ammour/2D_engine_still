
// src/main/java/org/example/ecs/Component.java
package org.example.ecs;

/**
 * Strategy Pattern: Components define different behaviors
 */
public abstract class Component {
    protected GameObject owner;
    private boolean enabled = true;

    void setOwner(GameObject owner) {
        this.owner = owner;
    }

    protected void onAttach() {}
    protected void onDetach() {}

    public abstract void update(double dt);

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public GameObject getOwner() { return owner; }
}
