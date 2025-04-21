package org.example.engine.rendering;

import org.example.engine.core.Component;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * Camera component for rendering the scene from a specific viewpoint.
 * Provides view and projection matrices for the rendering system.
 */
public class Camera extends Component {
    // Camera properties
    private float viewportWidth = 800;
    private float viewportHeight = 600;
    private float zoom = 1.0f;
    private float nearPlane = 0.1f;
    private float farPlane = 1000.0f;

    // Follow target
    private boolean followingTarget = false;
    private Vector3f targetPosition = new Vector3f();
    private float followSpeed = 0.1f;

    // Cached matrices
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f inverseViewMatrix = new Matrix4f();

    // Transformation caches
    private boolean viewMatrixDirty = true;
    private boolean projectionMatrixDirty = true;

    // ADDED: Direct position reference to avoid Transform dependency issues
    private Vector3f cameraPosition = new Vector3f(0, 0, 0);
    private float cameraRotation = 0.0f;

    /**
     * Create a camera with default settings
     */
    public Camera() {
        updateProjectionMatrix();
    }

    /**
     * Create a camera with specified viewport dimensions
     */
    public Camera(float width, float height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        updateProjectionMatrix();
    }

    @Override
    protected void onInit() {
        super.onInit();
        // ADDED: Initialize camera position from transform if available
        if (getGameObject() != null && getGameObject().getTransform() != null) {
            cameraPosition.set(getGameObject().getTransform().getPosition());
            cameraRotation = getGameObject().getTransform().getRotation();
        }
        System.out.println("Camera initialized with position: " + cameraPosition);
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // Update position from transform if available
        if (getGameObject() != null && getGameObject().getTransform() != null) {
            cameraPosition.set(getGameObject().getTransform().getPosition());
            cameraRotation = getGameObject().getTransform().getRotation();
            viewMatrixDirty = true;
        }

        // Handle camera following target
        if (followingTarget) {
            updateFollowing(deltaTime);
        }
    }

    /**
     * Update camera position when following a target
     */
    private void updateFollowing(float deltaTime) {
        // Calculate target position with screen centering
        float targetX = targetPosition.x - (viewportWidth * zoom) / 2;
        float targetY = targetPosition.y - (viewportHeight * zoom) / 2;

        // Smoothly interpolate towards target position
        float followLerp = Math.min(1.0f, followSpeed * deltaTime * 60.0f); // Normalize by 60 FPS

        float newX = cameraPosition.x + (targetX - cameraPosition.x) * followLerp;
        float newY = cameraPosition.y + (targetY - cameraPosition.y) * followLerp;

        // Only update if significant movement
        if (Math.abs(newX - cameraPosition.x) > 0.01f || Math.abs(newY - cameraPosition.y) > 0.01f) {
            cameraPosition.x = newX;
            cameraPosition.y = newY;

            // Update transform if available
            if (getGameObject() != null && getGameObject().getTransform() != null) {
                getGameObject().getTransform().setPosition(cameraPosition.x, cameraPosition.y, cameraPosition.z);
            }

            viewMatrixDirty = true;
        }
    }

    /**
     * Update the view matrix based on transform
     */
    private void updateViewMatrix() {
        if (!viewMatrixDirty) return;

        // FIXED: Use our cached position instead of trying to get it from transform
        Vector3f position = cameraPosition;
        float rotation = cameraRotation;

        // Build the view matrix
        viewMatrix.identity()
                .translate(-position.x, -position.y, -position.z)
                .rotateZ(-rotation)
                .scale(zoom, zoom, 1.0f);

        // Calculate inverse view matrix for unproject operations
        inverseViewMatrix.set(viewMatrix).invert();

        viewMatrixDirty = false;
    }

    /**
     * Update the projection matrix
     */
    private void updateProjectionMatrix() {
        if (!projectionMatrixDirty) return;

        // For 2D games, use orthographic projection
        // Try changing the coordinate system to have (0,0) at top-left
        projectionMatrix.identity().ortho(
                0, viewportWidth,   // left to right
                viewportHeight, 0,  // top to bottom (flipped Y)
                -1, 1               // near/far planes closer to screen
        );

        projectionMatrixDirty = false;
    }

    /**
     * Get the view matrix
     */
    public Matrix4f getViewMatrix() {
        updateViewMatrix();
        return new Matrix4f(viewMatrix);
    }

    /**
     * Get the view matrix (without copy for performance)
     */
    public Matrix4f getViewMatrix(Matrix4f dest) {
        updateViewMatrix();
        return dest.set(viewMatrix);
    }

