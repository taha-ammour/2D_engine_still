package org.example.engine.physics;

/**
 * Material for physics properties like friction and bounciness.
 */
public class PhysicsMaterial {
    private static final PhysicsMaterial DEFAULT = new PhysicsMaterial(0.2f, 0.2f);

    private final float friction;
    private final float restitution;

    /**
     * Create a physics material with the specified friction and restitution
     */
    public PhysicsMaterial(float friction, float restitution) {
        this.friction = Math.max(0, friction);
        this.restitution = Math.max(0, Math.min(1, restitution));
    }

    /**
     * Get the default physics material
     */
    public static PhysicsMaterial getDefault() {
        return DEFAULT;
    }

    /**
     * Create a physics material with zero friction and high restitution (bouncy)
     */
    public static PhysicsMaterial bouncy() {
        return new PhysicsMaterial(0.05f, 0.8f);
    }

    /**
     * Create a physics material with high friction and low restitution (rough)
     */
    public static PhysicsMaterial rough() {
        return new PhysicsMaterial(0.8f, 0.1f);
    }

    /**
     * Create a physics material with low friction and low restitution (smooth)
     */
    public static PhysicsMaterial smooth() {
        return new PhysicsMaterial(0.1f, 0.1f);
    }

    /**
     * Create a physics material with zero friction and zero restitution (ice)
     */
    public static PhysicsMaterial ice() {
        return new PhysicsMaterial(0.01f, 0.01f);
    }

    /**
     * Get the friction coefficient
     */
    public float getFriction() {
        return friction;
    }

    /**
     * Get the restitution (bounciness) coefficient
     */
    public float getRestitution() {
        return restitution;
    }
}