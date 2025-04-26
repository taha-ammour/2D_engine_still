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
    private float originalAspectRatio = 800f / 600f; // Store original aspect ratio
    private float zoom = 1.0f;
    private float nearPlane = 0.1f;
    private float farPlane = 1000.0f;
    private boolean maintainAspectRatio = true; // Flag to enable/disable aspect ratio preservation

    // Follow target
    private boolean followingTarget = false;
    private Vector3f targetPosition = new Vector3f();
    private float followSpeed = 0.1f;

    // Cached matrices
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f inverseViewMatrix = new Matrix4f();
    private final Matrix4f combinedMatrix = new Matrix4f();
    private final Matrix4f invertedMatrix = new Matrix4f();

    // Transformation caches
    private boolean viewMatrixDirty = true;
    private boolean projectionMatrixDirty = true;
    private boolean combinedMatrixDirty = true;

    // Direct position reference to avoid Transform dependency issues
    private Vector3f cameraPosition = new Vector3f(0, 0, 0);
    private float cameraRotation = 0.0f;

    // Virtual viewport coordinates (for letterboxing/pillarboxing)
    private float virtualViewportX = 0;
    private float virtualViewportY = 0;
    private float virtualViewportWidth = 800;
    private float virtualViewportHeight = 600;

    // Debug mode
    private boolean debugMode = false;

    /**
     * Create a camera with default settings
     */
    public Camera() {
        this.originalAspectRatio = viewportWidth / viewportHeight;
        updateProjectionMatrix();
    }

    /**
     * Create a camera with specified viewport dimensions
     */
    public Camera(float width, float height) {
        this.viewportWidth = width;
        this.viewportHeight = height;
        this.originalAspectRatio = width / height;
        this.virtualViewportWidth = width;
        this.virtualViewportHeight = height;
        updateProjectionMatrix();
    }

    @Override
    protected void onInit() {
        super.onInit();
        // Initialize camera position from transform if available
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
        float targetX = targetPosition.x - (virtualViewportWidth * zoom) / 2;
        float targetY = targetPosition.y - (virtualViewportHeight * zoom) / 2;

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
            combinedMatrixDirty = true;
        }
    }

    /**
     * Update the view matrix based on transform
     */
    private void updateViewMatrix() {
        if (!viewMatrixDirty) return;

        // Use our cached position instead of trying to get it from transform
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
        combinedMatrixDirty = true;
    }

    /**
     * Update the projection matrix
     */
    private void updateProjectionMatrix() {
        if (!projectionMatrixDirty) return;

        if (maintainAspectRatio) {
            // Calculate the virtual viewport to maintain aspect ratio
            float currentAspectRatio = viewportWidth / viewportHeight;

            if (currentAspectRatio > originalAspectRatio) {
                // Window is wider than original - add pillarboxing
                virtualViewportWidth = viewportHeight * originalAspectRatio;
                virtualViewportHeight = viewportHeight;
                virtualViewportX = (viewportWidth - virtualViewportWidth) / 2f;
                virtualViewportY = 0;
            } else {
                // Window is taller than original - add letterboxing
                virtualViewportWidth = viewportWidth;
                virtualViewportHeight = viewportWidth / originalAspectRatio;
                virtualViewportX = 0;
                virtualViewportY = (viewportHeight - virtualViewportHeight) / 2f;
            }

            // Create orthographic projection for the virtual viewport dimensions
            projectionMatrix.identity().ortho(
                    0, virtualViewportWidth,
                    virtualViewportHeight, 0,
                    -100, 100
            );

            if (debugMode) {
                System.out.println("Updated projection matrix with virtual viewport: " +
                        virtualViewportWidth + "x" + virtualViewportHeight +
                        " at " + virtualViewportX + "," + virtualViewportY);
            }
        } else {
            // Original behavior - directly use viewport dimensions
            projectionMatrix.identity().ortho(
                    0, viewportWidth,
                    viewportHeight, 0,
                    -100, 100
            );

            // Reset virtual viewport to match actual viewport
            virtualViewportX = 0;
            virtualViewportY = 0;
            virtualViewportWidth = viewportWidth;
            virtualViewportHeight = viewportHeight;

            if (debugMode) {
                System.out.println("Updated projection matrix with full viewport: " +
                        viewportWidth + "x" + viewportHeight);
            }
        }

        projectionMatrixDirty = false;
        combinedMatrixDirty = true;
    }

    /**
     * Update the combined view-projection matrix
     */
    private void updateCombinedMatrix() {
        if (!combinedMatrixDirty) return;

        // Ensure individual matrices are up to date
        updateViewMatrix();
        updateProjectionMatrix();

        // Calculate combined matrix and its inverse
        combinedMatrix.set(projectionMatrix).mul(viewMatrix);
        invertedMatrix.set(combinedMatrix).invert();

        combinedMatrixDirty = false;
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
     * This is an integrated solution that properly handles virtual viewport
     */
    public Vector2f screenToWorld(float screenX, float screenY) {
        // Ensure matrices are up to date
        updateCombinedMatrix();

        // Check if point is inside virtual viewport (when aspect ratio is maintained)
        if (maintainAspectRatio) {
            if (screenX < virtualViewportX || screenX > virtualViewportX + virtualViewportWidth ||
                    screenY < virtualViewportY || screenY > virtualViewportY + virtualViewportHeight) {
                if (debugMode) {
                    System.out.println("Click outside virtual viewport: " + screenX + "," + screenY);
                }
                // Return a point outside the view to indicate invalid location
                return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
            }

            // Adjust for virtual viewport origin
            screenX -= virtualViewportX;
            screenY -= virtualViewportY;
        }

        // Convert to normalized device coordinates (NDC)
        // For the projection matrix defined with ortho(0, width, height, 0, -1, 1)
        float ndcX = (2.0f * screenX / virtualViewportWidth) - 1.0f;
        float ndcY = 1.0f - (2.0f * screenY / virtualViewportHeight);

        // Create NDC point
        Vector3f ndcPoint = new Vector3f(ndcX, ndcY, 0);

        // Transform to world space using the inverted combined matrix
        Vector3f worldPoint = ndcPoint.mulPosition(invertedMatrix);

        if (debugMode) {
            System.out.println("Screen (" + screenX + "," + screenY + ") -> World (" +
                    worldPoint.x + "," + worldPoint.y + ")");
        }

        return new Vector2f(worldPoint.x, worldPoint.y);
    }

    /**
     * Convert raw screen coordinates (including virtual viewport adjustment)
     */
    public Vector2f rawScreenToWorld(float rawScreenX, float rawScreenY) {
        return screenToWorld(rawScreenX, rawScreenY);
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
        if (this.viewportWidth != width || this.viewportHeight != height) {
            this.viewportWidth = width;
            this.viewportHeight = height;
            projectionMatrixDirty = true;
            combinedMatrixDirty = true;

            if (debugMode) {
                System.out.println("Camera viewport resized to: " + width + "x" + height);
            }
        }
    }

    /**
     * Enable or disable aspect ratio preservation
     */
    public void setMaintainAspectRatio(boolean maintain) {
        if (this.maintainAspectRatio != maintain) {
            this.maintainAspectRatio = maintain;
            projectionMatrixDirty = true;
            combinedMatrixDirty = true;

            if (debugMode) {
                System.out.println("Aspect ratio preservation set to: " + maintain);
            }
        }
    }

    /**
     * Set the original aspect ratio to be maintained
     */
    public void setOriginalAspectRatio(float aspectRatio) {
        this.originalAspectRatio = aspectRatio;
        projectionMatrixDirty = true;
        combinedMatrixDirty = true;
    }

    /**
     * Get the current virtual viewport dimensions
     */
    public float getVirtualViewportWidth() {
        updateProjectionMatrix(); // Ensure virtual viewport is up to date
        return virtualViewportWidth;
    }

    public float getVirtualViewportHeight() {
        updateProjectionMatrix(); // Ensure virtual viewport is up to date
        return virtualViewportHeight;
    }

    public float getVirtualViewportX() {
        updateProjectionMatrix(); // Ensure virtual viewport is up to date
        return virtualViewportX;
    }

    public float getVirtualViewportY() {
        updateProjectionMatrix(); // Ensure virtual viewport is up to date
        return virtualViewportY;
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
            combinedMatrixDirty = true;
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
        float visibleWidth = virtualViewportWidth * zoom;
        float visibleHeight = virtualViewportHeight * zoom;

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
        combinedMatrixDirty = true;
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

    /**
     * Check if aspect ratio maintenance is enabled
     */
    public boolean getMaintainAspectRatio() {
        return maintainAspectRatio;
    }

    /**
     * Enable or disable debug mode
     */
    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
    }

    /**
     * Check if a screen point is within the virtual viewport
     */
    public boolean isPointInVirtualViewport(float screenX, float screenY) {
        return !maintainAspectRatio ||
                (screenX >= virtualViewportX &&
                        screenX <= virtualViewportX + virtualViewportWidth &&
                        screenY >= virtualViewportY &&
                        screenY <= virtualViewportY + virtualViewportHeight);
    }
}