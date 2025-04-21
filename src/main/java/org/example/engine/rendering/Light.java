package org.example.engine.rendering;

import org.joml.Vector3f;

/**
 * Represents a light source in the scene.
 */
public class Light {
    // Light types
    public static final int TYPE_DIRECTIONAL = 0;
    public static final int TYPE_POINT = 1;
    public static final int TYPE_SPOT = 2;

    // Light properties
    public Vector3f position = new Vector3f(0, 0, 0);
    public Vector3f color = new Vector3f(1, 1, 1);
    public Vector3f direction = new Vector3f(0, 0, -1);
    public float intensity = 1.0f;

    // Attenuation factors
    public float constant = 1.0f;
    public float linear = 0.09f;
    public float quadratic = 0.032f;

    // Spotlight properties
    public float cutoff = (float) Math.cos(Math.toRadians(12.5f));
    public float outerCutoff = (float) Math.cos(Math.toRadians(17.5f));

    // Light type
    public int type = TYPE_POINT;

    /**
     * Create a new light with default settings
     */
    public Light() {
    }

    /**
     * Create a new directional light
     */
    public static Light createDirectionalLight(Vector3f direction, Vector3f color, float intensity) {
        Light light = new Light();
        light.type = TYPE_DIRECTIONAL;
        light.direction.set(direction).normalize();
        light.color.set(color);
        light.intensity = intensity;
        return light;
    }

    /**
     * Create a new point light
     */
    public static Light createPointLight(Vector3f position, Vector3f color, float intensity,
                                         float constant, float linear, float quadratic) {
        Light light = new Light();
        light.type = TYPE_POINT;
        light.position.set(position);
        light.color.set(color);
        light.intensity = intensity;
        light.constant = constant;
        light.linear = linear;
        light.quadratic = quadratic;
        return light;
    }

    /**
     * Create a new spotlight
     */
    public static Light createSpotLight(Vector3f position, Vector3f direction, Vector3f color,
                                        float intensity, float cutoffDegrees, float outerCutoffDegrees) {
        Light light = new Light();
        light.type = TYPE_SPOT;
        light.position.set(position);
        light.direction.set(direction).normalize();
        light.color.set(color);
        light.intensity = intensity;
        light.cutoff = (float) Math.cos(Math.toRadians(cutoffDegrees));
        light.outerCutoff = (float) Math.cos(Math.toRadians(outerCutoffDegrees));
        return light;
    }
}