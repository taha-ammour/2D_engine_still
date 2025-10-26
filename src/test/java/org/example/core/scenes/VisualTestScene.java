// src/main/java/org/example/core/scenes/VisualTestScene.java
package org.example.core.scenes;

import org.example.ecs.GameObject;
import org.example.ecs.Component;
import org.example.ecs.components.SpriteRenderer;
import org.example.ecs.components.Transform;
import org.example.ecs.components.RigidBody;
import org.example.engine.input.Input;
import org.example.engine.input.InputManager;
import org.example.engine.input.Mouse;
import org.example.gfx.Camera2D;
import org.example.gfx.Material;
import org.example.gfx.Renderer2D;
import org.example.physics.*;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Visual Test Runner - Watch collision tests in action!
 * Press number keys (1-9) to run different test scenarios
 */
public final class VisualTestScene extends BaseScene {
    private final List<GameObject> gameObjects = new ArrayList<>();
    private final CollisionSystem collisionSystem = new CollisionSystem();
    private CollisionDebugRenderer debugRenderer;
    private InputManager inputManager;

    private TestCase currentTest;
    private float testTimer = 0f;
    private String testStatus = "Press 1-9 to run a test";
    private List<String> testLog = new ArrayList<>();

    public VisualTestScene(Input input, Mouse mouse, Renderer2D renderer, Camera2D camera) {
        super(input, mouse, renderer, camera);
    }

    @Override
    protected void onLoad() {
        debugRenderer = new CollisionDebugRenderer(renderer);
        debugRenderer.setEnabled(true); // Always show collision boxes

        inputManager = new InputManager(input,mouse);

        // Bind test cases to number keys
        inputManager.bindKeyPress(GLFW_KEY_1, dt -> runTest(new Test1_RegisterColliders()));
        inputManager.bindKeyPress(GLFW_KEY_2, dt -> runTest(new Test2_CollisionDetection()));
        inputManager.bindKeyPress(GLFW_KEY_3, dt -> runTest(new Test3_CollisionResolution()));
        inputManager.bindKeyPress(GLFW_KEY_4, dt -> runTest(new Test4_CollisionLayers()));
        inputManager.bindKeyPress(GLFW_KEY_5, dt -> runTest(new Test5_TriggerColliders()));
        inputManager.bindKeyPress(GLFW_KEY_6, dt -> runTest(new Test6_VelocityResolution()));
        inputManager.bindKeyPress(GLFW_KEY_7, dt -> runTest(new Test7_MultipleCollisions()));
        inputManager.bindKeyPress(GLFW_KEY_8, dt -> runTest(new Test8_PointCast()));
        inputManager.bindKeyPress(GLFW_KEY_9, dt -> runTest(new Test9_OverlapBox()));

        inputManager.bindKeyPress(GLFW_KEY_R, dt -> resetTest());
        inputManager.bindKeyPress(GLFW_KEY_ESCAPE, dt -> System.exit(0));

        camera.setPosition(0, 0);

        log("Visual Test Runner Started!");
        log("Controls:");
        log("  1-9: Run test cases");
        log("  R: Reset current test");
        log("  ESC: Exit");
        log("");
    }

    private void runTest(TestCase test) {
        resetTest();
        currentTest = test;
        testStatus = "Running: " + test.getName();
        log("=== " + test.getName() + " ===");
        test.setup(this);
    }

    private void resetTest() {
        // Clean up old objects
        for (GameObject obj : gameObjects) {
            BoxCollider collider = obj.getComponent(BoxCollider.class);
            if (collider != null) {
                collisionSystem.unregister(collider);
            }
        }
        gameObjects.clear();
        collisionSystem.clear();
        testTimer = 0f;
        currentTest = null;
        testStatus = "Test reset. Press 1-9 to run a test";
    }

    @Override
    public void handleInput() {
        input.update();
        inputManager.processInput(0);
    }

    @Override
    public void update(double dt) {
        testTimer += (float) dt;

        // Update current test
        if (currentTest != null) {
            currentTest.update((float) dt);
        }

        // Update game objects
        for (GameObject obj : gameObjects) {
            obj.update(dt);
        }

        // Update collision system
        collisionSystem.update();
    }

