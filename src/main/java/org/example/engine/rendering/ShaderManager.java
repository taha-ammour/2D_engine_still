package org.example.engine.rendering;

import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

/**
 * Manages loading, compiling, and caching shaders
 */
public class ShaderManager {
    private static ShaderManager instance;

    private final Map<String, Shader> shaders = new HashMap<>();

    /**
     * Singleton instance accessor
     */
    public static ShaderManager getInstance() {
        if (instance == null) {
            instance = new ShaderManager();
        }
        return instance;
    }

    /**
     * Private constructor for singleton
     */
    private ShaderManager() {
        // Initialize default shaders
    }

    /**
     * Load a shader from shader source files
     */
    public Shader loadShader(String name, String vertexShaderPath, String fragmentShaderPath) {
        if (shaders.containsKey(name)) {
            return shaders.get(name);
        }

        try {
            String vertexShaderSource = loadShaderSource(vertexShaderPath);
            String fragmentShaderSource = loadShaderSource(fragmentShaderPath);

            Shader shader = new Shader(vertexShaderSource, fragmentShaderSource);
            shaders.put(name, shader);

            return shader;
        } catch (Exception e) {
            System.err.println("Error loading shader '" + name + "': " + e.getMessage());

            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a shader by name
     */
    public Shader getShader(String name) {
        return shaders.get(name);
    }

    /**
     * Load shader source code from a file
     */
    private String loadShaderSource(String resourcePath) throws IOException {
        StringBuilder source = new StringBuilder();

        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Shader file not found: " + resourcePath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    source.append(line).append("\n");
                }
            }
        }

        return source.toString();
    }

    /**
     * Reload all shaders (useful for hot-reloading during development)
     */
    public void reloadAll() {
        for (Map.Entry<String, Shader> entry : new HashMap<>(shaders).entrySet()) {
            try {
                String name = entry.getKey();
                Shader shader = entry.getValue();

                // Get shader paths from metadata
                String vertexPath = shader.getVertexShaderPath();
                String fragmentPath = shader.getFragmentShaderPath();

                if (vertexPath != null && fragmentPath != null) {
                    // Delete old shader
                    shader.delete();

                    // Load new shader
                    Shader newShader = loadShader(name, vertexPath, fragmentPath);
                    shaders.put(name, newShader);

                    System.out.println("Reloaded shader: " + name);
                }
            } catch (Exception e) {
                System.err.println("Error reloading shader: " + e.getMessage());
            }
        }
    }

    /**
     * Clean up all shaders
     */
    public void cleanup() {
        for (Shader shader : shaders.values()) {
            shader.delete();
        }
        shaders.clear();
    }

    /**
     * Modern Shader implementation that wraps OpenGL shader program
     */
    public static class Shader {
        private final int programId;
        private final Map<String, Integer> uniformLocationCache = new HashMap<>();

        // Metadata for reloading
        private String vertexShaderPath;
        private String fragmentShaderPath;

        /**
         * Create a shader from source code
         */
        public Shader(String vertexShaderSource, String fragmentShaderSource) {
            // Compile vertex shader
            int vertexShaderId = glCreateShader(GL_VERTEX_SHADER);
            glShaderSource(vertexShaderId, vertexShaderSource);
            glCompileShader(vertexShaderId);

            // Check for compilation errors
            if (glGetShaderi(vertexShaderId, GL_COMPILE_STATUS) == GL_FALSE) {
                String log = glGetShaderInfoLog(vertexShaderId);
                glDeleteShader(vertexShaderId);
                throw new RuntimeException("Vertex shader compilation failed: " + log);
            }

            // Compile fragment shader
            int fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER);
            glShaderSource(fragmentShaderId, fragmentShaderSource);
            glCompileShader(fragmentShaderId);

            // Check for compilation errors
            if (glGetShaderi(fragmentShaderId, GL_COMPILE_STATUS) == GL_FALSE) {
                String log = glGetShaderInfoLog(fragmentShaderId);
                glDeleteShader(vertexShaderId);
                glDeleteShader(fragmentShaderId);
                throw new RuntimeException("Fragment shader compilation failed: " + log);
            }

            // Link the program
            programId = glCreateProgram();
            glAttachShader(programId, vertexShaderId);
            glAttachShader(programId, fragmentShaderId);
            glLinkProgram(programId);

            // Check for linking errors
            if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
                String log = glGetProgramInfoLog(programId);
                glDeleteShader(vertexShaderId);
                glDeleteShader(fragmentShaderId);
                glDeleteProgram(programId);
                throw new RuntimeException("Shader program linking failed: " + log);
            }

            // Shaders are linked into the program, so we can delete them
            glDeleteShader(vertexShaderId);
            glDeleteShader(fragmentShaderId);
        }

        /**
         * Set shader metadata for reloading
         */
        public void setShaderPaths(String vertexShaderPath, String fragmentShaderPath) {
            this.vertexShaderPath = vertexShaderPath;
            this.fragmentShaderPath = fragmentShaderPath;
        }

        /**
         * Get vertex shader path
         */
        public String getVertexShaderPath() {
            return vertexShaderPath;
        }

        /**
         * Get fragment shader path
         */
        public String getFragmentShaderPath() {
            return fragmentShaderPath;
        }

        /**
         * Use this shader program
         */
        public void use() {
            glUseProgram(programId);
        }

        /**
         * Delete this shader program
         */
        public void delete() {
            glDeleteProgram(programId);
        }

        /**
         * Get the OpenGL program ID
         */
        public int getProgramId() {
            return programId;
        }

        /**
         * Get the location of a uniform variable (with caching)
         */
        public int getUniformLocation(String name) {
            if (uniformLocationCache.containsKey(name)) {
                return uniformLocationCache.get(name);
            }

            int location = glGetUniformLocation(programId, name);
            uniformLocationCache.put(name, location);
            return location;
        }

        /**
         * Set a float uniform
         */
        public void setUniform1f(String name, float value) {
            int location = getUniformLocation(name);
            if (location != -1) {
                glUniform1f(location, value);
            }
        }

        /**
         * Set an integer uniform
         */
        public void setUniform1i(String name, int value) {
            int location = getUniformLocation(name);
            if (location != -1) {
                glUniform1i(location, value);
            }
        }

        /**
         * Set a Vector2f uniform
         */
        public void setUniform2f(String name, float x, float y) {
            int location = getUniformLocation(name);
            if (location != -1) {
                glUniform2f(location, x, y);
            }
        }

        /**
         * Set a Vector3f uniform
         */
        public void setUniform3f(String name, float x, float y, float z) {
            int location = getUniformLocation(name);
            if (location != -1) {
                glUniform3f(location, x, y, z);
            }
        }

        /**
         * Set a Vector4f uniform
         */
        public void setUniform4f(String name, float x, float y, float z, float w) {
            int location = getUniformLocation(name);
            if (location != -1) {
                glUniform4f(location, x, y, z, w);
            }
        }

        /**
         * Set a Matrix4f uniform
         */
        public void setUniformMatrix4fv(String name, FloatBuffer matrix) {
            int location = getUniformLocation(name);
            if (location != -1) {
                glUniformMatrix4fv(location, false, matrix);
            }
        }

        /**
         * Set an array of Vector3f uniforms
         */
        public void setUniform3fv(String name, float[] values) {
            int location = getUniformLocation(name);
            if (location != -1) {
                FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
                buffer.put(values).flip();
                glUniform3fv(location, buffer);
            }
        }
    }
}