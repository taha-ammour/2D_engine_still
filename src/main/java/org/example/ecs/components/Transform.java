
// src/main/java/org/example/ecs/components/Transform.java
package org.example.ecs.components;

import org.example.ecs.Component;
import org.joml.Vector2f;

/**
 * Data Component: Holds position, rotation, scale
 */
public final class Transform extends Component {
    public final Vector2f position = new Vector2f();
    public final Vector2f scale = new Vector2f(1, 1);
    public float rotation = 0f;

    public Transform() {}

    public Transform(float x, float y) {
        position.set(x, y);
    }

    @Override
    public void update(double dt) {
        // Transform is just data, no update logic
    }

    public void translate(float dx, float dy) {
        position.add(dx, dy);
    }
}