    /**
     * Get the projection matrix
     */
    public Matrix4f getProjectionMatrix() {
        updateProjectionMatrix();
        return new Matrix4f(projectionMatrix);
    }

    /**
     * Get the projection matrix (without copy for performance)
     */
    public Matrix4f getProjectionMatrix(Matrix4f dest) {
        updateProjectionMatrix();
        return dest.set(projectionMatrix);
    }

    /**
     * Convert screen coordinates to world coordinates
     */
    public Vector2f screenToWorld(float screenX, float screenY) {
        updateViewMatrix();

        // Convert screen coordinates to normalized device coordinates
        float ndcX = (2.0f * screenX) / viewportWidth - 1.0f;
        float ndcY = 1.0f - (2.0f * screenY) / viewportHeight;

        // Create point in NDC space with z=0 (near plane)
        Vector3f ndcPoint = new Vector3f(ndcX, ndcY, 0);

        // Convert to world space
        Matrix4f invViewProj = new Matrix4f();
        projectionMatrix.mul(viewMatrix, invViewProj);
        invViewProj.invert();

        Vector3f worldPoint = ndcPoint.mulPosition(invViewProj);

        return new Vector2f(worldPoint.x, worldPoint.y);
    }

    /**
     * Convert world coordinates to screen coordinates
     */
    public Vector2f worldToScreen(float worldX, float worldY) {
        updateViewMatrix();

        // Create point in world space
        Vector3f worldPoint = new Vector3f(worldX, worldY, 0);

        // Convert to NDC space
        Matrix4f viewProj = new Matrix4f();
        projectionMatrix.mul(viewMatrix, viewProj);

        Vector3f ndcPoint = worldPoint.mulPosition(viewProj);

        // Convert NDC to screen space
        float screenX = (ndcPoint.x + 1.0f) * 0.5f * viewportWidth;
        float screenY = (1.0f - ndcPoint.y) * 0.5f * viewportHeight;

        return new Vector2f(screenX, screenY);
    }

    /**
     * Set camera to follow a target position
     */
    public void follow(Vector3f target) {
        this.followingTarget = true;
        this.targetPosition.set(target);
    }

    /**
     * Set camera to follow a target position
     */
    public void follow(float x, float y, float z) {
        this.followingTarget = true;
        this.targetPosition.set(x, y, z);
    }

    /**
     * Stop camera from following target
     */
    public void stopFollowing() {
        this.followingTarget = false;
    }

    /**
     * Set the follow speed
     * @param speed Value between 0.01 (very slow) and 1.0 (instant)
     */
    public void setFollowSpeed(float speed) {
        this.followSpeed = Math.max(0.01f, Math.min(1.0f, speed));
    }

    /**
     * Check if the camera is following a target
     */
    public boolean isFollowing() {
        return followingTarget;
    }

    /**
     * Set the viewport size (e.g., when window resizes)
     */
    public void setViewportSize(float width, float height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        projectionMatrixDirty = true;
    }

    /**
     * Set camera zoom level
     * @param zoom Zoom factor (1.0 = normal, < 1.0 = zoom out, > 1.0 = zoom in)
     */
    public void setZoom(float zoom) {
        if (zoom <= 0) {
            zoom = 0.1f; // Prevent 0 or negative zoom
        }

        if (this.zoom != zoom) {
            this.zoom = zoom;
            viewMatrixDirty = true;
        }
    }

    /**
     * Get current zoom level
     */
    public float getZoom() {
        return zoom;
    }

    /**
     * Check if a point is visible in the camera's view
     */
    public boolean isInView(float x, float y, float radius) {
        // Calculate the visible area in world space
        float visibleWidth = viewportWidth * zoom;
        float visibleHeight = viewportHeight * zoom;

        // Check if the point is within the visible area (with radius for buffer)
        return x + radius >= cameraPosition.x &&
                x - radius <= cameraPosition.x + visibleWidth &&
                y + radius >= cameraPosition.y &&
                y - radius <= cameraPosition.y + visibleHeight;
    }

    /**
     * Get the camera position in world space
     */
    public Vector3f getPosition() {
        return new Vector3f(cameraPosition);
    }

    /**
     * Set camera position in world space
     */
    public void setPosition(float x, float y, float z) {
        cameraPosition.set(x, y, z);

        // Update transform if available
        if (getGameObject() != null && getGameObject().getTransform() != null) {
            getGameObject().getTransform().setPosition(x, y, z);
        }

        viewMatrixDirty = true;
    }

    /**
     * Get viewport width
     */
    public float getViewportWidth() {
        return viewportWidth;
    }

    /**
     * Get viewport height
     */
    public float getViewportHeight() {
        return viewportHeight;
    }
}