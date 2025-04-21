package org.example.engine.core;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Transform component that handles position, rotation, and scale of a GameObject.
 * Also manages parent-child relationships for hierarchical transformations.
 */
public class Transform extends Component {
    // Local transformation properties
    private final Vector3f localPosition = new Vector3f(0, 0, 0);
    private final Vector3f localScale = new Vector3f(1, 1, 1);
    private float localRotation = 0.0f; // in radians (around Z axis for 2D)

    // World transformation properties (cached)
    private final Vector3f worldPosition = new Vector3f(0, 0, 0);
    private final Vector3f worldScale = new Vector3f(1, 1, 1);
    private float worldRotation = 0.0f;

    // Transformation matrix (cached)
    private final Matrix4f localMatrix = new Matrix4f();
    private final Matrix4f worldMatrix = new Matrix4f();

    // Hierarchy
    private Transform parent = null;
    private boolean transformDirty = true;

    // Event flags
    private boolean positionChanged = false;
    private boolean rotationChanged = false;
    private boolean scaleChanged = false;

    /**
     * Create a new Transform with default values
     */
    public Transform() {
        updateLocalMatrix();
        updateWorldMatrix();
    }

    /**
     * Create a new Transform with the specified position
     */
    public Transform(float x, float y, float z) {
        localPosition.set(x, y, z);
        updateLocalMatrix();
        updateWorldMatrix();
    }

    /**
     * Set the parent transform
     */
    public void setParent(Transform parent) {
        if (this.parent == parent) return;

        this.parent = parent;
        transformDirty = true;
    }

    /**
     * Get the parent transform
     */
    public Transform getParent() {
        return parent;
    }

    /**
     * Set the local position
     */
    public void setLocalPosition(float x, float y, float z) {
        if (localPosition.x == x && localPosition.y == y && localPosition.z == z) return;

        localPosition.set(x, y, z);
        transformDirty = true;
        positionChanged = true;
    }

    /**
     * Set the local position
     */
    public void setLocalPosition(Vector3f position) {
        setLocalPosition(position.x, position.y, position.z);
    }

    /**
     * Get the local position
     */
    public Vector3f getLocalPosition() {
        return new Vector3f(localPosition);
    }

    /**
     * Set the local scale
     */
    public void setLocalScale(float x, float y, float z) {
        if (localScale.x == x && localScale.y == y && localScale.z == z) return;

        localScale.set(x, y, z);
        transformDirty = true;
        scaleChanged = true;
    }

    /**
     * Set local scale from a Vector3f
     */
    public void setLocalScale(Vector3f scale) {
        setLocalScale(scale.x, scale.y, scale.z);
    }

    /**
     * Set uniform local scale
     */
    public void setLocalScale(float scale) {
        setLocalScale(scale, scale, scale);
    }

    /**
     * Get the local scale
     */
    public Vector3f getLocalScale() {
        return new Vector3f(localScale);
    }

    /**
     * Set the local rotation (around Z axis for 2D)
     */
    public void setLocalRotation(float radians) {
        if (localRotation == radians) return;

        localRotation = radians;
        transformDirty = true;
        rotationChanged = true;
    }

    /**
     * Get the local rotation
     */
    public float getLocalRotation() {
        return localRotation;
    }

    /**
     * Set the world position directly
     */
    public void setPosition(float x, float y, float z) {
        if (parent == null) {
            setLocalPosition(x, y, z);
        } else {
            // Convert world position to local position
            Vector3f worldPos = new Vector3f(x, y, z);
            Vector3f localPos = worldToLocalPosition(worldPos);
            setLocalPosition(localPos);
        }
    }

    /**
     * Set the world position directly
     */
    public void setPosition(Vector3f position) {
        setPosition(position.x, position.y, position.z);
    }

    /**
     * Get the world position
     */
    public Vector3f getPosition() {
        updateMatricesIfNeeded();
        return new Vector3f(worldPosition);
    }

    /**
     * Set the world scale directly
     */
    public void setScale(float x, float y, float z) {
        if (parent == null) {
            setLocalScale(x, y, z);
        } else {
            // Convert world scale to local scale
            Vector3f worldScale = new Vector3f(x, y, z);
            Vector3f parentWorldScale = parent.getScale();
            Vector3f localScale = new Vector3f(
                    worldScale.x / parentWorldScale.x,
                    worldScale.y / parentWorldScale.y,
                    worldScale.z / parentWorldScale.z
            );
            setLocalScale(localScale);
        }
    }

    /**
     * Set uniform world scale
     */
    public void setScale(float scale) {
        setScale(scale, scale, scale);
    }

    /**
     * Get the world scale
     */
    public Vector3f getScale() {
        updateMatricesIfNeeded();
        return new Vector3f(worldScale);
    }

