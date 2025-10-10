package org.example.gfx;

public final class TextureAtlas {
    private final Texture texture;
    private final int cols, rows;

    public TextureAtlas(Texture texture, int cols, int rows) {
        this.texture = texture;
        this.cols = cols; this.rows = rows;
    }

    public Texture texture() { return texture; }

    /**
     * Returns [scaleU, scaleV, offsetU, offsetV] for the given frame index (0..cols*rows-1)
     */
    public float[] frameUV(int index) {
        int cx = index % cols;
        int cy = index / cols;
        float su = 1.0f / cols;
        float sv = 1.0f / rows;
        float ou = cx * su;
        float ov = cy * sv;
        return new float[]{ su, sv, ou, ov };
    }

    public float texelW() { return 1.0f / texture.width(); }
    public float texelH() { return 1.0f / texture.height(); }
}
