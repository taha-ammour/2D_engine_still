package org.example.game;

import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.core.Transform;
import org.example.engine.rendering.Camera;
import org.example.engine.rendering.Sprite;
import org.example.engine.scene.SceneManager;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StarsComp extends Component {

    private int numStars = 100; // Number of stars
    private float minSpeed = 50.0f;  // Minimum star speed
    private float maxSpeed = 150.0f; // Maximum star speed
    private int starSize = 2;       // Size of each star sprite

    private final List<GameObject> starObjects = new ArrayList<>();
    private final Random random = new Random();

    private float windowWidth;
    private float windowHeight;

    @Override
    protected void onInit() {
        // Get screen dimensions from the camera
        Camera camera = SceneManager.getInstance().getActiveScene().getMainCamera();
        if (camera == null) {
            System.err.println("StarsComp requires a main camera in the scene.");
            return;
        }
        // Use virtual viewport if aspect ratio is maintained, otherwise full viewport
        windowWidth = camera.getMaintainAspectRatio() ? camera.getVirtualViewportWidth() : camera.getViewportWidth();
        windowHeight = camera.getMaintainAspectRatio() ? camera.getVirtualViewportHeight() : camera.getViewportHeight();


        // Create star game objects
        for (int i = 0; i < numStars; i++) {
            GameObject starGo = new GameObject("Star_" + i);

            // Create a simple sprite for the star (e.g., white square)
            Sprite starSprite = new Sprite(null, starSize, starSize);
            starSprite.setColor(0xFFFFFF, 1.0f); // White color
            starGo.addComponent(starSprite);

            // Assign random speed
            float speed = minSpeed + random.nextFloat() * (maxSpeed - minSpeed);
            starGo.addComponent(new StarMovement(speed));

            // Assign random initial position (mostly off-screen top)
            float initialX = random.nextFloat() * windowWidth;
            float initialY = -random.nextFloat() * windowHeight; // Start above the screen
            starGo.setPosition(initialX, initialY, -1.0f); // Place behind other elements

            // Add star as a child of the GameObject this component is attached to
            // This ensures stars move relative to the StarsComp object if it moves
            // If StarsComp is static, they just move within the screen space
            getGameObject().addChild(starGo);

            // Keep track of the star object
            starObjects.add(starGo);
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // Update camera dimensions in case of resize
        Camera camera = SceneManager.getInstance().getActiveScene().getMainCamera();
        if (camera != null) {
            windowWidth = camera.getMaintainAspectRatio() ? camera.getVirtualViewportWidth() : camera.getViewportWidth();
            windowHeight = camera.getMaintainAspectRatio() ? camera.getVirtualViewportHeight() : camera.getViewportHeight();
        } else {
            return; // Cannot update without camera info
        }


        for (GameObject starGo : starObjects) {
            if (!starGo.isActive()) continue;

            Transform transform = starGo.getTransform();
            StarMovement movement = starGo.getComponent(StarMovement.class);
            Sprite sprite = starGo.getComponent(Sprite.class); // Get sprite for height

            if (transform == null || movement == null || sprite == null) continue;

            // Move the star down
            Vector3f currentPos = transform.getLocalPosition(); // Use local position relative to parent
            currentPos.y += movement.speed * deltaTime;

            // If star goes below the screen, reset its position to the top
            // Add sprite height to check to ensure it's fully off-screen
            if (currentPos.y > windowHeight) {
                currentPos.y = -sprite.getHeight(); // Reset just above the screen
                currentPos.x = random.nextFloat() * windowWidth; // Randomize horizontal position
            }

            // Update the star's local position
            transform.setLocalPosition(currentPos);
        }
    }
}