    @Override
    public void render() {
        // Render game objects
        for (GameObject obj : gameObjects) {
            obj.render();
        }

        // Render collision debug boxes
        debugRenderer.render(collisionSystem.getAllColliders());

        // Render UI text (test status and log)
        renderUI();
    }

    private void renderUI() {
        // Status text at top
        drawText(testStatus, 10, 690, new Vector4f(1, 1, 1, 1));

        // Test log at bottom
        int y = 20;
        for (int i = Math.max(0, testLog.size() - 5); i < testLog.size(); i++) {
            drawText(testLog.get(i), 10, y, new Vector4f(0.8f, 0.8f, 0.8f, 1));
            y += 20;
        }
    }

    private void drawText(String text, float x, float y, Vector4f color) {
        // Simple text rendering using colored boxes for now
        // (You could integrate a proper font rendering system later)
        Material mat = Material.builder().tint(color).build();
        float charWidth = 8;
        for (int i = 0; i < Math.min(text.length(), 100); i++) {
            renderer.drawQuad(
                    x + i * charWidth, y,
                    charWidth - 1, 12,
                    0f, mat,
                    new float[]{1, 1, 0, 0},
                    1f, 1f
            );
        }
    }

    @Override
    protected void onUnload() {
        resetTest();
    }

    public void log(String message) {
        testLog.add(message);
        System.out.println(message);
        if (testLog.size() > 50) {
            testLog.remove(0);
        }
    }

    public GameObject createBox(String name, float x, float y, float w, float h,
                                CollisionLayer layer, Vector4f color, boolean isStatic) {
        GameObject obj = new GameObject(name);

        Transform transform = new Transform(x, y);
        obj.addComponent(transform);

        if (!isStatic) {
            RigidBody rb = new RigidBody();
            obj.addComponent(rb);
        }

        BoxCollider collider = new BoxCollider(w, h, layer);
        collider.isStatic = isStatic;
        obj.addComponent(collider);
        collisionSystem.register(collider);

        SpriteRenderer sprite = new SpriteRenderer(renderer);
        sprite.width = w;
        sprite.height = h;
        sprite.setTint(color.x, color.y, color.z, color.w);
        obj.addComponent(sprite);

        gameObjects.add(obj);
        return obj;
    }

    public CollisionSystem getCollisionSystem() {
        return collisionSystem;
    }

    public float getTestTimer() {
        return testTimer;
    }

    // ===== TEST CASE INTERFACE =====

    private interface TestCase {
        String getName();
        void setup(VisualTestScene scene);
        void update(float dt);
    }

    // ===== TEST 1: Register Colliders =====
    private class Test1_RegisterColliders implements TestCase {
        @Override
        public String getName() {
            return "Test 1: Register Colliders";
        }

        @Override
        public void setup(VisualTestScene scene) {
            createBox("Player", 200, 200, 32, 48, CollisionLayer.PLAYER,
                    new Vector4f(0, 1, 0, 1), false);
            createBox("Ground", 100, 100, 200, 32, CollisionLayer.GROUND,
                    new Vector4f(0.5f, 0.5f, 0.5f, 1), true);

            log("Created 2 colliders");
            log("Expected: 2 colliders in system");
            log("Actual: " + collisionSystem.getAllColliders().size());
            log(collisionSystem.getAllColliders().size() == 2 ? "✅ PASS" : "❌ FAIL");
        }

        @Override
        public void update(float dt) {
        }
    }

    // ===== TEST 2: Collision Detection =====
    private class Test2_CollisionDetection implements TestCase {
        private GameObject player;
        private boolean detected = false;

        @Override
        public String getName() {
            return "Test 2: Collision Detection";
        }

        @Override
        public void setup(VisualTestScene scene) {
            player = createBox("Player", 200, 150, 32, 48, CollisionLayer.PLAYER,
                    new Vector4f(0, 1, 0, 1), false);
            createBox("Ground", 100, 100, 200, 32, CollisionLayer.GROUND,
                    new Vector4f(0.5f, 0.5f, 0.5f, 1), true);

            // Add collision listener
            player.addComponent(new CollisionDetector());

            log("Player falling onto ground...");
            log("Watch for collision detection!");
        }

        @Override
        public void update(float dt) {
            if (!detected && player.getComponent(CollisionDetector.class).wasCollision) {
                detected = true;
                log("✅ Collision detected!");
                log("Player velocity stopped: " + player.getComponent(RigidBody.class).velocity.y);
            }
        }
    }

