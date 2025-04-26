package org.example.game;

import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.input.InputSystem;
import org.example.engine.physics.BoxCollider;
import org.example.engine.rendering.Camera;
import org.example.engine.rendering.Sprite;
import org.example.engine.scene.SceneManager;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class PlayerWeapon extends Component {
    private float fireRate = 0.5f; // Seconds between shots
    private float fireTimer = 0;
    private float projectileSpeed = 400f;
    private boolean debugMode = true;  // Set to true to help diagnose issues

    @Override
    protected void onUpdate(float deltaTime) {
        // Update fire timer
        if (fireTimer > 0) {
            fireTimer -= deltaTime;
        }

        // Handle shooting with direct camera handling
        InputSystem input = InputSystem.getInstance();
        if (input.isMouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT) && fireTimer <= 0) {
            // Direct approach for handling mouse clicks
            handleMouseShoot();
            fireTimer = fireRate;
        }
    }

    /**
     * Direct approach to handle mouse shooting that explicitly works with the camera
     */
    private void handleMouseShoot() {
        // Get mouse position directly
        InputSystem input = InputSystem.getInstance();
        double mouseX = input.getMouseX();
        double mouseY = input.getMouseY();

        // Get the active camera directly
        Camera camera = SceneManager.getInstance().getActiveScene().getMainCamera();

        if (camera == null) {
            System.err.println("Cannot shoot: No active camera found");
            return;
        }

        // Check if click is within the virtual viewport (if aspect ratio is maintained)
        if (camera.getMaintainAspectRatio() && !camera.isPointInVirtualViewport((float)mouseX, (float)mouseY)) {
            if (debugMode) {
                System.out.println("Click outside virtual viewport: " + mouseX + "," + mouseY);
                System.out.println("Virtual viewport: " + camera.getVirtualViewportX() + "," +
                        camera.getVirtualViewportY() + " - " +
                        (camera.getVirtualViewportX() + camera.getVirtualViewportWidth()) + "," +
                        (camera.getVirtualViewportY() + camera.getVirtualViewportHeight()));
            }
            return; // Don't shoot if clicked outside the virtual viewport
        }

        // Convert to world coordinates using the camera directly
        Vector2f worldPos = camera.screenToWorld((float)mouseX, (float)mouseY);

        // Sanity check - don't shoot if the world position is invalid
        if (worldPos.x == Float.MAX_VALUE || worldPos.y == Float.MAX_VALUE) {
            if (debugMode) {
                System.out.println("Invalid world position, cannot shoot");
            }
            return;
        }

        if (debugMode) {
            System.out.println("Shooting from screen (" + mouseX + "," + mouseY +
                    ") to world (" + worldPos.x + "," + worldPos.y + ")");
        }

        // Calculate direction
        Vector3f playerPos = getGameObject().getTransform().getPosition();
        Vector2f direction = new Vector2f(worldPos.x - playerPos.x, worldPos.y - playerPos.y);

        // Check for zero direction (can happen if world conversion is incorrect)
        if (direction.length() < 0.01f) {
            if (debugMode) {
                System.out.println("Direction too short, cannot shoot");
            }
            return;
        }

        // Normalize direction
        direction.normalize();

        // Create projectile in the scene directly
        createProjectile(playerPos, direction);
    }

    /**
     * Create a projectile with the given direction
     */
    private void createProjectile(Vector3f playerPos, Vector2f direction) {
        // Create projectile object first
        GameObject projectile = new GameObject("Projectile");
        projectile.setPosition(playerPos.x, playerPos.y, 0);

        // Add to scene BEFORE adding components
        SceneManager.getInstance().getActiveScene().addGameObject(projectile);

        // Now add sprite and components
        Sprite projectileSprite = new Sprite(null, 10, 10);
        projectileSprite.setColor(0xFF0000, 1.0f); // Red color
        projectile.addComponent(projectileSprite);

        // Add collider
        BoxCollider collider = new BoxCollider(10, 10);
        projectile.addComponent(collider);

        // Add projectile behavior
        ProjectileBehavior behavior = new ProjectileBehavior(direction, projectileSpeed);
        projectile.addComponent(behavior);

        if (debugMode) {
            System.out.println("Created projectile with direction: " + direction.x + "," + direction.y);
        }
    }
}

// Projectile behavior component
class ProjectileBehavior extends Component {
    private Vector2f direction;
    private float speed;
    private float lifetime = 2.0f; // Seconds before auto-destruction

    public ProjectileBehavior(Vector2f direction, float speed) {
        this.direction = new Vector2f(direction);
        this.speed = speed;
    }

    @Override
    protected void onUpdate(float deltaTime) {
        // Move projectile
        Vector3f position = getGameObject().getTransform().getPosition();
        position.x += direction.x * speed * deltaTime;
        position.y += direction.y * speed * deltaTime;
        getGameObject().getTransform().setPosition(position);

        // Handle lifetime
        lifetime -= deltaTime;
        if (lifetime <= 0) {
            getGameObject().destroy();
        }
    }
}