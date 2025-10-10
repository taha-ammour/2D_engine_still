package org.example.gfx;

public final class TextureAtlas {
    private final Texture texture;
    private final int cols, rows;

    public TextureAtlas(Texture texture, int cols, int rows) {
        this.texture = texture;
        this.cols = cols;
        this.rows = rows;
    }

    public Texture texture() { return texture; }

    /**
     * Returns [scaleU, scaleV, offsetU, offsetV] for the given frame index (0..cols*rows-1)
     * FIXED: Accounts for vertically flipped texture loading
     */
    public float[] frameUV(int index) {
        int cx = index % cols;  // column (x)
        int cy = index / cols;  // row (y)

        // Since STBImage flips the texture vertically, we need to invert the row
        // Row 0 in our frame index should map to the bottom row in UV space
        int flippedRow = (rows - 1) - cy;

        float su = 1.0f / cols;  // width of one frame in UV space
        float sv = 1.0f / rows;  // height of one frame in UV space
        float ou = cx * su;       // U offset (horizontal)
        float ov = flippedRow * sv; // V offset (vertical) - using flipped row

        return new float[]{ su, sv, ou, ov };
    }

    public float texelW() { return 1.0f / texture.width(); }
    public float texelH() { return 1.0f / texture.height(); }

    /**
     * Debug method to verify frame mapping
     */
    public void debugFrameMapping() {
        System.out.println("TextureAtlas Debug: " + cols + "x" + rows + " grid");
        for (int i = 0; i < cols * rows; i++) {
            float[] uv = frameUV(i);
            int col = i % cols;
            int row = i / cols;
            System.out.printf("Frame %d (row=%d, col=%d) -> UV: scale=(%.3f, %.3f), offset=(%.3f, %.3f)%n",
                    i, row, col, uv[0], uv[1], uv[2], uv[3]);
        }
    }
}