    // ===== TEST 3: Collision Resolution =====
    private class Test3_CollisionResolution implements TestCase {
        private GameObject player;
        private float initialY;
        private boolean resolved = false;

        @Override
        public String getName() {
            return "Test 3: Collision Resolution";
        }

        @Override
        public void setup(VisualTestScene scene) {
            player = createBox("Player", 200, 120, 32, 48, CollisionLayer.PLAYER,
                    new Vector4f(0, 1, 0, 1), false);
            createBox("Ground", 100, 100, 200, 32, CollisionLayer.GROUND,
                    new Vector4f(0.5f, 0.5f, 0.5f, 1), true);

            initialY = player.getComponent(Transform.class).position.y;
            RigidBody rb = player.getComponent(RigidBody.class);
            rb.velocity.y = -200; // Fall fast

            log("Player falling with velocity: " + rb.velocity.y);
            log("Initial Y: " + initialY);
        }

        @Override
        public void update(float dt) {
            if (!resolved) {
                Transform transform = player.getComponent(Transform.class);
                RigidBody rb = player.getComponent(RigidBody.class);

                if (rb.velocity.y == 0 && transform.position.y >= initialY) {
                    resolved = true;
                    log("✅ Collision resolved!");
                    log("Final Y: " + transform.position.y);
                    log("Velocity stopped: " + rb.velocity.y);
                }
            }
        }
    }

    // ===== TEST 4: Collision Layers =====
    private class Test4_CollisionLayers implements TestCase {
        @Override
        public String getName() {
            return "Test 4: Collision Layers";
        }

        @Override
        public void setup(VisualTestScene scene) {
            createBox("Player", 200, 200, 32, 48, CollisionLayer.PLAYER,
                    new Vector4f(0, 1, 0, 1), false);
            createBox("Enemy", 250, 200, 32, 32, CollisionLayer.ENEMY,
                    new Vector4f(1, 0, 0, 1), false);
            createBox("Ground", 100, 100, 300, 32, CollisionLayer.GROUND,
                    new Vector4f(0.5f, 0.5f, 0.5f, 1), true);

            log("Testing layer collision rules:");
            log("Player vs Enemy: " + CollisionLayer.PLAYER.canCollideWith(CollisionLayer.ENEMY));
            log("Player vs Ground: " + CollisionLayer.PLAYER.canCollideWith(CollisionLayer.GROUND));
            log("Enemy vs Ground: " + CollisionLayer.ENEMY.canCollideWith(CollisionLayer.GROUND));
        }

        @Override
        public void update(float dt) {
        }
    }

    // ===== TEST 5: Trigger Colliders =====
    private class Test5_TriggerColliders implements TestCase {
        private GameObject player;

        @Override
        public String getName() {
            return "Test 5: Trigger Colliders (No Physics)";
        }

        @Override
        public void setup(VisualTestScene scene) {
            player = createBox("Player", 200, 200, 32, 48, CollisionLayer.PLAYER,
                    new Vector4f(0, 1, 0, 1), false);

            GameObject trigger = createBox("Trigger", 200, 150, 64, 64, CollisionLayer.TRIGGER,
                    new Vector4f(1, 1, 0, 0.3f), true);
            trigger.getComponent(BoxCollider.class).isTrigger = true;

            createBox("Ground", 100, 100, 300, 32, CollisionLayer.GROUND,
                    new Vector4f(0.5f, 0.5f, 0.5f, 1), true);

            log("Player will pass through yellow trigger");
            log("Trigger should not affect physics");
        }

        @Override
        public void update(float dt) {
        }
    }

    // ===== TEST 6: Velocity Resolution =====
    private class Test6_VelocityResolution implements TestCase {
        private GameObject player;
        private boolean checked = false;

        @Override
        public String getName() {
            return "Test 6: Velocity Resolution (Directional)";
        }

        @Override
        public void setup(VisualTestScene scene) {
            player = createBox("Player", 200, 120, 32, 48, CollisionLayer.PLAYER,
                    new Vector4f(0, 1, 0, 1), false);
            createBox("Ground", 100, 100, 300, 32, CollisionLayer.GROUND,
                    new Vector4f(0.5f, 0.5f, 0.5f, 1), true);

            RigidBody rb = player.getComponent(RigidBody.class);
            rb.velocity.set(100, -200); // Moving right and down

            log("Player velocity: X=" + rb.velocity.x + ", Y=" + rb.velocity.y);
            log("Expecting: Y stops, X continues");
        }

