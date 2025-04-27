package org.example.game.font;

import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.rendering.Camera;
import org.example.engine.scene.SceneManager;
import org.joml.Vector3f;

/**
 * This component explicitly updates UI elements to follow the camera
 * by directly updating their positions every frame.
 */
public class CameraUIManager extends Component {
    private Camera camera;
    private GameObject[] uiElements;
    private Vector3f[] relativePositions;
    private Vector3f lastCameraPosition = new Vector3f();

    /**
     * Create a new CameraUIManager to keep UI elements fixed to the screen
     * @param uiElements The GameObject array containing all UI elements to manage
     */
    public CameraUIManager(GameObject... uiElements) {
        this.uiElements = uiElements;
        this.relativePositions = new Vector3f[uiElements.length];

        // Initialize relative positions array
        for (int i = 0; i < uiElements.length; i++) {
            this.relativePositions[i] = new Vector3f();
        }
    }

    @Override
    protected void onInit() {
        // Get reference to camera
        if (SceneManager.getInstance().getActiveScene() != null) {
            camera = SceneManager.getInstance().getActiveScene().getMainCamera();

            if (camera != null) {
                // Store the initial camera position
                lastCameraPosition.set(camera.getPosition());

                // Calculate initial relative positions
                for (int i = 0; i < uiElements.length; i++) {
                    if (uiElements[i] != null) {
                        Vector3f elementPos = uiElements[i].getTransform().getPosition();
                        relativePositions[i].set(
                                elementPos.x - lastCameraPosition.x,
                                elementPos.y - lastCameraPosition.y,
                                elementPos.z
                        );
                    }
                }
            }
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // Get camera reference if not already obtained
        if (camera == null && SceneManager.getInstance().getActiveScene() != null) {
            camera = SceneManager.getInstance().getActiveScene().getMainCamera();
            if (camera != null) {
                lastCameraPosition.set(camera.getPosition());
            }
        }

        if (camera != null) {
            // Get current camera position
            Vector3f cameraPos = camera.getPosition();

            // Check if camera has moved
            if (!cameraPos.equals(lastCameraPosition)) {
                // Update all UI elements
                for (int i = 0; i < uiElements.length; i++) {
                    if (uiElements[i] != null) {
                        // Set new position based on relative position from camera
                        uiElements[i].setPosition(
                                cameraPos.x + relativePositions[i].x,
                                cameraPos.y + relativePositions[i].y,
                                relativePositions[i].z
                        );
                    }
                }

                // Update last camera position
                lastCameraPosition.set(cameraPos);
            }
        }
    }

    /**
     * Add a new UI element to be managed
     * @param element The GameObject to add
     * @param relativeX X position relative to camera center
     * @param relativeY Y position relative to camera center
     * @param z Z position (depth)
     */
    public void addUIElement(GameObject element, float relativeX, float relativeY, float z) {
        if (element == null) return;

        // Create new arrays with increased size
        GameObject[] newElements = new GameObject[uiElements.length + 1];
        Vector3f[] newRelativePositions = new Vector3f[relativePositions.length + 1];

        // Copy existing elements
        System.arraycopy(uiElements, 0, newElements, 0, uiElements.length);
        System.arraycopy(relativePositions, 0, newRelativePositions, 0, relativePositions.length);

        // Add new element
        newElements[uiElements.length] = element;
        newRelativePositions[relativePositions.length] = new Vector3f(relativeX, relativeY, z);

        // Update arrays
        uiElements = newElements;
        relativePositions = newRelativePositions;

        // Update element position if camera is available
        if (camera != null) {
            Vector3f cameraPos = camera.getPosition();
            element.setPosition(
                    cameraPos.x + relativeX,
                    cameraPos.y + relativeY,
                    z
            );
        }
    }
}