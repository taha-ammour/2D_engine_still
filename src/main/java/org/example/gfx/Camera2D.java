// src/main/java/org/example/gfx/Camera2D.java
package org.example.gfx;

import org.joml.Matrix4f;
import org.joml.Vector2f;

public final class Camera2D {
    private final Matrix4f proj = new Matrix4f();
    private final Matrix4f view = new Matrix4f();
    private final Vector2f position = new Vector2f();
    private int width, height;


    public Camera2D(int width, int height) {
        // origin at (0,0) bottom-left; y up
        this.width = width;
        this.height = height;
        updateProjection();
        view.identity();
    }

    private void updateProjection() {
        // origin at (0,0) bottom-left; y up
        proj.identity().ortho(0, width, 0, height, -1f, 1f);
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
        view.identity().translate(-position.x, -position.y, 0f);
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        updateProjection();
    }

    public Matrix4f projection() { return proj; }
    public Matrix4f view() { return view; }
    public Vector2f getPosition() { return position; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }


}
