package org.example.engine.physics;

import org.joml.Vector2f;
import java.util.*;

/**
 * Spatial partitioning grid for efficient collision detection.
 * Divides the world into grid cells for faster object querying.
 */
public class SpatialGrid {
    // Grid cell dimensions
    private final float cellSize;

    // Map of grid cells to objects
    private final Map<Integer, Set<Collider>> grid = new HashMap<>();

    // Map of objects to grid cells they occupy
    private final Map<Collider, Set<Integer>> objectCells = new HashMap<>();

    /**
     * Create a new spatial grid with the specified cell size
     */
    public SpatialGrid(float cellSize) {
        this.cellSize = Math.max(1.0f, cellSize);
    }

    /**
     * Clear the grid
     */
    public void clear() {
        grid.clear();
        objectCells.clear();
    }

    /**
     * Add an object to the grid
     */
    public void addObject(Collider object) {
        if (object == null) return;

        // Get the cells that the object overlaps
        Set<Integer> cells = getCellsForObject(object);

        // Add the object to each cell
        for (Integer cellId : cells) {
            grid.computeIfAbsent(cellId, k -> new HashSet<>()).add(object);
        }

        // Remember which cells this object is in
        objectCells.put(object, cells);
    }

    /**
     * Remove an object from the grid
     */
    public void removeObject(Collider object) {
        if (object == null) return;

        // Get the cells that the object is in
        Set<Integer> cells = objectCells.get(object);

        if (cells != null) {
            // Remove the object from each cell
            for (Integer cellId : cells) {
                Set<Collider> cellObjects = grid.get(cellId);
                if (cellObjects != null) {
                    cellObjects.remove(object);

                    // Remove the cell if it's empty
                    if (cellObjects.isEmpty()) {
                        grid.remove(cellId);
                    }
                }
            }

            // Remove the object's cell mappings
            objectCells.remove(object);
        }
    }

    /**
     * Get all potential collision candidates for an object
     */
    public List<Collider> getPotentialCollisions(Collider object) {
        if (object == null) return Collections.emptyList();

        Set<Collider> potentialCollisions = new HashSet<>();

        // Get the cells that the object overlaps
        Set<Integer> cells = getCellsForObject(object);

        // Get all objects in those cells
        for (Integer cellId : cells) {
            Set<Collider> cellObjects = grid.get(cellId);

            if (cellObjects != null) {
                potentialCollisions.addAll(cellObjects);
            }
        }

        // Remove the object itself
        potentialCollisions.remove(object);

        return new ArrayList<>(potentialCollisions);
    }

    /**
     * Get all objects in the specified area
     */
    public List<Collider> queryArea(float minX, float minY, float maxX, float maxY) {
        Set<Collider> result = new HashSet<>();

        // Convert world coordinates to cell coordinates
        int startX = worldToCell(minX);
        int startY = worldToCell(minY);
        int endX = worldToCell(maxX);
        int endY = worldToCell(maxY);

        // Query each cell in the area
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                int cellId = hashCell(x, y);
                Set<Collider> cellObjects = grid.get(cellId);

                if (cellObjects != null) {
                    result.addAll(cellObjects);
                }
            }
        }

        return new ArrayList<>(result);
    }

    /**
     * Get the set of cell IDs that an object overlaps
     */
    private Set<Integer> getCellsForObject(Collider object) {
        Set<Integer> cells = new HashSet<>();

        // Get the bounding box of the object
        Vector2f min = object.getMin();
        Vector2f max = object.getMax();

        // Convert world coordinates to cell coordinates
        int startX = worldToCell(min.x);
        int startY = worldToCell(min.y);
        int endX = worldToCell(max.x);
        int endY = worldToCell(max.y);

        // Get all cells that the object overlaps
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                cells.add(hashCell(x, y));
            }
        }

        return cells;
    }

    /**
     * Convert a world coordinate to a cell coordinate
     */
    private int worldToCell(float coord) {
        return (int) Math.floor(coord / cellSize);
    }

    /**
     * Hash a cell coordinate pair into a single integer
     */
    private int hashCell(int x, int y) {
        return x * 73856093 ^ y * 19349663; // Hash function to distribute cells
    }
}
