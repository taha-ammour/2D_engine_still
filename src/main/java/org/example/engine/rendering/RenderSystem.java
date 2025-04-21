package org.example.engine.rendering;

import org.example.engine.core.GameObject;
import org.example.engine.core.Transform;
import org.example.engine.core.ZComparator;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.example.engine.core.Component;
import org.example.engine.rendering.ShaderManager.Shader;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

/**
 * Central rendering system that manages all rendering operations with proper Z-ordering.
 * This system handles sorting, batching, and drawing all renderable objects.
 */
public class RenderSystem {
    private static RenderSystem instance;

    private final ShaderManager shaderManager;
    private Camera camera;

    // Render queues for different layers (grouped by z-order)
    private final Map<Integer, List<Renderable>> renderLayers = new HashMap<>();
    private final List<Renderable> transparentObjects = new ArrayList<>();

    // Render statistics for performance monitoring
    private int drawCalls = 0;
    private int objectsRendered = 0;
    private int objectsCulled = 0;

    // Cached matrices to avoid creating new objects each frame
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f viewProjectionMatrix = new Matrix4f();
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    // Global lighting configuration
    private final List<Light> lights = new ArrayList<>();
    private final Vector3f ambientLight = new Vector3f(0.1f, 0.1f, 0.1f);

    // Configuration
    private boolean frustumCullingEnabled = true;
    private boolean wireframeMode = false;
    private boolean debugMode = false;

    /**
     * Get the singleton instance
     */
    public static RenderSystem getInstance() {
        if (instance == null) {
            ShaderManager shaderManager = ShaderManager.getInstance();
            // Temporary camera to be replaced later with the actual camera
            Camera camera = new Camera();
            instance = new RenderSystem(shaderManager, camera);
        }
        return instance;
    }

    public RenderSystem(ShaderManager shaderManager, Camera camera) {
        this.shaderManager = shaderManager;
        this.camera = camera;

        // Initialize standard shaders - use try-catch to handle missing files gracefully
        try {
            this.shaderManager.loadShader("sprite", "/shaders/sprite.vs.glsl", "/shaders/sprite.fs.glsl");
        } catch (Exception e) {
            System.err.println("Warning: Could not load sprite shader, using default shader");
            // Create a basic default shader if needed
        }

        try {
            this.shaderManager.loadShader("text", "/shaders/text.vs.glsl", "/shaders/text.fs.glsl");
        } catch (Exception e) {
            System.err.println("Warning: Could not load text shader, using default shader");
        }

        try {
            this.shaderManager.loadShader("particle", "/shaders/particle.vs.glsl", "/shaders/particle.fs.glsl");
        } catch (Exception e) {
            System.err.println("Warning: Could not load particle shader, using default shader");
        }

        // Set up OpenGL state
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Set the camera for rendering
     */
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    /**
     * Updates the camera matrices and prepares for rendering
     */
    public void updateCamera() {
        // Get updated matrices from camera
        camera.getViewMatrix(viewMatrix);
        camera.getProjectionMatrix(projectionMatrix);

        // Compute the combined view-projection matrix
        viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix);
    }

    /**
     * Submit a renderable object to be drawn this frame
     * @param renderable The object to render
     */
    public void submit(Renderable renderable) {
        if (renderable == null) return;

        // Skip if frustum culling is enabled and the object is outside view
        if (frustumCullingEnabled && !isInViewFrustum(renderable)) {
            objectsCulled++;
            return;
        }

        // Handle transparent objects separately for back-to-front rendering
        if (renderable.isTransparent()) {
            transparentObjects.add(renderable);
            return;
        }

        // Get the render layer based on z-order (using floor to group similar depths)
        int layer = (int) Math.floor(renderable.getZ() * 100);

        // Add to the appropriate render layer
        renderLayers.computeIfAbsent(layer, k -> new ArrayList<>()).add(renderable);
    }

    /**
     * Adds multiple renderables from a GameObject and its components
     * @param gameObject The game object containing renderables
     */
    public void submitGameObject(GameObject gameObject) {
        if (gameObject == null || !gameObject.isActive()) return;

        // Add any renderables directly attached to the game object
        if (gameObject instanceof Renderable) {
            submit((Renderable) gameObject);
        }

        // Add renderables from all components
        for (Component component : gameObject.getComponents()) {
            if (component instanceof Renderable) {
                submit((Renderable) component);
            }
        }

        // Recursively process children
        for (GameObject child : gameObject.getChildren()) {
            submitGameObject(child);
        }
    }

