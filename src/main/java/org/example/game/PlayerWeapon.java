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

    @Override
    protected void onUpdate(float deltaTime) {
        // Update fire timer
        if (fireTimer > 0) {
            fireTimer -= deltaTime;
        }

        // Handle shooting
        InputSystem input = InputSystem.getInstance();
        if (input.isMouseButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT) && fireTimer <= 0) {
            shoot();
            fireTimer = fireRate;
        }
    }

    private void shoot() {
        // Get mouse position in world space
        InputSystem input = InputSystem.getInstance();
        double mouseX = input.getMouseX();
        double mouseY = input.getMouseY();

        // Convert to world coordinates
        Camera camera = SceneManager.getInstance().getActiveScene().getMainCamera();
        Vector2f worldPos = camera.screenToWorld((float)mouseX, (float)mouseY);

        // Calculate direction
        Vector3f playerPos = getGameObject().getTransform().getPosition();
        Vector2f direction = new Vector2f(worldPos.x - playerPos.x, worldPos.y - playerPos.y).normalize();

        // Create projectile
        GameObject projectile = new GameObject("Projectile");
        projectile.setPosition(playerPos.x, playerPos.y, 0);

        // Add sprite
        Sprite projectileSprite = new Sprite(null, 10, 10);
        projectileSprite.setColor(0xFF0000, 1.0f); // Red color
        projectile.addComponent(projectileSprite);

        // Add collider
        BoxCollider collider = new BoxCollider(10, 10);
        projectile.addComponent(collider);

        // Add projectile behavior
        ProjectileBehavior behavior = new ProjectileBehavior(direction, projectileSpeed);
        projectile.addComponent(behavior);

        // Add to scene
        SceneManager.getInstance().getActiveScene().addGameObject(projectile);
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