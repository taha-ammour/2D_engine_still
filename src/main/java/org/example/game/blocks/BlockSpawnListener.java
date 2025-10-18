package org.example.game.blocks;

import org.example.ecs.GameObject;

/**
 * Observer Pattern: Interface for block spawn events
 */
public interface BlockSpawnListener {
    void onBlockSpawned(GameObject block);

    void onBlockDespawned(GameObject block);
}