    /**
     * Process all submitted objects and render them with proper Z-ordering
     */
    public void render() {
        // Reset statistics for this frame
        drawCalls = 0;
        objectsRendered = 0;

        // Clear buffers
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // Set wireframe mode if enabled
        if (wireframeMode) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

        // Process opaque objects layer by layer (front to back for better Z-buffer optimization)
        List<Integer> sortedLayers = new ArrayList<>(renderLayers.keySet());
        sortedLayers.sort(Integer::compareTo); // Sort layers by depth

        // Render opaque objects front-to-back for depth buffer optimization
        for (Integer layer : sortedLayers) {
            List<Renderable> layerObjects = renderLayers.get(layer);

            // Sort within layer for deterministic rendering
            layerObjects.sort(new ZComparator());

            // Batch and render objects in this layer
            batchAndRender(layerObjects);
        }

        // Enable alpha blending for transparent objects
        glEnable(GL_BLEND);

        // Sort transparent objects back-to-front for proper blending
        transparentObjects.sort((a, b) -> Float.compare(b.getZ(), a.getZ()));

        // Render transparent objects
        for (Renderable renderable : transparentObjects) {
            renderable.render(this, viewProjectionMatrix);
            drawCalls++;
            objectsRendered++;
        }

        // Clear render queues
        renderLayers.clear();
        transparentObjects.clear();

        // Render debug information if enabled
        if (debugMode) {
            renderDebugInfo();
        }
    }

    /**
     * Optimized batching for similar objects at the same depth
     */
    private void batchAndRender(List<Renderable> renderables) {
        // Group by material/shader
        Map<Material, List<Renderable>> materialGroups = new HashMap<>();

        for (Renderable renderable : renderables) {
            Material material = renderable.getMaterial();
            materialGroups.computeIfAbsent(material, k -> new ArrayList<>()).add(renderable);
        }

        // Render each material group as a batch where possible
        for (Map.Entry<Material, List<Renderable>> entry : materialGroups.entrySet()) {
            Material material = entry.getKey();
            List<Renderable> batch = entry.getValue();

            // Bind the material only once for the whole batch
            material.bind();

            // Upload light information once per batch
            uploadLightingData(material.getShader());

            // Render each object
            for (Renderable renderable : batch) {
                renderable.render(this, viewProjectionMatrix);
                objectsRendered++;
            }

            drawCalls++; // Each batch is one draw call

            // Unbind the material
            material.unbind();
        }
    }

    /**
     * Upload lighting data to a shader
     */
    private void uploadLightingData(ShaderManager.Shader shader) {
        if (shader == null) return;

        // Set ambient light
        shader.setUniform3f("u_AmbientColor", ambientLight.x, ambientLight.y, ambientLight.z);

        // Set light count
        shader.setUniform1i("lightCount", Math.min(lights.size(), 10));

        // Set each light's properties
        for (int i = 0; i < Math.min(lights.size(), 10); i++) {
            Light light = lights.get(i);
            String prefix = "lights[" + i + "].";

            shader.setUniform3f(prefix + "position", light.position.x, light.position.y, light.position.z);
            shader.setUniform3f(prefix + "color", light.color.x, light.color.y, light.color.z);
            shader.setUniform3f(prefix + "direction", light.direction.x, light.direction.y, light.direction.z);
            shader.setUniform1f(prefix + "intensity", light.intensity);
            shader.setUniform1f(prefix + "constant", light.constant);
            shader.setUniform1f(prefix + "linear", light.linear);
            shader.setUniform1f(prefix + "quadratic", light.quadratic);
            shader.setUniform1f(prefix + "cutoff", light.cutoff);
            shader.setUniform1f(prefix + "outerCutoff", light.outerCutoff);
            shader.setUniform1i(prefix + "type", light.type);
        }
    }

    /**
     * Check if an object is within the camera's view frustum
     */
    private boolean isInViewFrustum(Renderable renderable) {
        // Simple circle-based culling for 2D games
        float x = renderable.getTransform().getPosition().x;
        float y = renderable.getTransform().getPosition().y;
        float radius = Math.max(renderable.getWidth(), renderable.getHeight()) * 0.5f;

        return camera.isInView(x, y, radius);
    }

    /**
     * Render debug visualization information
     */
    private void renderDebugInfo() {
        // Render bounding boxes, colliders, etc.
        // This would be implemented with a debug renderer
    }

    /**
     * Add a light to the scene
     */
    public void addLight(Light light) {
        if (light == null) return;

        lights.add(light);
    }

    /**
     * Clear all lights from the scene
     */
    public void clearLights() {
        lights.clear();
    }

    /**
     * Set the ambient light color
     */
    public void setAmbientLight(float r, float g, float b) {
        ambientLight.set(r, g, b);
    }

    // Getters and setters

    public ShaderManager getShaderManager() {
        return shaderManager;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setFrustumCullingEnabled(boolean enabled) {
        this.frustumCullingEnabled = enabled;
    }

    public void setWireframeMode(boolean wireframeMode) {
        this.wireframeMode = wireframeMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean getDebugMode() {
        return debugMode;
    }

    public int getDrawCalls() {
        return drawCalls;
    }

    public int getObjectsRendered() {
        return objectsRendered;
    }

    public int getObjectsCulled() {
        return objectsCulled;
    }
}