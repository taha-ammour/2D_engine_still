package org.example.engine.physics;

import org.example.engine.core.Component;
import org.joml.Vector2f;
import java.util.UUID;

/**
 * Base collider component for physics interactions.
 * Defines the shape and properties of a physical object.
 */
public abstract class Collider extends Component {
    private final UUID id = UUID.randomUUID();
    private int layer = 0;
    private boolean isTrigger = false;
    private Vector2f offset = new Vector2f(0, 0);

    // Cached bounds
    protected Vector2f min = new Vector2f();
    protected Vector2f max = new Vector2f();
    protected boolean boundsDirty = true;

    // Material properties
    private PhysicsMaterial material;

    // Collision callbacks
    private CollisionCallback onCollisionEnterCallback;
    private CollisionCallback onCollisionStayCallback;
    private CollisionCallback onCollisionExitCallback;

    // Trigger callbacks
    private CollisionCallback onTriggerEnterCallback;
    private CollisionCallback onTriggerStayCallback;
    private CollisionCallback onTriggerExitCallback;

    @Override
    protected void onInit() {
        super.onInit();

        // Add to physics system
        PhysicsSystem.getInstance().addCollider(this);

        // Set default material
        material = PhysicsMaterial.getDefault();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Remove from physics system
        PhysicsSystem.getInstance().removeCollider(this);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // If transform has changed, update bounds
        if (getTransform().hasPositionChanged() || getTransform().hasScaleChanged()) {
            boundsDirty = true;
        }
    }

    /**
     * Get the unique ID of this collider
     */
    public UUID getId() {
        return id;
    }

    /**
     * Get the physics layer of this collider
     */
    public int getLayer() {
        return layer;
    }

    /**
     * Set the physics layer of this collider
     */
    public void setLayer(int layer) {
        this.layer = Math.max(0, Math.min(31, layer));
    }

    /**
     * Check if this collider is a trigger
     */
    public boolean isTrigger() {
        return isTrigger;
    }

    /**
     * Set whether this collider is a trigger
     */
    public void setTrigger(boolean trigger) {
        this.isTrigger = trigger;
    }

    /**
     * Get the offset of this collider from the transform position
     */
    public Vector2f getOffset() {
        return new Vector2f(offset);
    }

    /**
     * Set the offset of this collider from the transform position
     */
    public void setOffset(Vector2f offset) {
        this.offset.set(offset);
        boundsDirty = true;
    }

    /**
     * Set the offset of this collider from the transform position
     */
    public void setOffset(float x, float y) {
        this.offset.set(x, y);
        boundsDirty = true;
    }

    /**
     * Get the physics material of this collider
     */
    public PhysicsMaterial getMaterial() {
        return material;
    }

    /**
     * Set the physics material of this collider
     */
    public void setMaterial(PhysicsMaterial material) {
        this.material = material != null ? material : PhysicsMaterial.getDefault();
    }

    /**
     * Get the minimum bounds of this collider in world space
     */
    public Vector2f getMin() {
        if (boundsDirty) {
            updateBounds();
        }
        return new Vector2f(min);
    }

    /**
     * Get the maximum bounds of this collider in world space
     */
    public Vector2f getMax() {
        if (boundsDirty) {
            updateBounds();
        }
        return new Vector2f(max);
    }

    /**
     * Update the bounds of this collider
     */
    protected abstract void updateBounds();

    /**
     * Check if this collider contains a point
     */
    public abstract boolean containsPoint(Vector2f point);

    /**
     * Get the closest point on this collider to a given point
     */
    public abstract Vector2f getClosestPoint(Vector2f point);

    /**
     * Perform a raycast against this collider
     */
    public abstract RaycastHit raycast(Vector2f origin, Vector2f direction, float maxDistance);

    /**
     * Called when a collision occurs with another collider
     */
    public void onCollisionEnter(CollisionInfo collision) {
        if (onCollisionEnterCallback != null) {
            onCollisionEnterCallback.onCollision(collision);
        }
    }

    /**
     * Called when a collision is maintained with another collider
     */
    public void onCollisionStay(CollisionInfo collision) {
        if (onCollisionStayCallback != null) {
            onCollisionStayCallback.onCollision(collision);
        }
    }

    /**
     * Called when a collision ends with another collider
     */
    public void onCollisionExit(CollisionInfo collision) {
        if (onCollisionExitCallback != null) {
            onCollisionExitCallback.onCollision(collision);
        }
    }

    /**
     * Called when this trigger collider is entered by another collider
     */
    public void onTriggerEnter(CollisionInfo collision) {
        if (onTriggerEnterCallback != null) {
            onTriggerEnterCallback.onCollision(collision);
        }
    }

    /**
     * Called when another collider stays in this trigger collider
     */
    public void onTriggerStay(CollisionInfo collision) {
        if (onTriggerStayCallback != null) {
            onTriggerStayCallback.onCollision(collision);
        }
    }

    /**
     * Called when another collider exits this trigger collider
     */
    public void onTriggerExit(CollisionInfo collision) {
        if (onTriggerExitCallback != null) {
            onTriggerExitCallback.onCollision(collision);
        }
    }

    /**
     * Set the callback for collision enter events
     */
    public void setOnCollisionEnter(CollisionCallback callback) {
        this.onCollisionEnterCallback = callback;
    }

    /**
     * Set the callback for collision stay events
     */
    public void setOnCollisionStay(CollisionCallback callback) {
        this.onCollisionStayCallback = callback;
    }

    /**
     * Set the callback for collision exit events
     */
    public void setOnCollisionExit(CollisionCallback callback) {
        this.onCollisionExitCallback = callback;
    }

    /**
     * Set the callback for trigger enter events
     */
    public void setOnTriggerEnter(CollisionCallback callback) {
        this.onTriggerEnterCallback = callback;
    }

    /**
     * Set the callback for trigger stay events
     */
    public void setOnTriggerStay(CollisionCallback callback) {
        this.onTriggerStayCallback = callback;
    }

    /**
     * Set the callback for trigger exit events
     */
    public void setOnTriggerExit(CollisionCallback callback) {
        this.onTriggerExitCallback = callback;
    }

    /**
     * Interface for collision callbacks
     */
    public interface CollisionCallback {
        void onCollision(CollisionInfo collision);
    }
}