        @Override
        public void update(float dt) {
            if (!checked && testTimer > 0.5f) {
                checked = true;
                RigidBody rb = player.getComponent(RigidBody.class);
                log("After collision:");
                log("  X velocity: " + rb.velocity.x + " (should be preserved)");
                log("  Y velocity: " + rb.velocity.y + " (should be 0)");
                log(Math.abs(rb.velocity.y) < 0.1f ? "✅ PASS" : "❌ FAIL");
            }
        }
    }

    // ===== TEST 7: Multiple Collisions =====
    private class Test7_MultipleCollisions implements TestCase {
        @Override
        public String getName() {
            return "Test 7: Multiple Collisions (Corner)";
        }

        @Override
        public void setup(VisualTestScene scene) {
            createBox("Player", 250, 150, 32, 48, CollisionLayer.PLAYER,
                    new Vector4f(0, 1, 0, 1), false);

            // Create corner
            createBox("Ground", 100, 100, 200, 32, CollisionLayer.GROUND,
                    new Vector4f(0.5f, 0.5f, 0.5f, 1), true);
            createBox("Wall", 280, 132, 32, 100, CollisionLayer.GROUND,
                    new Vector4f(0.6f, 0.6f, 0.6f, 1), true);

            log("Player in corner - multiple collisions");
            log("Should resolve both ground and wall");
        }

        @Override
        public void update(float dt) {
        }
    }

    // ===== TEST 8: Point Cast =====
    private class Test8_PointCast implements TestCase {
        private GameObject indicator;
        private float pointX = 200;
        private float pointY = 116;

        @Override
        public String getName() {
            return "Test 8: Point Cast Query";
        }

        @Override
        public void setup(VisualTestScene scene) {
            createBox("Ground", 100, 100, 200, 32, CollisionLayer.GROUND,
                    new Vector4f(0.5f, 0.5f, 0.5f, 1), true);

            // Visual indicator for point
            indicator = createBox("Point", pointX - 2, pointY - 2, 4, 4, CollisionLayer.DEFAULT,
                    new Vector4f(1, 0, 1, 1), true);

            BoxCollider hit = collisionSystem.pointCast(pointX, pointY, CollisionLayer.GROUND);
            log("Point cast at (" + pointX + ", " + pointY + ")");
            log("Result: " + (hit != null ? "HIT - " + hit.getOwner().getName() : "MISS"));
            log(hit != null ? "✅ PASS" : "❌ FAIL");
        }

        @Override
        public void update(float dt) {
        }
    }

    // ===== TEST 9: Overlap Box =====
    private class Test9_OverlapBox implements TestCase {
        private GameObject queryBox;

        @Override
        public String getName() {
            return "Test 9: Overlap Box Query";
        }

        @Override
        public void setup(VisualTestScene scene) {
            createBox("Player", 200, 200, 32, 48, CollisionLayer.PLAYER,
                    new Vector4f(0, 1, 0, 1), false);
            createBox("Ground", 150, 150, 200, 32, CollisionLayer.GROUND,
                    new Vector4f(0.5f, 0.5f, 0.5f, 1), true);

            // Visual query box
            queryBox = createBox("Query", 150, 150, 100, 100, CollisionLayer.DEFAULT,
                    new Vector4f(1, 1, 0, 0.2f), true);

            var results = collisionSystem.overlapBox(200, 200, 100, 100,
                    CollisionLayer.PLAYER, CollisionLayer.GROUND);

            log("Overlap box query at (200, 200) size 100x100");
            log("Found " + results.size() + " colliders");
            for (BoxCollider col : results) {
                log("  - " + col.getOwner().getName());
            }
            log(results.size() > 0 ? "✅ PASS" : "❌ FAIL");
        }

        @Override
        public void update(float dt) {
        }
    }

    // Helper component for collision detection
    private static class CollisionDetector extends Component implements ICollisionListener {
        public boolean wasCollision = false;

        @Override
        public void onCollision(Collision collision) {
            wasCollision = true;
        }

        @Override
        public void update(double dt) {
        }
    }
}