    /**
     * Set the world rotation directly
     */
    public void setRotation(float radians) {
        if (parent == null) {
            setLocalRotation(radians);
        } else {
            // Convert world rotation to local rotation
            float parentWorldRotation = parent.getRotation();
            float localRotation = radians - parentWorldRotation;
            setLocalRotation(localRotation);
        }
    }

    /**
     * Get the world rotation
     */
    public float getRotation() {
        updateMatricesIfNeeded();
        return worldRotation;
    }

    /**
     * Get the local transformation matrix
     */
    public Matrix4f getLocalMatrix() {
        updateLocalMatrix();
        return new Matrix4f(localMatrix);
    }

    /**
     * Get the world transformation matrix
     */
    public Matrix4f getWorldMatrix() {
        updateMatricesIfNeeded();
        return new Matrix4f(worldMatrix);
    }

    /**
     * Convert a point from local space to world space
     */
    public Vector3f localToWorldPosition(Vector3f localPoint) {
        updateMatricesIfNeeded();
        Vector3f worldPoint = new Vector3f(localPoint);
        worldMatrix.transformPosition(worldPoint);
        return worldPoint;
    }

    /**
     * Convert a point from world space to local space
     */
    public Vector3f worldToLocalPosition(Vector3f worldPoint) {
        updateMatricesIfNeeded();

        Matrix4f inverseWorldMatrix = new Matrix4f(worldMatrix).invert();
        Vector3f localPoint = new Vector3f(worldPoint);
        inverseWorldMatrix.transformPosition(localPoint);

        return localPoint;
    }

    /**
     * Move the transform by the specified amount in local space
     */
    public void translate(float x, float y, float z) {
        localPosition.add(x, y, z);
        transformDirty = true;
        positionChanged = true;
    }

    /**
     * Rotate the transform by the specified amount
     */
    public void rotate(float radians) {
        localRotation += radians;
        transformDirty = true;
        rotationChanged = true;
    }

    /**
     * Scale the transform by the specified factors
     */
    public void scale(float x, float y, float z) {
        localScale.mul(x, y, z);
        transformDirty = true;
        scaleChanged = true;
    }

    /**
     * Update the transform matrices if needed
     */
    private void updateMatricesIfNeeded() {
        if (transformDirty) {
            updateLocalMatrix();
            updateWorldMatrix();
            transformDirty = false;
        }
    }

    /**
     * Update the local transformation matrix
     */
    private void updateLocalMatrix() {
        localMatrix.identity()
                .translate(localPosition)
                .rotateZ(localRotation)
                .scale(localScale);
    }

    /**
     * Update the world transformation matrix
     */
    private void updateWorldMatrix() {
        if (parent == null) {
            worldMatrix.set(localMatrix);
            worldPosition.set(localPosition);
            worldScale.set(localScale);
            worldRotation = localRotation;
        } else {
            // First get the parent's world matrix
            Matrix4f parentWorldMatrix = parent.getWorldMatrix();

            // Combine with local matrix
            worldMatrix.set(parentWorldMatrix).mul(localMatrix);

            // Extract position from world matrix
            worldPosition.set(0, 0, 0);
            worldMatrix.transformPosition(worldPosition);

            // Combine rotations
            worldRotation = parent.getRotation() + localRotation;

            // Combine scales
            Vector3f parentScale = parent.getScale();
            worldScale.set(
                    localScale.x * parentScale.x,
                    localScale.y * parentScale.y,
                    localScale.z * parentScale.z
            );
        }
    }

    /**
     * Check if the position has changed since last frame
     */
    public boolean hasPositionChanged() {
        boolean changed = positionChanged;
        positionChanged = false;
        return changed;
    }

    /**
     * Check if the rotation has changed since last frame
     */
    public boolean hasRotationChanged() {
        boolean changed = rotationChanged;
        rotationChanged = false;
        return changed;
    }

    /**
     * Check if the scale has changed since last frame
     */
    public boolean hasScaleChanged() {
        boolean changed = scaleChanged;
        scaleChanged = false;
        return changed;
    }

    /**
     * Look at a specific point (rotate to face it)
     */
    public void lookAt(float x, float y) {
        Vector3f pos = getPosition();
        float dx = x - pos.x;
        float dy = y - pos.y;
        float angle = (float) Math.atan2(dy, dx);
        setRotation(angle);
    }

    @Override
    public String toString() {
        return String.format("Transform[pos=(%.2f, %.2f, %.2f), rot=%.2f, scale=(%.2f, %.2f, %.2f)]",
                worldPosition.x, worldPosition.y, worldPosition.z,
                worldRotation, worldScale.x, worldScale.y, worldScale.z);
    }
}