package org.example.game.blocks;

import org.example.game.blocks.effects.BlockEffect;
import org.example.game.blocks.effects.BlockEffects;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry Pattern: Manages all block types
 * Singleton Pattern: Single source of truth
 */
public final class BlockRegistry {
    private static BlockRegistry instance;

    private final Map<String, BlockEffect> effects = new HashMap<>();
    private final Map<String, BlockTemplate> templates = new HashMap<>();

    private BlockRegistry() {
        registerDefaultEffects();
        registerDefaultTemplates();
    }

    public static BlockRegistry getInstance() {
        if (instance == null) {
            instance = new BlockRegistry();
        }
        return instance;
    }

    private void registerDefaultEffects() {
        effects.put("power_up", BlockEffects.POWER_UP);
        effects.put("invincibility", BlockEffects.INVINCIBILITY);
        effects.put("coin", BlockEffects.COIN);
        effects.put("poison", BlockEffects.POISON);
        effects.put("slow", BlockEffects.SLOW);
        effects.put("bounce", BlockEffects.BOUNCE);
        effects.put("teleport", BlockEffects.TELEPORT);
    }

    private void registerDefaultTemplates() {
        templates.put("lucky_coin", new BlockTemplate(
                "lucky", "coin", false, false, null
        ));

        templates.put("lucky_powerup", new BlockTemplate(
                "lucky", "power_up", false, false, null
        ));

        templates.put("super_lucky", new BlockTemplate(
                "lucky", "power_up", true, true, new String[]{"pulsate", "outline"}
        ));

        templates.put("poison", new BlockTemplate(
                "poison", null, false, true, new String[]{"wobble"}
        ));

        templates.put("mystery", new BlockTemplate(
                "mystery", null, false, true, new String[]{"pulsate"}
        ));
    }

    // ===== REGISTRATION =====

    public void registerEffect(String name, BlockEffect effect) {
        effects.put(name, effect);
    }

    public void registerTemplate(String name, BlockTemplate template) {
        templates.put(name, template);
    }

    // ===== RETRIEVAL =====

    public BlockEffect getEffect(String name) {
        return effects.get(name);
    }

    public BlockTemplate getTemplate(String name) {
        return templates.get(name);
    }

    public Collection<String> getAllEffectNames() {
        return effects.keySet();
    }

    public Collection<String> getAllTemplateNames() {
        return templates.keySet();
    }
}
