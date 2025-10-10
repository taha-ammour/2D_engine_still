// src/main/java/org/example/gfx/Shader.java
package org.example.gfx;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public final class Shader implements AutoCloseable {
    private final int program;

    public Shader(String vertexSource, String fragmentSource, boolean isPath) {
        String vsrc = isPath ? readFile(vertexSource) : vertexSource;
        String fsrc = isPath ? readFile(fragmentSource) : fragmentSource;

        int vs = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs, vsrc);
        glCompileShader(vs);
        checkShader(vs, "VERTEX");

        int fs = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs, fsrc);
        glCompileShader(fs);
        checkShader(fs, "FRAGMENT");

        program = glCreateProgram();
        glAttachShader(program, vs);
        glAttachShader(program, fs);
        glLinkProgram(program);
        checkProgram(program);

        glDeleteShader(vs);
        glDeleteShader(fs);
    }

    public void bind() { glUseProgram(program); }
    public static void unbind() { glUseProgram(0); }
    public int id() { return program; }

    @Override public void close() { glDeleteProgram(program); }

    private static void checkShader(int id, String label) {
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new IllegalStateException(label + " compile error:\n" + glGetShaderInfoLog(id));
        }
    }
    private static void checkProgram(int id) {
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
            throw new IllegalStateException("Program link error:\n" + glGetProgramInfoLog(id));
        }
    }
    private static String readFile(String p) {
        try { return Files.readString(Path.of(p), StandardCharsets.UTF_8); }
        catch (Exception e) { throw new RuntimeException("Failed reading " + p, e); }
    }
}
