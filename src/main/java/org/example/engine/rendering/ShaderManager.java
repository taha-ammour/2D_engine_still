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
    private boolean verboseLogging = false;

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
        if (verboseLogging) {
            System.out.println("ShaderManager initialized");
        }
    }

    /**
     * Add a shader directly (useful for default shaders)
     */
    public void addShader(String name, Shader shader) {
        if (shader != null) {
            shaders.put(name, shader);
            if (verboseLogging) {
                System.out.println("Added shader '" + name + "' with program ID: " + shader.getProgramId());
            }
        }
    }

    /**
     * Create a default shader with source code
     */
    public Shader createDefaultShader(String name, String vertexShaderSource, String fragmentShaderSource) {
        if (verboseLogging) {
            System.out.println("Creating default shader: " + name);
        }

        try {
            Shader shader = new Shader(vertexShaderSource, fragmentShaderSource);
            shaders.put(name, shader);
            return shader;
        } catch (Exception e) {
            System.err.println("Error creating default shader '" + name + "': " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Load a shader from shader source files
     */
    public Shader loadShader(String name, String vertexShaderPath, String fragmentShaderPath) {
        if (shaders.containsKey(name)) {
            if (verboseLogging) {
                System.out.println("Returning existing shader: " + name);
            }
            return shaders.get(name);
        }

        try {
            if (verboseLogging) {
                System.out.println("Loading shader '" + name + "' from:");
                System.out.println("  Vertex: " + vertexShaderPath);
                System.out.println("  Fragment: " + fragmentShaderPath);
            }

            String vertexShaderSource = loadShaderSource(vertexShaderPath);
            String fragmentShaderSource = loadShaderSource(fragmentShaderPath);

            Shader shader = new Shader(vertexShaderSource, fragmentShaderSource);
            shader.setShaderPaths(vertexShaderPath, fragmentShaderPath);
            shaders.put(name, shader);

            if (verboseLogging) {
                System.out.println("Shader '" + name + "' loaded successfully with program ID: " + shader.getProgramId());
            }

            return shader;
        } catch (Exception e) {
            System.err.println("Error loading shader '" + name + "': " + e.getMessage());
            e.printStackTrace();

            // Create a simple fallback shader
            return createSimpleFallbackShader(name);
        }
    }

    /**
     * Create a simple fallback shader when loading fails
     */
    private Shader createSimpleFallbackShader(String name) {
        System.out.println("Creating simple fallback shader for: " + name);

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
                        "void main() {\n" +
                        "    FragColor = u_Color;\n" +
                        "}\n";

        try {
            Shader shader = new Shader(vertexSource, fragmentSource);
            shaders.put(name, shader);
            System.out.println("Fallback shader created with program ID: " + shader.getProgramId());
            return shader;
        } catch (Exception e) {
            System.err.println("Failed to create fallback shader: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a shader by name
     */
    public Shader getShader(String name) {
        Shader shader = shaders.get(name);
        if (shader == null && verboseLogging) {
            System.out.println("Shader not found: " + name);
        }
        return shader;
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

            if (verboseLogging) {
                System.out.println("Loaded shader source from: " + resourcePath);
                if (source.length() > 500) {
                    System.out.println("Source length: " + source.length() + " characters");
                } else {
                    System.out.println("Source: " + source.toString());
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

        // Added for debugging
        private boolean verboseLogging = false;

        /**
         * Create a shader from source code
         */
        public Shader(String vertexShaderSource, String fragmentShaderSource) {
            if (verboseLogging) {
                System.out.println("Creating shader from source code");
            }

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

            if (verboseLogging) {
                System.out.println("Vertex shader compiled successfully");
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

            if (verboseLogging) {
                System.out.println("Fragment shader compiled successfully");
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

            if (verboseLogging) {
                System.out.println("Shader program linked successfully with ID: " + programId);
            }

            // Shaders are linked into the program, so we can delete them
            glDeleteShader(vertexShaderId);
            glDeleteShader(fragmentShaderId);

            // Validate the program
            glValidateProgram(programId);
            if (glGetProgrami(programId, GL_VALIDATE_STATUS) == GL_FALSE) {
                System.err.println("WARNING: Shader validation: " + glGetProgramInfoLog(programId));
            }
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
            if (verboseLogging) {
                System.out.println("Using shader program: " + programId);
            }
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

            if (location == -1 && verboseLogging &&
                    !name.startsWith("u_") && !name.startsWith("lights[")) {
                System.out.println("WARNING: Uniform '" + name + "' not found in shader " + programId);
            }

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