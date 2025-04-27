package org.example.game.map;

import org.example.engine.SpriteManager;
import org.example.engine.core.GameObject;
import org.example.engine.physics.BoxCollider;
import org.example.engine.rendering.Sprite;
import org.example.engine.scene.Scene;

/**
 * Creates a room with walls and floor tiles
 */
public class RoomGenerator {
    // Tile sizes
    // Base tile size - sprites are generally 16x16 pixels
    private final Biome biome;

    private static final float BASE_TILE_SIZE = 16.0f;

    // Scale factor for sprites (matching your game's overall scaling)
    private static final float SCALE_FACTOR = 2.0f;

    // Effective tile sizes after scaling
    private static final float TILE_SIZE = BASE_TILE_SIZE * SCALE_FACTOR;
    private static final float HALF_TILE_SIZE = BASE_TILE_SIZE * SCALE_FACTOR / 2.0f;

    // Room parameters
    private final int roomWidth;  // In tiles (normal size)
    private final int roomHeight; // In tiles (normal size)
    private final float startX;   // Starting X position
    private final float startY;   // Starting Y position

    // Resources
    private final SpriteManager spriteManager;
    private final Scene scene;

    public RoomGenerator(Scene scene, SpriteManager spriteManager, int roomWidth, int roomHeight, float startX, float startY) {
        this.scene = scene;
        this.spriteManager = spriteManager;
        this.roomWidth = roomWidth;
        this.roomHeight = roomHeight;
        this.startX = startX;
        this.startY = startY;
        this.biome = Biome.randomBiome();
    }

    /**
     * Generate the complete room
     */
    public void generateRoom() {
        // Generate floor
        generateFloor();

        // Generate walls
        generateWalls();
    }

    /**
     * Generate the floor with walkable tiles
     */
    private void generateFloor() {
        // Use a mix of different walkable tile variations for visual interest
        String[] floorTileTypes = {
                "tile_walkable_1",
                "tile_walkable_2",
                "tile_walkable_3",
                "tile_walkable_4",
                "tile_walkable_5",
                "tile_walkable_6",
                "tile_walkable_7",
                "tile_walkable_8"
        };

        // Create floor tiles
        for (int y = 0; y < roomHeight; y++) {
            for (int x = 0; x < roomWidth; x++) {
                // Choose a random tile type for variety
                int tileIndex = (int)(Math.random() * floorTileTypes.length);
                String tileType = floorTileTypes[tileIndex];

                // Calculate position
                float posX = startX + (x * TILE_SIZE);
                float posY = startY + (y * TILE_SIZE);

                // Create floor tile
                createTile(tileType, posX, posY, -0.1f, false);
            }
        }
    }

    /**
     * Generate walls around the room
     */
    /**
     * Generate walls around the room
     */
    private void generateWalls() {
        float wallZ = -0.09f; // Walls at same Z level as player

        // Calculate room bounds
        float roomStartX = startX;
        float roomStartY = startY;
        float roomEndX = startX + (roomWidth * TILE_SIZE);
        float roomEndY = startY + (roomHeight * TILE_SIZE);

        // TOP WALL
        // Top-left corner
        createTile("tile_sp_ul_1", roomStartX, roomStartY, wallZ, true);

        // Top edge - fix: use roomWidth - 2 for the correct number of tiles
        for (int x = 1; x < roomWidth - 1; x++) {
            createTile("tile_up_3u", roomStartX + (x * TILE_SIZE), roomStartY, wallZ, true);
        }

        // Top-right corner
        createTile("tile_sp_ur_1", roomEndX - TILE_SIZE, roomStartY, wallZ, true);

        // BOTTOM WALL
        // Bottom-left corner
        createTile("tile_corner_ld_1", roomStartX, roomEndY - TILE_SIZE, wallZ, true);

        // Bottom edge - fix: use roomWidth - 2 for the correct number of tiles
        for (int x = 1; x < roomWidth - 1; x++) {
            createTile("tile_down_1", roomStartX + (x * TILE_SIZE), roomEndY - TILE_SIZE, wallZ, true);
        }

        // Bottom-right corner
        createTile("tile_corner_rd_1", roomEndX - TILE_SIZE, roomEndY - TILE_SIZE, wallZ, true);

        // LEFT WALL - fix: use roomHeight - 2 to account for corners
        for (int y = 1; y < roomHeight - 1; y++) {
            createTile("tile_l_1", roomStartX, roomStartY + (y * TILE_SIZE), wallZ, true);
        }

        // RIGHT WALL - fix: use roomHeight - 2 to account for corners
        for (int y = 1; y < roomHeight - 1; y++) {
            createTile("tile_r_1", roomEndX - TILE_SIZE, roomStartY + (y * TILE_SIZE), wallZ, true);
        }

        // Add some decorative elements on the walls
        addWallDecorations();
    }

    /**
     * Add decorative elements to the walls
     */
    private void addWallDecorations() {
        // Add some wall torches or decorations
        float torchZ = 0.0f;

        // Add a few torches on the left wall
        createTile("tile_candle_1", startX, startY + (roomHeight * TILE_SIZE / 3), torchZ, false);
        createTile("tile_candle_1", startX, startY + (2 * roomHeight * TILE_SIZE / 3), torchZ, false);

        // Add a few torches on the right wall
        createTile("tile_candle_1", startX + (roomWidth * TILE_SIZE) - HALF_TILE_SIZE,
                startY + (roomHeight * TILE_SIZE / 3), torchZ, false);
        createTile("tile_candle_1", startX + (roomWidth * TILE_SIZE) - HALF_TILE_SIZE,
                startY + (2 * roomHeight * TILE_SIZE / 3), torchZ, false);

        // Add decorative elements (banners, etc)
        int numDecorations = Math.min(roomWidth / 3, 3); // Limit decorations based on room size
        for (int i = 0; i < numDecorations; i++) {
            int position = (i + 1) * (roomWidth / (numDecorations + 1));
            createTile("tile_banner", startX + (position * TILE_SIZE),
                    startY + HALF_TILE_SIZE, torchZ, false);
        }
    }

    /**
     * Create a tile at the specified position
     *
     * @param tileName The name of the tile sprite
     * @param x X position
     * @param y Y position
     * @param z Z position (depth)
     * @param isCollider Whether the tile should have a collider
     */
    private GameObject createTile(String tileName, float x, float y, float z, boolean isCollider) {
        GameObject tile = new GameObject("Tile_" + tileName + "_" + x + "_" + y);
        tile.setPosition(x, y, z);
        tile.setScale(2.0f,2.0f,1.0f);

        // Create sprite
        Sprite sprite = spriteManager.createSprite(tileName);
        if (sprite != null) {
            sprite.setPaletteFromCodes(biome.getPaletteCodes());
            tile.addComponent(sprite);

            // Add collider for walls
            if (isCollider) {
                BoxCollider collider = new BoxCollider(sprite.getWidth(), sprite.getHeight());
                tile.addComponent(collider);
            }

            // Add to scene
            scene.addGameObject(tile);
            return tile;
        } else {
            System.err.println("Could not create sprite for tile: " + tileName);
            return null;
        }
    }
}