// src/main/java/org/example/gfx/Renderer2D.java
package org.example.gfx;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;

/**
 * Improved Renderer2D with shader management and lazy initialization
 */
public final class Renderer2D implements AutoCloseable {
    private int vao, vbo, ebo;
    private final ShaderManager shaderManager;
    private final Camera2D camera;
    private final FloatBuffer fb = BufferUtils.createFloatBuffer(16);

    private int cameraUbo;
    private Texture defaultWhite;
    private boolean initialized = false;

    // Current shader state
    private Shader currentShader;
    private int currentShaderId = -1;

    // Cached uniform locations (per shader)
    private int locModel, locTint, locUVTr, locEff, locTexel, locTime, locUseTex;

    public Renderer2D(Camera2D camera) {
        this(camera, new ShaderManager());
    }

    public Renderer2D(Camera2D camera, ShaderManager shaderManager) {
        this.camera = camera;
        this.shaderManager = shaderManager;
        // Don't initialize anything OpenGL-related here!
    }

    /**
     * MUST be called after OpenGL context is created
     */
    public void init() {
        if (initialized) return;

        // Initialize shaders first
        shaderManager.init();

        // Setup quad geometry
        float[] quad = {
                // x, y,  u, v
                0f, 0f,  0f, 0f,
                1f, 0f,  1f, 0f,
                1f, 1f,  1f, 1f,
                0f, 1f,  0f, 1f,
        };
        int[] indices = { 0,1,2, 2,3,0 };

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, quad, GL_STATIC_DRAW);

        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0L);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2L * Float.BYTES);

        glBindVertexArray(0);

        // Create Camera UBO
        cameraUbo = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, cameraUbo);
        glBufferData(GL_UNIFORM_BUFFER, 2L * 16 * Float.BYTES, GL_DYNAMIC_DRAW);
        glBindBufferBase(GL_UNIFORM_BUFFER, 0, cameraUbo);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        // Link all shaders to Camera UBO
        for (String shaderName : new String[]{"sprite", "effects"}) {
            Shader shader = shaderManager.get(shaderName);
            int blockIndex = glGetUniformBlockIndex(shader.id(), "Camera");
            if (blockIndex != -1) {
                glUniformBlockBinding(shader.id(), blockIndex, 0);
            }
        }

        defaultWhite = Texture.makeSolid(1, 1, 0xFFFFFFFF);
        initialized = true;
    }

    public void begin(int backbufferWidth, int backbufferHeight) {
        if (!initialized) {
            throw new IllegalStateException("Renderer2D.init() must be called before begin()");
        }

        // Update camera UBO once per frame
        glBindBuffer(GL_UNIFORM_BUFFER, cameraUbo);
        camera.projection().get(fb.clear());
        glBufferSubData(GL_UNIFORM_BUFFER, 0L, fb);
        camera.view().get(fb.clear());
        glBufferSubData(GL_UNIFORM_BUFFER, 16L * Float.BYTES, fb);
        glBindBuffer(GL_UNIFORM_BUFFER, 0);

        // Start with default shader
        useShader(shaderManager.getDefault());
    }

    public void end() {
        if (currentShader != null) {
            Shader.unbind();
            currentShader = null;
            currentShaderId = -1;
        }
    }

    /**
     * Switch to a different shader
     */
    public void useShader(Shader shader) {
        if (shader.id() == currentShaderId) {
            return; // Already using this shader
        }

        currentShader = shader;
        currentShaderId = shader.id();
        shader.bind();

        // Cache uniform locations for this shader
        locModel = glGetUniformLocation(shader.id(), "uModel");
        locTint  = glGetUniformLocation(shader.id(), "uTint");
        locUVTr  = glGetUniformLocation(shader.id(), "uUVTransform");
        locEff   = glGetUniformLocation(shader.id(), "uEffectFlags");
        locTexel = glGetUniformLocation(shader.id(), "uTexelSize");
        locTime  = glGetUniformLocation(shader.id(), "uTime");
        locUseTex= glGetUniformLocation(shader.id(), "uUseTex");

        // Set texture sampler
        int locTex = glGetUniformLocation(shader.id(), "uTex");
        if (locTex != -1) {
            glUniform1i(locTex, 0);
        }
    }

    /**
     * Draw with Material (recommended)
     */
    public void drawQuad(float x, float y, float w, float h, float rotRadians,
                         Material material, float[] uvTransform, float texelW, float texelH) {

        // Switch shader if needed
        Shader shader = shaderManager.get(material.getShaderName());
        useShader(shader);

        Texture tex = material.getTexture();
        drawQuadInternal(x, y, w, h, rotRadians, tex, material.getTint(),
                uvTransform, material.getEffectFlags(), texelW, texelH);
    }

    /**
     * Legacy method - direct draw (for backward compatibility)
     */
    public void drawQuad(float x, float y, float w, float h, float rotRadians,
                         Texture tex, Vector4f tint, float[] uvTransform,
                         int effectFlags, float texelW, float texelH, float timeSec) {

        drawQuadInternal(x, y, w, h, rotRadians, tex, tint,
                uvTransform, effectFlags, texelW, texelH);
    }

    private void drawQuadInternal(float x, float y, float w, float h, float rotRadians,
                                  Texture tex, Vector4f tint, float[] uvTransform,
                                  int effectFlags, float texelW, float texelH) {

        Matrix4f model = new Matrix4f()
                .translate(x + w*0.5f, y + h*0.5f, 0f)
                .rotateZ(rotRadians)
                .translate(-w*0.5f, -h*0.5f, 0f)
                .scale(w, h, 1f);

        // Set uniforms
        if (locModel != -1) glUniformMatrix4fv(locModel, false, model.get(fb.clear()));
        if (locTint != -1)  glUniform4f(locTint,  tint.x, tint.y, tint.z, tint.w);
        if (locUVTr != -1)  glUniform4f(locUVTr,  uvTransform[0], uvTransform[1], uvTransform[2], uvTransform[3]);
        if (locEff != -1)   glUniform1i(locEff,   effectFlags);
        if (locTexel != -1) glUniform2f(locTexel, texelW, texelH);
        if (locTime != -1)  glUniform1f(locTime,  (float)(System.nanoTime() * 1e-9));

        glBindVertexArray(vao);

        boolean hasTex = (tex != null);
        if (locUseTex != -1) glUniform1i(locUseTex, hasTex ? 1 : 0);

        Texture toBind = hasTex ? tex : defaultWhite;
        toBind.bind(0);

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0L);

        Texture.unbind();
        glBindVertexArray(0);
    }

    public ShaderManager getShaderManager() {
        return shaderManager;
    }

    public Camera2D getCamera() {
        return camera;
    }

    @Override
    public void close() {
        if (initialized) {
            shaderManager.close();
            defaultWhite.close();
            glDeleteBuffers(ebo);
            glDeleteBuffers(vbo);
            glDeleteVertexArrays(vao);
            glDeleteBuffers(cameraUbo);
        }
    }
}