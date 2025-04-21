package org.example.engine.physics;

import org.example.engine.core.Component;
import org.joml.Vector2f;

/**
 * Rigidbody component that adds physics behavior to GameObjects.
 * Handles velocity, force, and physical properties.
 */
public class Rigidbody extends Component {
    // Physical properties
    private float mass = 1.0f;
    private float drag = 0.0f;
    private float angularDrag = 0.05f;
    private float restitution = 0.2f;
    private float friction = 0.2f;

    // State
    private Vector2f velocity = new Vector2f(0, 0);
    private float angularVelocity = 0.0f;
    private Vector2f force = new Vector2f(0, 0);
    private float torque = 0.0f;

    // Constraints
    private boolean freezePositionX = false;
    private boolean freezePositionY = false;
    private boolean freezeRotation = false;

    // Flags
    private boolean isKinematic = false;
    private boolean useGravity = true;
    private boolean isGrounded = false;

    @Override
    protected void onInit() {
        super.onInit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * Get the mass of this rigidbody
     */
    public float getMass() {
        return mass;
    }

    /**
     * Set the mass of this rigidbody
     */
    public void setMass(float mass) {
        this.mass = Math.max(0.0001f, mass);
    }

    /**
     * Get the velocity of this rigidbody
     */
    public Vector2f getVelocity() {
        return velocity;
    }

    /**
     * Set the velocity of this rigidbody
     */
    public void setVelocity(Vector2f velocity) {
        if (isKinematic) {
            return;
        }

        if (freezePositionX) {
            velocity.x = 0;
        }

        if (freezePositionY) {
            velocity.y = 0;
        }

        this.velocity.set(velocity);
    }

    /**
     * Set the velocity of this rigidbody
     */
    public void setVelocity(float x, float y) {
        setVelocity(new Vector2f(x, y));
    }

    /**
     * Get the angular velocity of this rigidbody (rotation speed)
     */
    public float getAngularVelocity() {
        return angularVelocity;
    }

    /**
     * Set the angular velocity of this rigidbody
     */
    public void setAngularVelocity(float angularVelocity) {
        if (isKinematic || freezeRotation) {
            return;
        }

        this.angularVelocity = angularVelocity;
    }

    /**
     * Get the accumulated force for this physics step
     */
    public Vector2f getForce() {
        return force;
    }

    /**
     * Clear all accumulated forces
     */
    public void clearForces() {
        force.set(0, 0);
        torque = 0;
    }

    /**
     * Add a force to this rigidbody
     */
    public void addForce(Vector2f force) {
        if (isKinematic) {
            return;
        }

        if (freezePositionX) {
            force.x = 0;
        }

        if (freezePositionY) {
            force.y = 0;
        }

        this.force.add(force);
    }

    /**
     * Add a force to this rigidbody
     */
    public void addForce(float x, float y) {
        addForce(new Vector2f(x, y));
    }

    /**
     * Add a force at a specific position, which may cause rotation
     */
    public void addForceAtPosition(Vector2f force, Vector2f position) {
        if (isKinematic) {
            return;
        }

        // Add linear force
        addForce(force);

        // Add torque if rotation is not frozen
        if (!freezeRotation) {
            // Calculate center of mass (assumed to be at transform position)
            Vector2f centerOfMass = new Vector2f(
                    getGameObject().getTransform().getPosition().x,
                    getGameObject().getTransform().getPosition().y
            );

            // Calculate lever arm
            Vector2f arm = new Vector2f(position).sub(centerOfMass);

            // Calculate torque (cross product in 2D)
            torque += arm.x * force.y - arm.y * force.x;
        }
    }

    /**
     * Add torque (rotational force) to this rigidbody
     */
    public void addTorque(float torque) {
        if (isKinematic || freezeRotation) {
            return;
        }

        this.torque += torque;
    }

    /**
     * Move the rigidbody to a position, ignoring physics
     */
    public void movePosition(Vector2f position) {
        getGameObject().getTransform().setPosition(position.x, position.y, getGameObject().getTransform().getPosition().z);
    }

    /**
     * Rotate the rigidbody to an angle, ignoring physics
     */
    public void moveRotation(float rotation) {
        getGameObject().getTransform().setRotation(rotation);
    }

    /**
     * Get the drag coefficient
     */
    public float getDrag() {
        return drag;
    }

    /**
     * Set the drag coefficient
     */
    public void setDrag(float drag) {
        this.drag = Math.max(0, drag);
    }

    /**
     * Get the angular drag coefficient
     */
    public float getAngularDrag() {
        return angularDrag;
    }

    /**
     * Set the angular drag coefficient
     */
    public void setAngularDrag(float angularDrag) {
        this.angularDrag = Math.max(0, angularDrag);
    }

    /**
     * Get the restitution (bounciness) coefficient
     */
    public float getRestitution() {
        return restitution;
    }

    /**
     * Set the restitution (bounciness) coefficient
     */
    public void setRestitution(float restitution) {
        this.restitution = Math.max(0, Math.min(1, restitution));
    }

    /**
     * Get the friction coefficient
     */
    public float getFriction() {
        return friction;
    }

    /**
     * Set the friction coefficient
     */
    public void setFriction(float friction) {
        this.friction = Math.max(0, friction);
    }

    /**
     * Check if position is frozen on X axis
     */
    public boolean isFreezePositionX() {
        return freezePositionX;
    }

    /**
     * Set whether position is frozen on X axis
     */
    public void setFreezePositionX(boolean freezePositionX) {
        this.freezePositionX = freezePositionX;

        if (freezePositionX) {
            velocity.x = 0;
            force.x = 0;
        }
    }

    /**
     * Check if position is frozen on Y axis
     */
    public boolean isFreezePositionY() {
        return freezePositionY;
    }

    /**
     * Set whether position is frozen on Y axis
     */
    public void setFreezePositionY(boolean freezePositionY) {
        this.freezePositionY = freezePositionY;

        if (freezePositionY) {
            velocity.y = 0;
            force.y = 0;
        }
    }

    /**
     * Check if rotation is frozen
     */
    public boolean isFreezeRotation() {
        return freezeRotation;
    }

    /**
     * Set whether rotation is frozen
     */
    public void setFreezeRotation(boolean freezeRotation) {
        this.freezeRotation = freezeRotation;

        if (freezeRotation) {
            angularVelocity = 0;
            torque = 0;
        }
    }

    /**
     * Check if this rigidbody is kinematic (not affected by forces)
     */
    public boolean isKinematic() {
        return isKinematic;
    }

    /**
     * Set whether this rigidbody is kinematic
     */
    public void setKinematic(boolean kinematic) {
        this.isKinematic = kinematic;

        if (isKinematic) {
            velocity.set(0, 0);
            angularVelocity = 0;
            force.set(0, 0);
            torque = 0;
        }
    }

    /**
     * Check if this rigidbody uses gravity
     */
    public boolean useGravity() {
        return useGravity;
    }

    /**
     * Set whether this rigidbody uses gravity
     */
    public void setUseGravity(boolean useGravity) {
        this.useGravity = useGravity;
    }

    /**
     * Check if this rigidbody is grounded (touching the ground)
     */
    public boolean isGrounded() {
        return isGrounded;
    }

    /**
     * Set whether this rigidbody is grounded
     */
    public void setGrounded(boolean grounded) {
        this.isGrounded = grounded;
    }
}