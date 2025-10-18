package org.example.game.blocks;

import org.example.ecs.GameObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Spawner Pattern: Manages block creation and placement
 * Observer Pattern: Notifies when blocks are spawned/destroyed
 */
public final class BlockSpawner {
    private final BlockFactory factory;
    private final List<GameObject> activeBlocks = new ArrayList<>();
    private final List<BlockSpawnListener> listeners = new ArrayList<>();

    public BlockSpawner(BlockFactory factory) {
        this.factory = factory;
    }

    // ===== SPAWNING =====

    public GameObject spawn(String templateName, float x, float y) {
        BlockTemplate template = BlockRegistry.getInstance().getTemplate(templateName);
        if (template == null) {
            throw new IllegalArgumentException("Unknown template: " + templateName);
        }

        BlockBuilder builder = factory.builder().position(x, y);
        template.applyTo(builder);
        GameObject block = builder.build();

        activeBlocks.add(block);
        notifySpawned(block);

        return block;
    }

    public GameObject spawnCustom(float x, float y,
                                  java.util.function.Consumer<BlockBuilder> configurator) {
        BlockBuilder builder = factory.builder().position(x, y);
        configurator.accept(builder);
        GameObject block = builder.build();

        activeBlocks.add(block);
        notifySpawned(block);

        return block;
    }

    public void despawn(GameObject block) {
        if (activeBlocks.remove(block)) {
            notifyDespawned(block);
        }
    }

    // ===== BATCH SPAWNING =====

    public List<GameObject> spawnGrid(String templateName,
                                      float startX, float startY,
                                      int columns, int rows,
                                      float spacing) {
        List<GameObject> blocks = new ArrayList<>();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                float x = startX + col * spacing;
                float y = startY + row * spacing;
                blocks.add(spawn(templateName, x, y));
            }
        }

        return blocks;
    }

    public List<GameObject> spawnLine(String templateName,
                                      float startX, float startY,
                                      float endX, float endY,
                                      int count) {
        List<GameObject> blocks = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            float t = (float) i / (count - 1);
            float x = startX + (endX - startX) * t;
            float y = startY + (endY - startY) * t;
            blocks.add(spawn(templateName, x, y));
        }

        return blocks;
    }

    // ===== OBSERVER PATTERN =====

    public void addListener(BlockSpawnListener listener) {
        listeners.add(listener);
    }

    public void removeListener(BlockSpawnListener listener) {
        listeners.remove(listener);
    }

    private void notifySpawned(GameObject block) {
        for (BlockSpawnListener listener : listeners) {
            listener.onBlockSpawned(block);
        }
    }

    private void notifyDespawned(GameObject block) {
        for (BlockSpawnListener listener : listeners) {
            listener.onBlockDespawned(block);
        }
    }

    // ===== MANAGEMENT =====

    public List<GameObject> getActiveBlocks() {
        return new ArrayList<>(activeBlocks);
    }

    public void clear() {
        List<GameObject> blocks = new ArrayList<>(activeBlocks);
        for (GameObject block : blocks) {
            despawn(block);
        }
    }
}
