package org.example.game;

import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.rendering.Camera;
import org.example.engine.scene.SceneManager;
import org.joml.Vector3f;

/**
 * Component that makes a camera follow a target (typically the player).
 * This should be attached to the camera GameObject.
 */
public class CameraFollow extends Component {
    private GameObject target;
    private String targetTag = "Player"; // Default to following the player
    private float followSpeed = 3.0f;    // Adjustable follow speed
    private float offsetX = 0;           // Horizontal offset
    private float offsetY = 0;           // Vertical offset
    private float minDistanceToMove = 0.1f; // Minimum distance before camera moves

    private Camera camera;
    private boolean debugging = true;  // Enable debugging by default to diagnose issues

    @Override
    protected void onInit() {
        // Get the camera component
        camera = getGameObject().getComponent(Camera.class);
        if (camera == null) {
            System.err.println("CameraFollow requires a Camera component on the same GameObject");
        }

        // We'll find the target in onUpdate instead of here
        // This way we avoid null references if things aren't ready yet

        if (debugging) {
            System.out.println("CameraFollow component initialized");
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // If we don't have a target yet, try to find it by tag
        if (target == null && targetTag != null && !targetTag.isEmpty()) {
            // Make sure we have an active scene before trying to find the target
            if (SceneManager.getInstance().getActiveScene() != null) {
                target = SceneManager.getInstance().getActiveScene().findGameObjectByTag(targetTag);
                if (target != null && debugging) {
                    System.out.println("CameraFollow: Found target with tag '" + targetTag + "'");
                }
            }
        }

        updateFollow(deltaTime);
    }

    /**
     * Update the camera position to follow the target
     */
    private void updateFollow(float deltaTime) {
        // Skip if no camera or target
        if (camera == null || target == null || !target.isActive()) {
            return;
        }

        // Get target position
        Vector3f targetPosition = target.getTransform().getPosition();

        // Calculate desired camera position (with offset)
        float desiredX = targetPosition.x + offsetX;
        float desiredY = targetPosition.y + offsetY;

        // Get current camera position
        Vector3f cameraPos = getGameObject().getTransform().getPosition();

        // Calculate distance to move
        float dx = desiredX - cameraPos.x;
        float dy = desiredY - cameraPos.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);


        // Only move if the distance is significant
        if (distance > minDistanceToMove) {
            // Calculate new position with smooth damping
            float lerpFactor = Math.min(1.0f, followSpeed * deltaTime);
            float newX = cameraPos.x + dx * lerpFactor;
            float newY = cameraPos.y + dy * lerpFactor;

            // Update camera position
            getGameObject().getTransform().setPosition(newX, newY, cameraPos.z);

            // Enable camera follow (in case it was disabled)
            if (camera.isFollowing()) {
                camera.follow(targetPosition);
            }
            setOffset(-camera.getVirtualViewportWidth()/2, -camera.getViewportHeight()/2); // Example: Camera 100 pixels above player

            if (debugging) {
                System.out.println("Camera following: " + newX + ", " + newY);
            }
        }
    }

    /**
     * Set the target GameObject to follow
     */
    public void setTarget(GameObject target) {
        this.target = target;
    }

    /**
     * Set the tag of the target to follow
     */
    public void setTargetTag(String tag) {
        this.targetTag = tag;
        // We'll find the target during the first update instead of here
        // This avoids NullPointerException if the scene isn't active yet
    }

    /**
     * Set the follow speed (higher = faster camera movement)
     */
    public void setFollowSpeed(float speed) {
        this.followSpeed = Math.max(0.1f, speed);
    }

    /**
     * Set camera offset from target
     */
    public void setOffset(float x, float y) {
        this.offsetX = x;
        this.offsetY = y;
    }

    /**
     * Enable debug logging
     */
    public void setDebugging(boolean debug) {
        this.debugging = debug;
    }
}