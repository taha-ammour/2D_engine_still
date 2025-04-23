package org.example.engine;

import org.example.engine.audio.AudioSystem;
import org.example.engine.core.GameObject;
import org.example.engine.input.InputSystem;
import org.example.engine.physics.PhysicsSystem;
import org.example.engine.rendering.Camera;
import org.example.engine.rendering.RenderSystem;
import org.example.engine.scene.SceneManager;
import org.example.engine.ui.UISystem;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

/**
 * Main engine class that ties all systems together.
 * Handles initialization, the main game loop, and cleanup.
 */
public class Engine {
    // Window handle
    private long window;

    // Window properties
    private int windowWidth = 800;
    private int windowHeight = 600;
    private String windowTitle = "Game Engine";

    // Systems
    private RenderSystem renderSystem;
    private PhysicsSystem physicsSystem;
    private InputSystem inputSystem;
    private AudioSystem audioSystem;
    private SceneManager sceneManager;
    private UISystem uiSystem;

    // Game loop properties
    private boolean running = false;
    private float timeScale = 1.0f;
    private float fixedTimeStep = 1.0f / 60.0f;
    private float maxDeltaTime = 0.25f;

    // Performance stats
    private float fps = 0;
    private int frameCount = 0;
    private float fpsTimer = 0;

    /**
     * Initialize the engine with default window size
     */
    public void init() {
        init(windowWidth, windowHeight, windowTitle);
    }

    /**
     * Initialize the engine with specified window size
     */
    public void init(int width, int height, String title) {
        this.windowWidth = width;
        this.windowHeight = height;
        this.windowTitle = title;

        initGLFW();
        initSystems();
    }

    /**
     * Initialize GLFW and create window
     */
    private void initGLFW() {
        // Set up error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        // Create the window
        window = GLFW.glfwCreateWindow(windowWidth, windowHeight, windowTitle, MemoryUtil.NULL, MemoryUtil.NULL);

        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Set up resize callback
        GLFW.glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                onResize(width, height);
            }
        });

        // Center the window
        GLFW.glfwSetWindowPos(
                window,
                (GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()).width() - windowWidth) / 2,
                (GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()).height() - windowHeight) / 2
        );

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(window);

        // Enable v-sync
        GLFW.glfwSwapInterval(1);

        // Show the window
        GLFW.glfwShowWindow(window);

        // Initialize OpenGL
        GL.createCapabilities();

        // Configure OpenGL
        GL11.glViewport(0, 0, windowWidth, windowHeight);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Initialize all engine systems
     */
    private void initSystems() {
        // Initialize render system
        renderSystem = RenderSystem.getInstance();

        // Initialize physics system
        physicsSystem = PhysicsSystem.getInstance();

        // Initialize input system
        inputSystem = InputSystem.getInstance();
        inputSystem.init(window);

        // Initialize audio system
        audioSystem = AudioSystem.getInstance();
        audioSystem.init();

        // Initialize scene manager
        sceneManager = SceneManager.getInstance();
        sceneManager.init(renderSystem, physicsSystem);

        // Initialize UI system
        uiSystem = UISystem.getInstance();
        uiSystem.init(renderSystem);
    }

    /**
     * Handle window resize
     */
    private void onResize(int width, int height) {
        // Update viewport
        windowWidth = width;
        windowHeight = height;

        GL11.glViewport(0, 0, width, height);

        // Update camera
        if (sceneManager.getActiveScene() != null &&
                sceneManager.getActiveScene().getMainCamera() != null) {
            Camera camera = sceneManager.getActiveScene().getMainCamera();

            // Update camera viewport size
            camera.setViewportSize(width, height);

            // Make sure aspect ratio preservation is enabled (default is already true in the updated Camera class)
            camera.setMaintainAspectRatio(true);
        }

    }

    /**
     * Start the main game loop
     */
    public void run() {
        running = true;

        // Timing variables
        double lastTime = GLFW.glfwGetTime();
        double accumulator = 0.0;

        // Main game loop
        while (running && !GLFW.glfwWindowShouldClose(window)) {
            // Calculate delta time
            double currentTime = GLFW.glfwGetTime();
            double deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            // Cap delta time to prevent spiral of death
            if (deltaTime > maxDeltaTime) {
                deltaTime = maxDeltaTime;
            }

            // Scale time
            deltaTime *= timeScale;

            // Update FPS counter
            updateFPS((float)deltaTime);

            // Process input
            GLFW.glfwPollEvents();
            inputSystem.update();

            // Update UI
            uiSystem.update((float)deltaTime);

            // Fixed update for physics
            accumulator += deltaTime;
            while (accumulator >= fixedTimeStep) {
                physicsSystem.update(fixedTimeStep);
                accumulator -= fixedTimeStep;
            }

            // Update scene
            sceneManager.update((float)deltaTime);

            // Update audio
            audioSystem.update();

            // Render scene
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            sceneManager.render();
            uiSystem.render();

            // Swap buffers
            GLFW.glfwSwapBuffers(window);

            // Check if window should close
            if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
                GLFW.glfwSetWindowShouldClose(window, true);
            }
        }

        // Clean up
        cleanup();
    }

    /**
     * Update FPS counter
     */
    private void updateFPS(float deltaTime) {
        frameCount++;
        fpsTimer += deltaTime;

        if (fpsTimer >= 1.0f) {
            fps = frameCount / fpsTimer;
            frameCount = 0;
            fpsTimer = 0;

            // Update window title with FPS
            GLFW.glfwSetWindowTitle(window, windowTitle + " - FPS: " + Math.round(fps));
        }
    }

    /**
     * Stop the game loop
     */
    public void stop() {
        running = false;
    }

    /**
     * Clean up resources
     */
    private void cleanup() {
        // Clean up systems
        audioSystem.cleanup();
        sceneManager.cleanup();

        // Clean up GLFW
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    /**
     * Get the GLFW window handle
     */
    public long getWindow() {
        return window;
    }

    /**
     * Get the window width
     */
    public int getWindowWidth() {
        return windowWidth;
    }

    /**
     * Get the window height
     */
    public int getWindowHeight() {
        return windowHeight;
    }

    /**
     * Set the time scale (slow motion or fast forward)
     */
    public void setTimeScale(float timeScale) {
        this.timeScale = Math.max(0.1f, timeScale);
    }

    /**
     * Get the current time scale
     */
    public float getTimeScale() {
        return timeScale;
    }

    /**
     * Get the current FPS
     */
    public float getFPS() {
        return fps;
    }

    /**
     * Create a new GameObject and add it to the active scene
     */
    public GameObject createGameObject(String name) {
        GameObject gameObject = new GameObject(name);

        if (sceneManager.getActiveScene() != null) {
            sceneManager.getActiveScene().addGameObject(gameObject);
        }

        return gameObject;
    }

    /**
     * Set the background clear color
     */
    public void setClearColor(float r, float g, float b) {
        GL11.glClearColor(r, g, b, 1.0f);
    }

    /**
     * Get the scene manager
     */
    public SceneManager getSceneManager() {
        return sceneManager;
    }
}