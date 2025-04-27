package org.example.game;

import org.example.engine.core.Component;
import org.example.engine.core.GameObject;
import org.example.engine.physics.BoxCollider;
import org.example.engine.physics.CollisionInfo;
import org.example.engine.physics.Rigidbody;
import org.example.engine.rendering.Sprite;
import org.example.engine.scene.Scene;
import org.example.engine.scene.SceneManager;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class EnemyAI extends Component {
    private enum State { PATROL, CHASE, RETURN }

    private State currentState = State.PATROL;
    private Vector3f[] patrolPoints;
    private int currentPatrolIndex = 0;
    private float moveSpeed = 100.0f;
    private float chaseSpeed = 150.0f;
    private float detectionRange = 20.0f;
    private float Health = 100;
    private GameObject player;
    private Sprite sprite;

    public EnemyAI(Vector3f[] patrolPoints) {
        this.patrolPoints = patrolPoints;
    }

    @Override
    protected void onInit() {
        sprite = getComponent(Sprite.class);

        // Find player
        //player = SceneManager.getInstance().getActiveScene().findGameObjectByTag("Player");

        // Add collider for interactions
        if (!getGameObject().hasComponent(BoxCollider.class)) {
            BoxCollider collider = new BoxCollider(16, 16);
            collider.setOnCollisionEnter(this::onCollision);
            getGameObject().addComponent(collider);
        }
    }

    @Override
    protected void onUpdate(float deltaTime) {

        if (player == null) {
            // Find player in the active scene
            Scene activeScene = SceneManager.getInstance().getActiveScene();
            if (activeScene != null) {
                player = activeScene.findGameObjectByTag("Player");
            }

            // If still null, just skip this frame
            if (player == null) return;
        }

        // Update state based on player distance
        updateState();

        // Act based on current state
        switch (currentState) {
            case PATROL:
                patrol(deltaTime);
                break;
            case CHASE:
                chasePlayer(deltaTime);
                break;
            case RETURN:
                returnToPatrol(deltaTime);
                break;
        }
    }

    private void updateState() {
        Vector3f playerPos = player.getTransform().getPosition();
        Vector3f enemyPos = getGameObject().getTransform().getPosition();

        float distanceToPlayer = new Vector2f(playerPos.x - enemyPos.x, playerPos.y - enemyPos.y).length();

        if (currentState == State.PATROL && distanceToPlayer < detectionRange) {
            // Player detected, start chasing
            currentState = State.CHASE;
        } else if (currentState == State.CHASE && distanceToPlayer > detectionRange * 1.5f) {
            // Player too far, return to patrol
            currentState = State.RETURN;
        }
    }

    private void patrol(float deltaTime) {
        if (patrolPoints.length == 0) return;

        Vector3f targetPoint = patrolPoints[currentPatrolIndex];
        Vector3f position = getGameObject().getTransform().getPosition();

        // Move towards target point
        Vector2f direction = new Vector2f(targetPoint.x - position.x, targetPoint.y - position.y);
        float distance = direction.length();

        if (distance < 10.0f) {
            // Reached point, move to next
            currentPatrolIndex = (currentPatrolIndex + 1) % patrolPoints.length;
        } else {
            // Continue moving
            direction.normalize().mul(moveSpeed * deltaTime);
            position.x += direction.x;
            position.y += direction.y;
            getGameObject().getTransform().setPosition(position);

            // Update sprite direction
            if (sprite != null) {
                sprite.setFlipX(direction.x < 0);
            }
        }
    }

    private void chasePlayer(float deltaTime) {
        Vector3f playerPos = player.getTransform().getPosition();
        Vector3f position = getGameObject().getTransform().getPosition();

        // Move towards player
        Vector2f direction = new Vector2f(playerPos.x - position.x, playerPos.y - position.y);
        direction.normalize().mul(chaseSpeed * deltaTime);

        position.x += direction.x;
        position.y += direction.y;
        getGameObject().getTransform().setPosition(position);

        // Update sprite direction
        if (sprite != null) {
            sprite.setFlipX(direction.x < 0);
        }
    }

    private void returnToPatrol(float deltaTime) {
        if (patrolPoints.length == 0) {
            currentState = State.PATROL;
            return;
        }

        Vector3f targetPoint = patrolPoints[currentPatrolIndex];
        Vector3f position = getGameObject().getTransform().getPosition();

        // Move towards patrol point
        Vector2f direction = new Vector2f(targetPoint.x - position.x, targetPoint.y - position.y);
        float distance = direction.length();

        if (distance < 10.0f) {
            // Reached patrol point
            currentState = State.PATROL;
        } else {
            // Continue moving
            direction.normalize().mul(moveSpeed * deltaTime);
            position.x += direction.x;
            position.y += direction.y;
            getGameObject().getTransform().setPosition(position);

            // Update sprite direction
            if (sprite != null) {
                sprite.setFlipX(direction.x < 0);
            }
        }
    }

    private void onCollision(CollisionInfo collision) {
        // Handle collision with player (damage, etc.)
        if (collision.colliderB.getGameObject().getName().equals("Player")) {
            // Deal damage to player
            PlayerHealths playerHealth = player.getComponent(PlayerHealths.class);
            if (playerHealth != null) {
                playerHealth.takeDamage(10);
            }

            // Knockback effect
            Vector3f playerPos = player.getTransform().getPosition();
            Vector3f enemyPos = getGameObject().getTransform().getPosition();
            Vector2f knockbackDir = new Vector2f(playerPos.x - enemyPos.x, playerPos.y - enemyPos.y).normalize();

            Rigidbody playerRb = player.getComponent(Rigidbody.class);
            if (playerRb != null) {
                playerRb.addForce(knockbackDir.x * 500, knockbackDir.y * 500);
            }
        }
        if (collision.colliderB.getGameObject().getName().equals("Bullet")){

        }

    }
}