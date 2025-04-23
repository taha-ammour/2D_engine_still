package org.example.engine.rendering;

import org.example.engine.core.GameObject;
import org.example.engine.core.Transform;
import org.example.engine.core.ZComparator;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.example.engine.core.Component;
import org.example.engine.rendering.ShaderManager.Shader;
import org.lwjgl.opengl.GL11;

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

    // Added for debugging
    private boolean verboseLogging = false;

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

        if (verboseLogging) {
            System.out.println("RenderSystem initialized with ShaderManager: " + shaderManager);
            System.out.println("Initial camera: " + camera);
        }

        // Initialize standard shaders - use try-catch to handle missing files gracefully
        try {
            this.shaderManager.loadShader("sprite", "/shaders/sprite.vs.glsl", "/shaders/sprite.fs.glsl");
            if (verboseLogging) {
                System.out.println("Sprite shader loaded successfully");
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load sprite shader, using default shader");
            e.printStackTrace();
            // Create a basic default shader
            createDefaultShader();
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

        // DEBUG: Print OpenGL capabilities
        if (verboseLogging) {
            System.out.println("OpenGL Depth Test: " + glIsEnabled(GL_DEPTH_TEST));
            System.out.println("OpenGL Blend: " + glIsEnabled(GL_BLEND));
        }
    }

    /**
     * Create a simple default shader if the main shader fails to load
     */
    private void createDefaultShader() {
        try {
            String vertexSource =
                    "#version 330 core\n" +
                            "layout (location = 0) in vec3 aPos;\n" +
                            "layout (location = 1) in vec2 aTexCoord;\n" +
                            "uniform mat4 u_MVP;\n" +
                            "out vec2 TexCoord;\n" +
                            "void main() {\n" +
                            "    gl_Position = u_MVP * vec4(aPos, 1.0);\n" +
                            "    TexCoord = aTexCoord;\n" +
                            "}\n";

            String fragmentSource =
                    "#version 330 core\n" +
                            "in vec2 TexCoord;\n" +
                            "out vec4 FragColor;\n" +
                            "uniform vec4 u_Color;\n" +
                            "uniform sampler2D u_Texture;\n" +
                            "void main() {\n" +
                            "    vec4 texColor = texture(u_Texture, TexCoord);\n" +
                            "    if (texColor.a < 0.01) {\n" +
                            "        FragColor = u_Color;\n" +
                            "    } else {\n" +
                            "        FragColor = texColor * u_Color;\n" +
                            "    }\n" +
                            "}\n";

            shaderManager.createDefaultShader("sprite", vertexSource, fragmentSource);
            System.out.println("Created default sprite shader");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to create default shader: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Set the camera for rendering
     */
    public void setCamera(Camera camera) {
        if (verboseLogging) {
            System.out.println("Setting camera: " + camera);
            if (camera != null) {
                System.out.println("Camera position: " + camera.getPosition());
            }
        }
        this.camera = camera;
    }


    private void configureViewport() {
        if (camera == null) {
            // No camera to configure viewport with
            return;
        }

        try {
            // Get viewport dimensions from camera
            float viewportWidth = camera.getViewportWidth();
            float viewportHeight = camera.getViewportHeight();

            // Check if aspect ratio maintenance is enabled
            if (camera.getMaintainAspectRatio()) {
                // Use virtual viewport dimensions
                int x = (int)camera.getVirtualViewportX();
                int y = (int)camera.getVirtualViewportY();
                int width = (int)camera.getVirtualViewportWidth();
                int height = (int)camera.getVirtualViewportHeight();

                // Set OpenGL viewport to match virtual viewport
                GL11.glViewport(x, y, width, height);

                // Clear the letterbox/pillarbox areas
                if (x > 0 || y > 0) {
                    // Save current clear color
                    float[] currentColor = new float[4];
                    GL11.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE, currentColor);

                    // Set clear color to black for letterbox/pillarbox
                    GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

                    // Clear the areas outside the viewport
                    if (x > 0) { // Pillarboxing (vertical bars)
                        GL11.glViewport(0, 0, x, (int)viewportHeight);
                        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                        GL11.glViewport(x + width, 0, x, (int)viewportHeight);
                        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                    }

                    if (y > 0) { // Letterboxing (horizontal bars)
                        GL11.glViewport(0, 0, (int)viewportWidth, y);
                        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                        GL11.glViewport(0, y + height, (int)viewportWidth, y);
                        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                    }

                    // Reset viewport to the virtual viewport
                    GL11.glViewport(x, y, width, height);

                    // Restore original clear color
                    GL11.glClearColor(currentColor[0], currentColor[1], currentColor[2], currentColor[3]);
                }
            } else {
                // Use full viewport area
                GL11.glViewport(0, 0, (int)viewportWidth, (int)viewportHeight);
            }
        } catch (Exception e) {
            System.err.println("Error configuring viewport: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates the camera matrices and prepares for rendering
     */
    public void updateCamera() {
        if (camera == null) {
            System.err.println("WARNING: No camera set in RenderSystem");
            return;
        }

        try {
            // Get updated matrices from camera
            camera.getViewMatrix(viewMatrix);
            camera.getProjectionMatrix(projectionMatrix);

            // Compute the combined view-projection matrix
            viewProjectionMatrix.set(projectionMatrix).mul(viewMatrix);

            if (verboseLogging) {
                System.out.println("Camera matrices updated");
                System.out.println("View matrix: " + matrixToString(viewMatrix));
                System.out.println("Projection matrix: " + matrixToString(projectionMatrix));
                System.out.println("ViewProjection matrix: " + matrixToString(viewProjectionMatrix));
            }
        } catch (Exception e) {
            System.err.println("ERROR updating camera matrices: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Submit a renderable object to be drawn this frame
     * @param renderable The object to render
     */
    public void submit(Renderable renderable) {
        if (renderable == null) {
            if (verboseLogging) {
                System.out.println("WARNING: Null renderable submitted to render system");
            }
            return;
        }

        // Skip if frustum culling is enabled and the object is outside view
        if (frustumCullingEnabled && !isInViewFrustum(renderable)) {
            objectsCulled++;
            if (verboseLogging) {
                System.out.println("Culled object: " + renderable.getClass().getSimpleName());
            }
            return;
        }

        // Handle transparent objects separately for back-to-front rendering
        if (renderable.isTransparent()) {
            transparentObjects.add(renderable);
            if (verboseLogging) {
                System.out.println("Added transparent object: " + renderable.getClass().getSimpleName());
            }
            return;
        }

        // Get the render layer based on z-order (using floor to group similar depths)
        int layer = (int) Math.floor(renderable.getZ() * 100);

        // Add to the appropriate render layer
        renderLayers.computeIfAbsent(layer, k -> new ArrayList<>()).add(renderable);

        if (verboseLogging) {
            System.out.println("Added object to layer " + layer + ": " + renderable.getClass().getSimpleName());
        }
    }

    /**
     * Adds multiple renderables from a GameObject and its components
     * @param gameObject The game object containing renderables
     */
    public void submitGameObject(GameObject gameObject) {
        if (gameObject == null || !gameObject.isActive()) {
            if (verboseLogging && gameObject == null) {
                System.out.println("WARNING: Null GameObject submitted to render system");
            }
            return;
        }

        if (verboseLogging) {
            System.out.println("Processing GameObject: " + gameObject.getName());
        }

        // Add any renderables directly attached to the game object
        if (gameObject instanceof Renderable) {
            submit((Renderable) gameObject);
        }

        // Add renderables from all components
        for (Component component : gameObject.getComponents()) {
            if (component instanceof Renderable) {
                if (verboseLogging) {
                    System.out.println("Found renderable component: " + component.getClass().getSimpleName());
                }
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
        configureViewport();

        if (verboseLogging) {
            System.out.println("=== RENDER FRAME START ===");
            System.out.println("Rendering " + renderLayers.size() + " layers with " +
                    countTotalObjects() + " objects");
            System.out.println("Transparent objects: " + transparentObjects.size());
        }

        // Check if camera is available
        if (camera == null) {
            System.err.println("ERROR: Cannot render without a camera");
            return;
        }

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

        if (verboseLogging) {
            System.out.println("Sorted layers: " + sortedLayers);
        }

        // Render opaque objects front-to-back for depth buffer optimization
        for (Integer layer : sortedLayers) {
            List<Renderable> layerObjects = renderLayers.get(layer);

            if (verboseLogging) {
                System.out.println("Rendering layer " + layer + " with " + layerObjects.size() + " objects");
            }

            // Sort within layer for deterministic rendering
            layerObjects.sort(new ZComparator());

            // Batch and render objects in this layer
            batchAndRender(layerObjects);
        }

        // Enable alpha blending for transparent objects
        glEnable(GL_BLEND);

        // Sort transparent objects back-to-front for proper blending
        transparentObjects.sort((a, b) -> Float.compare(b.getZ(), a.getZ()));

        if (verboseLogging) {
            System.out.println("Rendering " + transparentObjects.size() + " transparent objects");
        }

        // Render transparent objects
        for (Renderable renderable : transparentObjects) {
            try {
                if (verboseLogging) {
                    System.out.println("Rendering transparent object: " + renderable.getClass().getSimpleName());
                }
                renderable.render(this, viewProjectionMatrix);
                drawCalls++;
                objectsRendered++;
            } catch (Exception e) {
                System.err.println("ERROR rendering transparent object: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Clear render queues
        renderLayers.clear();
        transparentObjects.clear();

        // Render debug information if enabled
        if (debugMode) {
            renderDebugInfo();
        }

        if (verboseLogging) {
            System.out.println("Render complete: " + objectsRendered + " objects, " +
                    drawCalls + " draw calls, " + objectsCulled + " culled");
            System.out.println("=== RENDER FRAME END ===");
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

            if (verboseLogging) {
                System.out.println("Rendering batch of " + batch.size() + " objects with material: " +
                        (material != null ? material.getClass().getSimpleName() : "null"));
            }

            if (material == null) {
                System.err.println("WARNING: Null material in batch");
                continue;
            }

            // Bind the material only once for the whole batch
            try {
                material.bind();
            } catch (Exception e) {
                System.err.println("ERROR binding material: " + e.getMessage());
                e.printStackTrace();
                continue;
            }

            // Upload light information once per batch
            try {
                uploadLightingData(material.getShader());
            } catch (Exception e) {
                System.err.println("ERROR uploading lighting data: " + e.getMessage());
                e.printStackTrace();
            }

            // Render each object
            for (Renderable renderable : batch) {
                try {
                    if (verboseLogging) {
                        System.out.println("Rendering object: " + renderable.getClass().getSimpleName());
                    }
                    renderable.render(this, viewProjectionMatrix);
                    objectsRendered++;
                } catch (Exception e) {
                    System.err.println("ERROR rendering object: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            drawCalls++; // Each batch is one draw call

            // Unbind the material
            try {
                material.unbind();
            } catch (Exception e) {
                System.err.println("ERROR unbinding material: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Upload lighting data to a shader
     */
    private void uploadLightingData(ShaderManager.Shader shader) {
        if (shader == null) {
            System.err.println("WARNING: Null shader when uploading lighting data");
            return;
        }

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
        if (camera == null || renderable == null || renderable.getTransform() == null) {
            return true; // Default to visible if we can't determine
        }

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
        // This would be implemented with a debug renderer
        System.out.println("Render stats: " + objectsRendered + " objects, " +
                drawCalls + " draw calls, " + objectsCulled + " culled");
    }

    /**
     * Add a light to the scene
     */
    public void addLight(Light light) {
        if (light == null) return;

        lights.add(light);
        if (verboseLogging) {
            System.out.println("Added light: " + light.type);
        }
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
        if (verboseLogging) {
            System.out.println("Set ambient light: " + r + ", " + g + ", " + b);
        }
    }

    /**
     * Count total objects in all layers
     */
    private int countTotalObjects() {
        int count = 0;
        for (List<Renderable> layer : renderLayers.values()) {
            count += layer.size();
        }
        return count;
    }

    /**
     * Convert matrix to string for debugging
     */
    private String matrixToString(Matrix4f matrix) {
        StringBuilder sb = new StringBuilder("Matrix[");
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                sb.append(String.format("%.2f", matrix.get(i, j)));
                if (i != 3 || j != 3) sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
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

    public void setVerboseLogging(boolean verbose) {
        this.verboseLogging = verbose;
    }
}