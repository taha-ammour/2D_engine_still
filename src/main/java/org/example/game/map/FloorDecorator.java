package org.example.game.map;

import org.example.engine.SpriteManager;
import org.example.engine.core.GameObject;
import org.example.engine.rendering.Sprite;
import org.example.engine.scene.Scene;

/**
 * Adds decorative elements to floor tiles
 */
public class FloorDecorator {
    private final SpriteManager spriteManager;
    private final Scene scene;
    private final float startX;
    private final float startY;
    private final int roomWidth;
    private final int roomHeight;
    private final float tileSize = 16.0f;

    public FloorDecorator(Scene scene, SpriteManager spriteManager,
                          float startX, float startY,
                          int roomWidth, int roomHeight) {
        this.scene = scene;
        this.spriteManager = spriteManager;
        this.startX = startX;
        this.startY = startY;
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;
    }

    /**
     * Add all decorations to the floor
     */
    public void decorateFloor() {
        // Add a carpet in the center
        addCarpet();

        // Add misc decorations
        addMiscDecorations();

        // Add light sources
        addLightSources();
    }

    /**
     * Add a carpet in the center of the room
     */
    private void addCarpet() {
        // Calculate carpet size (about half the room size)
        int carpetWidth = Math.max(3, roomWidth / 2);
        int carpetHeight = Math.max(2, roomHeight / 2);

        // Calculate start position (center the carpet)
        float carpetStartX = startX + ((roomWidth - carpetWidth) / 2f * tileSize);
        float carpetStartY = startY + ((roomHeight - carpetHeight) / 2f * tileSize);

        // Use different sprite for each carpet tile
        for (int y = 0; y < carpetHeight; y++) {
            for (int x = 0; x < carpetWidth; x++) {
                String tileType;

                // Determine which type of tile based on position (edge, corner, or center)
                if (x == 0 && y == 0) {
                    // Top-left corner
                    tileType = "tile_sp_ul_2";
                } else if (x == carpetWidth - 1 && y == 0) {
                    // Top-right corner
                    tileType = "tile_sp_ur_2";
                } else if (x == 0 && y == carpetHeight - 1) {
                    // Bottom-left corner
                    tileType = "tile_sp_ld_2";
                } else if (x == carpetWidth - 1 && y == carpetHeight - 1) {
                    // Bottom-right corner
                    tileType = "tile_sp_rd_2";
                } else if (x == 0) {
                    // Left edge
                    tileType = "tile_sp_l_2";
                } else if (x == carpetWidth - 1) {
                    // Right edge
                    tileType = "tile_sp_r_2";
                } else if (y == 0) {
                    // Top edge
                    tileType = "tile_sp_u_2";
                } else if (y == carpetHeight - 1) {
                    // Bottom edge
                    tileType = "tile_sp_d_2";
                } else {
                    // Center
                    tileType = "tile_sp_w_2";
                }

                // Calculate position
                float x1 = carpetStartX + (x * tileSize);
                float y1 = carpetStartY + (y * tileSize);

                // Create the carpet tile at a slightly higher Z value than the floor
                // but lower than the player
                createDecoration(tileType, x1, y1, -0.05f);
            }
        }
    }

    /**
     * Add miscellaneous decorations to the floor
     */
    private void addMiscDecorations() {

        // Calculate room bounds
        float roomCenterX = startX + (roomWidth * tileSize / 2);
        float roomCenterY = startY + (roomHeight * tileSize / 2);

        // Add spawn point in center
        createDecoration("tile_spawn_1u", roomCenterX, roomCenterY, -0.01f);
    }

    /**
     * Add lights to the room
     */
    private void addLightSources() {
        float lightZ = -0.15f;  // Make lights appear above other decorations

        // Add a few lights around the room
        createDecoration("tile_light_1",
                startX + (roomWidth * 0.25f * tileSize),
                startY + (roomHeight * 0.25f * tileSize),
                lightZ);

        createDecoration("tile_light_2",
                startX + (roomWidth * 0.75f * tileSize),
                startY + (roomHeight * 0.25f * tileSize),
                lightZ);

        createDecoration("tile_light_3",
                startX + (roomWidth * 0.25f * tileSize),
                startY + (roomHeight * 0.75f * tileSize),
                lightZ);

        createDecoration("tile_light_4",
                startX + (roomWidth * 0.75f * tileSize),
                startY + (roomHeight * 0.75f * tileSize),
                lightZ);

        // Add a lamp post in the center
        createDecoration("tile_lamp_post",
                startX + (roomWidth * 0.5f * tileSize),
                startY + (roomHeight * 0.5f * tileSize) - 16,
                lightZ);
    }

    /**
     * Add random decorations of a specific type
     *
     * @param decorationType The decoration sprite name
     * @param count How many to add
     */
    private void addRandomDecorations(String decorationType, int count) {
        // Add padding to keep decorations away from walls
        float paddingX = tileSize;
        float paddingY = tileSize;

        for (int i = 0; i < count; i++) {
            float x = startX + paddingX + (float)(Math.random() * (roomWidth * tileSize - 2 * paddingX));
            float y = startY + paddingY + (float)(Math.random() * (roomHeight * tileSize - 2 * paddingY));

            createDecoration(decorationType, x, y, -0.05f);
        }
    }

    /**
     * Create a decoration at the specified coordinates
     */
    private GameObject createDecoration(String spriteName, float x, float y, float z) {
        GameObject decoration = new GameObject("Decoration_" + spriteName + "_" + x + "_" + y);
        decoration.setPosition(x, y, z);
        decoration.setScale(2,2,1);

        Sprite sprite = spriteManager.createSprite(spriteName);
        if (sprite != null) {
            decoration.addComponent(sprite);
            scene.addGameObject(decoration);
            return decoration;
        } else {
            System.err.println("Failed to create decoration sprite: " + spriteName);
            return null;
        }
    }
}