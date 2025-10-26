package org.example.physics;

import org.example.ecs.GameObject;
import org.example.ecs.Component;
import org.example.ecs.components.Transform;
import org.example.ecs.components.RigidBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class CollisionSystemTest {

    private CollisionSystem collisionSystem;
    private GameObject player;
    private GameObject ground;
    private BoxCollider playerCollider;
    private BoxCollider groundCollider;

    @BeforeEach
    void setUp() {
        collisionSystem = new CollisionSystem();

        // Create player GameObject with components
        player = new GameObject("Player");
        Transform playerTransform = new Transform(100, 100);
        RigidBody playerRb = new RigidBody();
        playerCollider = new BoxCollider(32, 48, CollisionLayer.PLAYER);

        player.addComponent(playerTransform);
        player.addComponent(playerRb);
        player.addComponent(playerCollider);

        // Create ground GameObject
        ground = new GameObject("Ground");
        Transform groundTransform = new Transform(0, 0);
        groundCollider = new BoxCollider(1000, 32, CollisionLayer.GROUND);
        groundCollider.isStatic = true;

        ground.addComponent(groundTransform);
        ground.addComponent(groundCollider);
    }

    @Test
    @DisplayName("Should register colliders successfully")
    void testRegisterColliders() {
        collisionSystem.register(playerCollider);
        collisionSystem.register(groundCollider);

        assertEquals(2, collisionSystem.getAllColliders().size());
    }

    @Test
    @DisplayName("Should detect collision when objects overlap")
    void testCollisionDetection() {
        // Setup: Place player so it overlaps with ground
        Transform playerTransform = player.getComponent(Transform.class);
        playerTransform.position.set(100, 30); // Will overlap with ground at Y=0

        collisionSystem.register(playerCollider);
        collisionSystem.register(groundCollider);

        // Update bounds BEFORE collision resolution
        playerCollider.updateBounds();
        groundCollider.updateBounds();

        // Verify they are overlapping BEFORE update() resolves the collision
        assertTrue(playerCollider.overlaps(groundCollider),
                "Player and ground should be overlapping before collision resolution");

        // Now update to resolve collision
        collisionSystem.update();

        // After resolution, player should be pushed up
        assertTrue(playerTransform.position.y > 30,
                "Player should be moved up after collision resolution");
    }

    @Test
    @DisplayName("Should resolve collision by moving player up")
    void testCollisionResolution() {
        Transform playerTransform = player.getComponent(Transform.class);
        RigidBody playerRb = player.getComponent(RigidBody.class);

        // Place player falling onto ground
        playerTransform.position.set(100, 20);
        playerRb.velocity.y = -100; // Falling down

        collisionSystem.register(playerCollider);
        collisionSystem.register(groundCollider);

        float initialY = playerTransform.position.y;

        collisionSystem.update();

        // Player should be pushed up to resolve collision
        assertTrue(playerTransform.position.y >= initialY,
                "Player should be moved up to resolve ground collision");

        // Downward velocity should be stopped
        assertEquals(0, playerRb.velocity.y, 0.01f,
                "Downward velocity should be stopped on ground collision");
    }

    @Test
    @DisplayName("Should respect collision layers")
    void testCollisionLayers() {
        // Create an enemy that can collide with player
        GameObject enemy = new GameObject("Enemy");
        Transform enemyTransform = new Transform(100, 100);
        BoxCollider enemyCollider = new BoxCollider(32, 32, CollisionLayer.ENEMY);

        enemy.addComponent(enemyTransform);
        enemy.addComponent(enemyCollider);

        collisionSystem.register(playerCollider);
        collisionSystem.register(enemyCollider);

        // Verify layer collision rules
        assertTrue(CollisionLayer.PLAYER.canCollideWith(CollisionLayer.ENEMY));
        assertTrue(CollisionLayer.ENEMY.canCollideWith(CollisionLayer.PLAYER));
    }

    @Test
    @DisplayName("Should not collide with same object")
    void testNoSelfCollision() {
        collisionSystem.register(playerCollider);

        // Update should not cause issues with single collider
        assertDoesNotThrow(() -> collisionSystem.update());
    }

    @Test
    @DisplayName("Should handle trigger colliders without physics resolution")
    void testTriggerColliders() {
        Transform playerTransform = player.getComponent(Transform.class);
        playerTransform.position.set(100, 50);

        // Create trigger collider (coin pickup zone)
        GameObject trigger = new GameObject("Trigger");
        Transform triggerTransform = new Transform(100, 50);
        BoxCollider triggerCollider = new BoxCollider(40, 40, CollisionLayer.TRIGGER);
        triggerCollider.isTrigger = true;

        trigger.addComponent(triggerTransform);
        trigger.addComponent(triggerCollider);

        collisionSystem.register(playerCollider);
        collisionSystem.register(triggerCollider);

        float initialX = playerTransform.position.x;
        float initialY = playerTransform.position.y;

        collisionSystem.update();

        // Trigger should not move player
        assertEquals(initialX, playerTransform.position.x, 0.01f);
        assertEquals(initialY, playerTransform.position.y, 0.01f);
    }

    @Test
    @DisplayName("Should detect collision callbacks")
    void testCollisionCallbacks() {
        // Create a spy listener to track collision calls
        CollisionSpy spy = new CollisionSpy();

        // Create a new player GameObject with spy listener
        GameObject testPlayer = new GameObject("TestPlayer");
        Transform transform = new Transform(100, 30); // Overlapping with ground
        RigidBody rb = new RigidBody();
        BoxCollider collider = new BoxCollider(32, 48, CollisionLayer.PLAYER);

        testPlayer.addComponent(transform);
        testPlayer.addComponent(rb);
        testPlayer.addComponent(collider);
        testPlayer.addComponent(spy);

        collisionSystem.register(collider);
        collisionSystem.register(groundCollider);

        // Update collision system - should trigger collision
        collisionSystem.update();

        // Verify collision callback was called
        assertTrue(spy.wasOnCollisionCalled(), "onCollision should have been called");
        assertNotNull(spy.getLastCollision(), "Collision data should not be null");
    }

    @Test
    @DisplayName("Should detect collision enter events")
    void testCollisionEnterEvent() {
        // Create a spy listener to track collision enter
        CollisionSpy spy = new CollisionSpy();

        GameObject testPlayer = new GameObject("TestPlayer");
        Transform transform = new Transform(100, 200); // Start far from ground
        RigidBody rb = new RigidBody();
        BoxCollider collider = new BoxCollider(32, 48, CollisionLayer.PLAYER);

        testPlayer.addComponent(transform);
        testPlayer.addComponent(rb);
        testPlayer.addComponent(collider);
        testPlayer.addComponent(spy);

        collisionSystem.register(collider);
        collisionSystem.register(groundCollider);

        // First update - no collision
        collisionSystem.update();
        assertFalse(spy.wasOnCollisionEnterCalled(), "Should not trigger enter yet");

        // Move into collision range
        transform.position.y = 30;
        collisionSystem.update();

        // Should trigger enter event
        assertTrue(spy.wasOnCollisionEnterCalled(), "onCollisionEnter should have been called");
    }

    @Test
    @DisplayName("Should handle point cast correctly")
    void testPointCast() {
        collisionSystem.register(playerCollider);
        collisionSystem.register(groundCollider);
        collisionSystem.update();

        // Point inside ground collider
        BoxCollider hit = collisionSystem.pointCast(50, 16, CollisionLayer.GROUND);
        assertNotNull(hit, "Should detect ground at Y=16");
        assertEquals(groundCollider, hit);

        // Point outside any collider
        BoxCollider miss = collisionSystem.pointCast(50, 500, CollisionLayer.GROUND);
        assertNull(miss, "Should not detect anything at Y=500");
    }

    @Test
    @DisplayName("Should handle overlap box query")
    void testOverlapBox() {
        collisionSystem.register(playerCollider);
        collisionSystem.register(groundCollider);
        collisionSystem.update();

        var results = collisionSystem.overlapBox(
                100, 50,
                100, 100,
                CollisionLayer.PLAYER, CollisionLayer.GROUND
        );

        assertTrue(results.size() > 0, "Should find overlapping colliders");
    }

    @Test
    @DisplayName("Should stop velocity in collision direction only")
    void testVelocityResolution() {
        Transform playerTransform = player.getComponent(Transform.class);
        RigidBody playerRb = player.getComponent(RigidBody.class);

        // Player moving down and right
        playerTransform.position.set(100, 20);
        playerRb.velocity.set(50, -100);

        collisionSystem.register(playerCollider);
        collisionSystem.register(groundCollider);

        collisionSystem.update();

        // Y velocity should be stopped (hitting ground)
        assertEquals(0, playerRb.velocity.y, 0.01f);

        // X velocity should be preserved (or close to it)
        assertTrue(Math.abs(playerRb.velocity.x) > 40, "X velocity should be mostly preserved");
    }

    @Test
    @DisplayName("Should clear all colliders")
    void testClear() {
        collisionSystem.register(playerCollider);
        collisionSystem.register(groundCollider);

        assertEquals(2, collisionSystem.getAllColliders().size());

        collisionSystem.clear();

        assertEquals(0, collisionSystem.getAllColliders().size());
    }

    @Test
    @DisplayName("Should handle multiple collisions iteratively")
    void testMultipleCollisions() {
        // Create corner scenario
        Transform playerTransform = player.getComponent(Transform.class);
        playerTransform.position.set(100, 40);

        // Create ground and wall
        collisionSystem.register(playerCollider);
        collisionSystem.register(groundCollider);

        GameObject wall = new GameObject("Wall");
        Transform wallTransform = new Transform(150, 40);
        BoxCollider wallCollider = new BoxCollider(32, 100, CollisionLayer.GROUND);
        wallCollider.isStatic = true;

        wall.addComponent(wallTransform);
        wall.addComponent(wallCollider);
        collisionSystem.register(wallCollider);

        // Update should resolve both collisions without errors
        assertDoesNotThrow(() -> collisionSystem.update());
    }

    @Test
    @DisplayName("Should not process disabled colliders")
    void testDisabledColliders() {
        collisionSystem.register(playerCollider);
        collisionSystem.register(groundCollider);

        // Disable player collider
        playerCollider.setEnabled(false);

        Transform playerTransform = player.getComponent(Transform.class);
        float initialY = playerTransform.position.y;
        playerTransform.position.set(100, 20); // Should overlap with ground

        collisionSystem.update();

        // Player position should not be affected since collider is disabled
        assertTrue(playerTransform.position.y <= initialY + 5,
                "Disabled collider should not participate in collision resolution");
    }

    @Test
    @DisplayName("Should unregister colliders")
    void testUnregisterCollider() {
        collisionSystem.register(playerCollider);
        collisionSystem.register(groundCollider);

        assertEquals(2, collisionSystem.getAllColliders().size());

        collisionSystem.unregister(playerCollider);

        assertEquals(1, collisionSystem.getAllColliders().size());
        assertTrue(collisionSystem.getAllColliders().contains(groundCollider));
        assertFalse(collisionSystem.getAllColliders().contains(playerCollider));
    }

    @Test
    @DisplayName("Should update collider bounds")
    void testBoundsUpdate() {
        Transform playerTransform = player.getComponent(Transform.class);
        playerTransform.position.set(200, 300);

        collisionSystem.register(playerCollider);

        // Before update, bounds should be uninitialized (0)
        assertEquals(0, playerCollider.getMinX(), 0.01f);

        collisionSystem.update();

        // After update, bounds should reflect actual position
        assertEquals(200, playerCollider.getMinX(), 0.01f);
        assertEquals(232, playerCollider.getMaxX(), 0.01f);
    }

    // Spy component for testing collision callbacks (no mocking needed!)
    private static class CollisionSpy extends Component implements ICollisionListener {
        private final AtomicBoolean onCollisionCalled = new AtomicBoolean(false);
        private final AtomicBoolean onCollisionEnterCalled = new AtomicBoolean(false);
        private final AtomicBoolean onCollisionExitCalled = new AtomicBoolean(false);
        private Collision lastCollision;
        private GameObject lastOther;

        @Override
        public void onCollision(Collision collision) {
            onCollisionCalled.set(true);
            lastCollision = collision;
        }

        @Override
        public void onCollisionEnter(GameObject other) {
            onCollisionEnterCalled.set(true);
            lastOther = other;
        }

        @Override
        public void onCollisionExit(GameObject other) {
            onCollisionExitCalled.set(true);
            lastOther = other;
        }

        @Override
        public void update(double dt) {
            // No-op
        }

        public boolean wasOnCollisionCalled() {
            return onCollisionCalled.get();
        }

        public boolean wasOnCollisionEnterCalled() {
            return onCollisionEnterCalled.get();
        }

        public boolean wasOnCollisionExitCalled() {
            return onCollisionExitCalled.get();
        }

        public Collision getLastCollision() {
            return lastCollision;
        }

        public GameObject getLastOther() {
            return lastOther;
        }
    }
}