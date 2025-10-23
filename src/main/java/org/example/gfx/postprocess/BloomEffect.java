// src/main/java/org/example/gfx/postprocess/BloomEffect.java
package org.example.gfx.postprocess;

import org.example.gfx.Framebuffer;
import org.example.gfx.Shader;
import org.example.gfx.Shaders;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Bloom post-processing effect
 * Creates glow around bright areas
 */
public final class BloomEffect implements AutoCloseable {
    private final Shader brightPassShader;
    private final Shader blurShader;
    private final Shader combineShader;

    private final Framebuffer brightPass;
    private final Framebuffer blurH;
    private final Framebuffer blurV;

    private final int quadVAO;
    private final int quadVBO;

    private float threshold = 0.7f;
    private float intensity = 1.0f;
    private int blurPasses = 2;

    public BloomEffect(int width, int height) {
        // Create shaders
        brightPassShader = new Shader(
                Shaders.POSTPROCESS_VERT,
                BRIGHT_PASS_FRAG,
                false
        );

        blurShader = new Shader(
                Shaders.POSTPROCESS_VERT,
                Shaders.BLUR_FRAG,
                false
        );

        combineShader = new Shader(
                Shaders.POSTPROCESS_VERT,
                COMBINE_FRAG,
                false
        );

        // Create framebuffers
        brightPass = new Framebuffer(width / 2, height / 2); // Half resolution for performance
        blurH = new Framebuffer(width / 2, height / 2);
        blurV = new Framebuffer(width / 2, height / 2);

        // Create fullscreen quad
        float[] quadVertices = {
                // positions   // texCoords
                -1f,  1f,      0f, 1f,
                -1f, -1f,      0f, 0f,
                1f, -1f,      1f, 0f,

                -1f,  1f,      0f, 1f,
                1f, -1f,      1f, 0f,
                1f,  1f,      1f, 1f
        };

        quadVAO = glGenVertexArrays();
        quadVBO = glGenBuffers();

        glBindVertexArray(quadVAO);
        glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
        glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);

        glBindVertexArray(0);
    }

    /**
     * Apply bloom to the input texture
     */
    public void apply(int sourceTexture, int screenWidth, int screenHeight) {
        // 1. Extract bright pixels
        brightPass.bind();
        glClear(GL_COLOR_BUFFER_BIT);
        brightPassShader.bind();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, sourceTexture);
        glUniform1i(glGetUniformLocation(brightPassShader.id(), "uTexture"), 0);
        glUniform1f(glGetUniformLocation(brightPassShader.id(), "uThreshold"), threshold);

        renderQuad();

        // 2. Blur bright pixels (multi-pass)
        for (int i = 0; i < blurPasses; i++) {
            // Horizontal blur
            blurH.bind();
            glClear(GL_COLOR_BUFFER_BIT);
            blurShader.bind();

            int texToBlur = (i == 0) ? brightPass.getColorTexture() : blurV.getColorTexture();
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texToBlur);
            glUniform1i(glGetUniformLocation(blurShader.id(), "uTexture"), 0);
            glUniform2f(glGetUniformLocation(blurShader.id(), "uTexelSize"),
                    1.0f / blurH.getWidth(), 0f);
            glUniform1f(glGetUniformLocation(blurShader.id(), "uBlurAmount"), 1f);

            renderQuad();

            // Vertical blur
            blurV.bind();
            glClear(GL_COLOR_BUFFER_BIT);
            blurShader.bind();

            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, blurH.getColorTexture());
            glUniform1i(glGetUniformLocation(blurShader.id(), "uTexture"), 0);
            glUniform2f(glGetUniformLocation(blurShader.id(), "uTexelSize"),
                    0f, 1.0f / blurV.getHeight());
            glUniform1f(glGetUniformLocation(blurShader.id(), "uBlurAmount"), 1f);

            renderQuad();
        }

        // 3. Combine original + bloom
        Framebuffer.unbind(screenWidth, screenHeight);
        combineShader.bind();

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, sourceTexture);
        glUniform1i(glGetUniformLocation(combineShader.id(), "uScene"), 0);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, blurV.getColorTexture());
        glUniform1i(glGetUniformLocation(combineShader.id(), "uBloom"), 1);
        glUniform1f(glGetUniformLocation(combineShader.id(), "uIntensity"), intensity);

        renderQuad();
    }

    private void renderQuad() {
        glBindVertexArray(quadVAO);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
    }

    public void setThreshold(float threshold) { this.threshold = threshold; }
    public void setIntensity(float intensity) { this.intensity = intensity; }
    public void setBlurPasses(int passes) { this.blurPasses = Math.max(1, passes); }

    @Override
    public void close() {
        brightPassShader.close();
        blurShader.close();
        combineShader.close();
        brightPass.close();
        blurH.close();
        blurV.close();
        glDeleteVertexArrays(quadVAO);
        glDeleteBuffers(quadVBO);
    }

    // ===== SHADERS =====

    private static final String BRIGHT_PASS_FRAG = """
    #version 330 core
    in vec2 vUV;
    out vec4 FragColor;
    
    uniform sampler2D uTexture;
    uniform float uThreshold;
    
    void main() {
        vec4 color = texture(uTexture, vUV);
        float brightness = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
        
        if (brightness > uThreshold) {
            FragColor = color;
        } else {
            FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        }
    }
    """;

    private static final String COMBINE_FRAG = """
    #version 330 core
    in vec2 vUV;
    out vec4 FragColor;
    
    uniform sampler2D uScene;
    uniform sampler2D uBloom;
    uniform float uIntensity;
    
    void main() {
        vec3 scene = texture(uScene, vUV).rgb;
        vec3 bloom = texture(uBloom, vUV).rgb;
        
        // Additive blending
        vec3 result = scene + bloom * uIntensity;
        
        FragColor = vec4(result, 1.0);
    }
    """;
}