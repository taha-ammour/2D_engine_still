package org.example.gfx;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public final class Texture implements AutoCloseable {
    private final int id;
    private final int width, height;

    public Texture(ByteBuffer image, int width, int height, int channels) {
        this.width = width; this.height = height;
        id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        int format = (channels == 4) ? GL_RGBA : GL_RGB;
        glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, image);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public static Texture load(String path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);
            STBImage.stbi_set_flip_vertically_on_load(true);
            ByteBuffer data = STBImage.stbi_load(path, w, h, c, 0);
            if (data == null) throw new RuntimeException("Failed to load image: " + path);
            Texture t = new Texture(data, w.get(0), h.get(0), c.get(0));
            STBImage.stbi_image_free(data);
            return t;
        }
    }

    /** Create a solid-color RGBA texture of size w×h. rgba = 0xRRGGBBAA. */
    public static Texture makeSolid(int w, int h, int rgba) {
        java.nio.ByteBuffer buf = org.lwjgl.BufferUtils.createByteBuffer(w * h * 4);
        byte r = (byte)((rgba >> 24) & 0xFF);
        byte g = (byte)((rgba >> 16) & 0xFF);
        byte b = (byte)((rgba >> 8)  & 0xFF);
        byte a = (byte)( rgba        & 0xFF);
        for (int i = 0; i < w*h; i++) buf.put(r).put(g).put(b).put(a);
        buf.flip();
        return new Texture(buf, w, h, 4);
    }

    public void bind(int unit) {
        glActiveTexture(GL_TEXTURE0 + unit);
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public static void unbind() { glBindTexture(GL_TEXTURE_2D, 0); }
    public int width() { return width; }
    public int height() { return height; }

    @Override public void close() { glDeleteTextures(id); }
}
