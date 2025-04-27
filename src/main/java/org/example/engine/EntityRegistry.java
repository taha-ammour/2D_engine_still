package org.example.engine;

import java.util.HashMap;
import java.util.Map;


/**
 * Registers entity sprites and assigns a default palette to each group.
 * Also supports per-sprite palette overrides.
 */
public class EntityRegistry {

    // Holds palette overrides for individual sprites by name.
    // If a sprite's name is in this map, we use the override instead of the group palette.
    private static final Map<String, String[]> overridePalettes = new HashMap<>();

    /**
     * Call this before registerEntities if you want a single sprite to have a custom palette
     * that differs from the group's default.
     * Example:
     * EntityRegistry.setOverridePalette("player_sprite_d", new String[]{"300","400","500","555"});
     */
    public static void setOverridePalette(String spriteName, String[] customPalette) {
        overridePalettes.put(spriteName.toLowerCase(), customPalette);
    }


    public static void registerEntities(SpriteManager spriteManager) {
        // Load the sprite sheet named "entities".
        spriteManager.loadSpriteSheet("font", "/textures/font.png");
        spriteManager.loadSpriteSheet("entities", "/textures/entities.png");

        // Player sprites (IDs 1-4)
        spriteManager.defineSprite(1, "player_sprite_d", "entities",
                0, 240, 16, 16, getPaletteForGroup("player_sprite_d"));
        spriteManager.defineSprite(2, "player_sprite_u", "entities",
                16, 240, 16, 16, getPaletteForGroup("player_sprite_u"));
        spriteManager.defineSprite(3, "player_sprite_r", "entities",
                32, 240, 16, 16, getPaletteForGroup("player_sprite_r"));
        spriteManager.defineSprite(4, "player_sprite_rr", "entities",
                48, 240, 16, 16, getPaletteForGroup("player_sprite_rr"));

        // Enemy demon sprites (IDs 5-8)
        spriteManager.defineSprite(5, "enemy_demon_sprite_d", "entities",
                0, 224, 16, 16, getPaletteForGroup("enemy_demon_sprite_d"));
        spriteManager.defineSprite(6, "enemy_demon_sprite_u", "entities",
                16, 224, 16, 16, getPaletteForGroup("enemy_demon_sprite_u"));
        spriteManager.defineSprite(7, "enemy_demon_sprite_r", "entities",
                32, 224, 16, 16, getPaletteForGroup("enemy_demon_sprite_r"));
        spriteManager.defineSprite(8, "enemy_demon_sprite_rr", "entities",
                48, 224, 16, 16, getPaletteForGroup("enemy_demon_sprite_rr"));

        // Enemy ghostsk sprites (IDs 9-11)
        spriteManager.defineSprite(9, "enemy_ghostsk_sprite_d", "entities",
                0, 208, 16, 16, getPaletteForGroup("enemy_ghostsk_sprite_d"));
        spriteManager.defineSprite(10, "enemy_ghostsk_sprite_u", "entities",
                16, 208, 16, 16, getPaletteForGroup("enemy_ghostsk_sprite_u"));
        spriteManager.defineSprite(11, "enemy_ghostsk_sprite_r", "entities",
                32, 208, 16, 16, getPaletteForGroup("enemy_ghostsk_sprite_r"));

        // Enemy ghost sprites (IDs 12-15)
        spriteManager.defineSprite(12, "enemy_ghost_sprite_d", "entities",
                0, 192, 16, 16, getPaletteForGroup("enemy_ghost_sprite_d"));
        spriteManager.defineSprite(13, "enemy_ghost_sprite_u", "entities",
                16, 192, 16, 16, getPaletteForGroup("enemy_ghost_sprite_u"));
        spriteManager.defineSprite(14, "enemy_ghost_sprite_r", "entities",
                32, 192, 16, 16, getPaletteForGroup("enemy_ghost_sprite_r"));
        spriteManager.defineSprite(15, "enemy_ghost_sprite_rr", "entities",
                48, 192, 16, 16, getPaletteForGroup("enemy_ghost_sprite_rr"));

        // Enemy orc sprites (IDs 16-19)
        spriteManager.defineSprite(16, "enemy_orc_sprite_d", "entities",
                0, 176, 16, 16, getPaletteForGroup("enemy_orc_sprite_d"));
        spriteManager.defineSprite(17, "enemy_orc_sprite_u", "entities",
                16, 176, 16, 16, getPaletteForGroup("enemy_orc_sprite_u"));
        spriteManager.defineSprite(18, "enemy_orc_sprite_r", "entities",
                32, 176, 16, 16, getPaletteForGroup("enemy_orc_sprite_r"));
        spriteManager.defineSprite(19, "enemy_orc_sprite_rr", "entities",
                48, 176, 16, 16, getPaletteForGroup("enemy_orc_sprite_rr"));

        // Enemy small orc (IDs 20-23)
        spriteManager.defineSprite(20, "enemy_smallorc_sprite_d", "entities",
                0, 160, 16, 16, getPaletteForGroup("enemy_smallorc_sprite_d"));
        spriteManager.defineSprite(21, "enemy_smallorc_sprite_u", "entities",
                16, 160, 16, 16, getPaletteForGroup("enemy_smallorc_sprite_u"));
        spriteManager.defineSprite(22, "enemy_smallorc_sprite_r", "entities",
                32, 160, 16, 16, getPaletteForGroup("enemy_smallorc_sprite_r"));
        spriteManager.defineSprite(23, "enemy_smallorc_sprite_rr", "entities",
                48, 160, 16, 16, getPaletteForGroup("enemy_smallorc_sprite_rr"));

        // Enemy horndem (IDs 24-27)
        spriteManager.defineSprite(24, "enemy_horndem_sprite_d", "entities",
                0, 144, 16, 16, getPaletteForGroup("enemy_horndem_sprite_d"));
        spriteManager.defineSprite(25, "enemy_horndem_sprite_u", "entities",
                16, 144, 16, 16, getPaletteForGroup("enemy_horndem_sprite_u"));
        spriteManager.defineSprite(26, "enemy_horndem_sprite_r", "entities",
                32, 144, 16, 16, getPaletteForGroup("enemy_horndem_sprite_r"));
        spriteManager.defineSprite(27, "enemy_horndem_sprite_rr", "entities",
                48, 144, 16, 16, getPaletteForGroup("enemy_horndem_sprite_rr"));

        // Enemy slime (IDs 28-31)
        spriteManager.defineSprite(28, "enemy_slime_sprite_d", "entities",
                0, 136, 16, 8, getPaletteForGroup("enemy_slime_sprite_d"));
        spriteManager.defineSprite(29, "enemy_slime_sprite_u", "entities",
                16, 136, 16, 8, getPaletteForGroup("enemy_slime_sprite_u"));
        spriteManager.defineSprite(30, "enemy_slime_sprite_r", "entities",
                32, 136, 16, 8, getPaletteForGroup("enemy_slime_sprite_r"));
        spriteManager.defineSprite(31, "enemy_slime_sprite_rr", "entities",
                48, 136, 16, 8, getPaletteForGroup("enemy_slime_sprite_rr"));

        // Dead player sprite (ID 32)
        spriteManager.defineSprite(32, "dead_p_sprite", "entities",
                0, 120, 16, 16, getPaletteForGroup("dead_p_sprite"));

        // Spikes (IDs 33-35)
        spriteManager.defineSprite(33, "spike_sprite_0", "entities",
                0, 104, 16, 16, getPaletteForGroup("spike_sprite_0"));
        spriteManager.defineSprite(34, "spike_sprite_1", "entities",
                16, 104, 16, 16, getPaletteForGroup("spike_sprite_1"));
        spriteManager.defineSprite(35, "spike_sprite_2", "entities",
                32, 104, 16, 16, getPaletteForGroup("spike_sprite_2"));

        // Lamp posts (IDs 36-37)
        spriteManager.defineSprite(36, "lamp_post_0", "entities",
                0, 88, 16, 16, getPaletteForGroup("lamp_post_0"));
        spriteManager.defineSprite(37, "lamp_post_1", "entities",
                16, 88, 16, 16, getPaletteForGroup("lamp_post_1"));

        // Leg versions 1 (IDs 38-41)
        spriteManager.defineSprite(38, "leg_l_1", "entities",
                64, 240, 16, 16, getPaletteForGroup("leg_l_1"));
        spriteManager.defineSprite(39, "leg_r_1", "entities",
                80, 240, 16, 16, getPaletteForGroup("leg_r_1"));
        spriteManager.defineSprite(40, "leg_d_1", "entities",
                96, 240, 16, 16, getPaletteForGroup("leg_d_1"));
        spriteManager.defineSprite(41, "leg_idle_1", "entities",
                112, 240, 16, 16, getPaletteForGroup("leg_idle_1"));

        // Leg versions 2 (IDs 42-45)
        spriteManager.defineSprite(42, "leg_l_2", "entities",
                64, 224, 16, 16, getPaletteForGroup("leg_l_2"));
        spriteManager.defineSprite(43, "leg_r_2", "entities",
                80, 224, 16, 16, getPaletteForGroup("leg_r_2"));
        spriteManager.defineSprite(44, "leg_d_2", "entities",
                96, 224, 16, 16, getPaletteForGroup("leg_d_2"));
        spriteManager.defineSprite(45, "leg_idle_2", "entities",
                112, 224, 16, 16, getPaletteForGroup("leg_idle_2"));

        // Body versions 1 (IDs 46-49)
        spriteManager.defineSprite(46, "body_l_1", "entities",
                64, 208, 16, 16, getPaletteForGroup("body_l_1"));
        spriteManager.defineSprite(47, "body_r_1", "entities",
                80, 208, 16, 16, getPaletteForGroup("body_r_1"));
        spriteManager.defineSprite(48, "body_d_1", "entities",
                96, 208, 16, 16, getPaletteForGroup("body_d_1"));
        spriteManager.defineSprite(49, "body_idle_1", "entities",
                112, 208, 16, 16, getPaletteForGroup("body_idle_1"));

        // Hat versions 1 (IDs 50-53)
        spriteManager.defineSprite(50, "hat_l_1", "entities",
                64, 192, 16, 16, getPaletteForGroup("hat_l_1"));
        spriteManager.defineSprite(51, "hat_r_1", "entities",
                80, 192, 16, 16, getPaletteForGroup("hat_r_1"));
        spriteManager.defineSprite(52, "hat_d_1", "entities",
                96, 192, 16, 16, getPaletteForGroup("hat_d_1"));
        spriteManager.defineSprite(53, "hat_idle_1", "entities",
                112, 192, 16, 16, getPaletteForGroup("hat_idle_1"));

        // Hat versions 2 (IDs 54-57)
        spriteManager.defineSprite(54, "hat_l_2", "entities",
                64, 176, 16, 16, getPaletteForGroup("hat_l_2"));
        spriteManager.defineSprite(55, "hat_r_2", "entities",
                80, 176, 16, 16, getPaletteForGroup("hat_r_2"));
        spriteManager.defineSprite(56, "hat_d_2", "entities",
                96, 176, 16, 16, getPaletteForGroup("hat_d_2"));
        spriteManager.defineSprite(57, "hat_idle_2", "entities",
                112, 176, 16, 16, getPaletteForGroup("hat_idle_2"));

        // Body versions 2 (IDs 58-61)
        spriteManager.defineSprite(58, "body_l_2", "entities",
                64, 160, 16, 16, getPaletteForGroup("body_l_2"));
        spriteManager.defineSprite(59, "body_r_2", "entities",
                80, 160, 16, 16, getPaletteForGroup("body_r_2"));
        spriteManager.defineSprite(60, "body_d_2", "entities",
                96, 160, 16, 16, getPaletteForGroup("body_d_2"));
        spriteManager.defineSprite(61, "body_idle_2", "entities",
                112, 160, 16, 16, getPaletteForGroup("body_idle_2"));

        // Hat versions 3 (IDs 62-65)
        spriteManager.defineSprite(62, "hat_l_3", "entities",
                64, 144, 16, 16, getPaletteForGroup("hat_l_3"));
        spriteManager.defineSprite(63, "hat_r_3", "entities",
                80, 144, 16, 16, getPaletteForGroup("hat_r_3"));
        spriteManager.defineSprite(64, "hat_d_3", "entities",
                96, 144, 16, 16, getPaletteForGroup("hat_d_3"));
        spriteManager.defineSprite(65, "hat_idle_3", "entities",
                112, 144, 16, 16, getPaletteForGroup("hat_idle_3"));

        // ChestE (IDs 66-68)
        spriteManager.defineSprite(66, "ChestE_id_1", "entities",
                128, 240, 16, 16, getPaletteForGroup("ChestE_id_1"));
        spriteManager.defineSprite(67, "ChestE_id_2", "entities",
                144, 240, 16, 16, getPaletteForGroup("ChestE_id_2"));
        spriteManager.defineSprite(68, "ChestE_id_3", "entities",
                160, 240, 16, 16, getPaletteForGroup("ChestE_id_3"));

        // Emerald (IDs 69-71)
        spriteManager.defineSprite(69, "Emerald_id_1", "entities",
                128, 224, 16, 16, getPaletteForGroup("Emerald_id_1"));
        spriteManager.defineSprite(70, "Emerald_id_2", "entities",
                144, 224, 16, 16, getPaletteForGroup("Emerald_id_2"));
        spriteManager.defineSprite(71, "Emerald_id_3", "entities",
                160, 224, 16, 16, getPaletteForGroup("Emerald_id_3"));

        // ChestH (IDs 72-74)
        spriteManager.defineSprite(72, "ChestH_id_1", "entities",
                176, 240, 16, 16, getPaletteForGroup("ChestH_id_1"));
        spriteManager.defineSprite(73, "ChestH_id_2", "entities",
                192, 240, 16, 16, getPaletteForGroup("ChestH_id_2"));
        spriteManager.defineSprite(74, "ChestH_id_3", "entities",
                208, 240, 16, 16, getPaletteForGroup("ChestH_id_3"));
    }

    public static void registerUi(SpriteManager spriteManager){
        spriteManager.loadSpriteSheet("ui", "/textures/ui.png");

        spriteManager.defineSprite(169, "health_1", "ui",
                0, 120, 8, 8, new String[]{"100","300","510","401"});
        spriteManager.defineSprite(170, "energy_1", "ui",
                8, 120, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(171, "armor_1", "ui",
                16, 120, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(172, "wand_1", "ui",
                24, 120, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(173, "book_1", "ui",
                32, 120, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(174, "backpack_1", "ui",
                40, 120, 8, 8, new String[]{"510","300","210","421"});

        spriteManager.defineSprite(175, "powerup_gu_1", "ui",
                48, 120, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(176, "arrow_down_1", "ui",
                56, 120, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(177, "arrow_up_1", "ui",
                64, 120, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(178, "open_inv_1", "ui",
                72, 120, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(179, "key_1", "ui",
                80, 120, 8, 8, new String[]{"510","300","210","421"});

        spriteManager.defineSprite(180, "progressbar_upward_start", "ui",
                96, 120, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(181, "progressbar_upward_middle", "ui",
                96, 112, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(182, "progressbar_upward_end", "ui",
                96, 104, 8, 8, new String[]{"510","300","210","421"});

        spriteManager.defineSprite(183, "progressbar_downward_start", "ui",
                104, 120, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(184, "progressbar_downward_middle", "ui",
                112, 120, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(185, "progressbar_downward_end", "ui",
                120, 120, 8, 8, new String[]{"510","300","210","421"});

        spriteManager.defineSprite(186, "smile_1", "ui",
                0, 112, 8, 8, new String[]{"100","300","510","401"});
        spriteManager.defineSprite(187, "ring_1", "ui",
                8, 112, 8, 8, new String[]{"100","300","510","401"});
        spriteManager.defineSprite(188, "swordic_1", "ui",
                16, 112, 8, 8, new String[]{"100","300","510","401"});
        spriteManager.defineSprite(189, "wand_2", "ui",
                24, 112, 8, 8, new String[]{"100","300","510","401"});
        spriteManager.defineSprite(190, "helmetic_1", "ui",
                32, 112, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(191, "chestplateic_1", "ui",
                40, 112, 8, 8, new String[]{"510","300","210","421"});

        spriteManager.defineSprite(192, "leggingic_1", "ui",
                48, 112, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(193, "bootsic_1", "ui",
                56, 112, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(194, "backpackic_1", "ui",
                64, 112, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(195, "hatic_1", "ui",
                72, 112, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(196, "miscic_1", "ui",
                80, 112, 8, 8, new String[]{"510","300","210","421"});

        spriteManager.defineSprite(197, "inv_slot_1", "ui",
                0, 96, 16, 16, new String[]{"510","300","210","421"});

        spriteManager.defineSprite(198, "inv_slot_eq_1", "ui",
                16, 96, 16, 16, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(199, "button_down_1", "ui",
                32, 96, 16, 16, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(200, "button_up_1", "ui",
                48, 96, 16, 16, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(201, "inv_slot_2", "ui",
                64, 96, 16, 16, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(202, "small_button_up_2", "ui",
                80, 104, 8, 8, new String[]{"510","300","210","421"});

        spriteManager.defineSprite(203, "zeroic_1", "ui",
                0, 88, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(204, "oneic_1", "ui",
                8, 88, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(205, "twoic_1", "ui",
                16, 88, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(206, "threeic_1", "ui",
                24, 88, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(207, "fooric_1", "ui",
                32, 88, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(208, "fiveic_1", "ui",
                40, 88, 8, 8, new String[]{"510","300","210","421"});


        spriteManager.defineSprite(209, "Xic_1", "ui",
                48, 88, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(210, "Bic_1", "ui",
                56, 88, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(211, "Mic_1", "ui",
                64, 88, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(212, "Cic_1", "ui",
                72, 88, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(213, "Fic_1", "ui",
                80, 88, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(214, "Eic_1", "ui",
                88, 88, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(215, "Gic_1", "ui",
                96, 88, 8, 8, new String[]{"510","300","210","421"});

        spriteManager.defineSprite(216, "Cube_Black_ic_1", "ui",
                0, 80, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(217, "Question_ic_1", "ui",
                8, 80, 8, 8, new String[]{"510","300","210","421"});

        spriteManager.defineSprite(218, "flame_spell_fil_ic_1", "ui",
                0, 72, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(219, "flame_spell_ic_1", "ui",
                8, 72, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(220, "flame_particle_ic_1", "ui",
                16, 72, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(221, "spread_spell_ic_1", "ui",
                24, 72, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(222, "type_spell_sp_ic_1", "ui",
                32, 72, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(223, "type_spell_fl_ic_1", "ui",
                40, 72, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(224, "type_spell_pa_ic_1", "ui",
                48, 72, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(225, "type_spell_qt_ic_1", "ui",
                56, 72, 8, 8, new String[]{"510","300","210","421"});

        spriteManager.defineSprite(226, "coin_pickup_1", "ui",
                0, 64, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(227, "Question_ic_2", "ui",
                8, 64, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(228, "flame_spell_fil_minus_ic_1", "ui",
                16, 64, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(229, "flame_spell_minus_ic_1", "ui",
                24, 64, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(230, "lock_opened_ic_1", "ui",
                32, 64, 8, 8, new String[]{"510","300","210","421"});

        spriteManager.defineSprite(231, "plus_ic_1", "ui",
                0, 56, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(232, "minus_ic_2", "ui",
                8, 56, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(233, "speed_inc_1", "ui",
                16, 56, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(234, "speed_dec_1", "ui",
                24, 56, 8, 8, new String[]{"510","300","210","421"});
        spriteManager.defineSprite(235, "type_spell_square_ic_1", "ui",
                32, 56, 8, 8, new String[]{"510","300","210","421"});


    }
    public static void registerTiles(SpriteManager spriteManager){
        spriteManager.loadSpriteSheet("tiles", "/textures/tiles.png");

        spriteManager.defineSprite(75, "tile_corner_ld_1", "tiles",
                0, 240, 16, 16, new String[]{"231","300","231","100"});
        spriteManager.defineSprite(76, "tile_down_1", "tiles",
                16, 240, 16, 16, new String[]{"231","300","231","100"});
        spriteManager.defineSprite(77, "tile_corner_rd_1", "tiles",
                32, 240, 16, 16, new String[]{"231","300","231","100"});

        spriteManager.defineSprite(78, "tile_up_1", "tiles",
                48, 240, 16, 16, new String[]{"001","224","208","001"});
        spriteManager.defineSprite(79, "tile_up_2", "tiles",
                64, 240, 16, 16, new String[]{"001","112","145","224"});

        spriteManager.defineSprite(80, "shadow_door_1", "tiles",
                80, 248, 16, 8, new String[]{"001","112","145","005"});
        spriteManager.defineSprite(81, "shadow_door_open", "tiles",
                96, 248, 8, 8, new String[]{"001","112","145","005"});

        spriteManager.defineSprite(82, "tile_l_1", "tiles",
                0, 224, 16, 16, new String[]{"231","300","231","100"});
        spriteManager.defineSprite(83, "tile_w_1", "tiles",
                16, 224, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(84, "tile_r_1", "tiles",
                32, 224, 16, 16, new String[]{"001","215","014","105"});

        spriteManager.defineSprite(85, "tile_l_2", "tiles",
                0, 208, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(86, "tile_up_3u", "tiles",
                16, 208, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(87, "tile_r_2", "tiles",
                32, 208, 16, 16, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(88, "tile_liquid", "tiles",
                48, 208, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(89, "tile_dec_1", "tiles",
                48, 224, 16, 16, new String[]{"001","215","014","105"});
        spriteManager.defineSprite(90, "tile_dec_2", "tiles",
                64, 224, 16, 16, new String[]{"001","215","014","105"});

        spriteManager.defineSprite(91, "door_closed_1", "tiles",
                80, 232, 16, 16, new String[]{"001","112","145","005"});
        spriteManager.defineSprite(92, "door_open_1", "tiles",
                96, 232, 16, 16, new String[]{"001","112","145","005"});
        spriteManager.defineSprite(93, "door_closed_boss_1", "tiles",
                80, 216, 16, 16, new String[]{"001","112","145","005"});

        spriteManager.defineSprite(94, "stairs_1", "tiles",
                96, 208, 16, 16, new String[]{"001","112","145","005"});
        spriteManager.defineSprite(95, "tile_dec_3", "tiles",
                80, 208, 16, 8, new String[]{"001","215","014","105"});
        spriteManager.defineSprite(96, "tile_dec_4", "tiles",
                64, 208, 16, 8, new String[]{"001","215","014","105"});
//Corners
        spriteManager.defineSprite(97, "tile_sp_ld_1", "tiles",
                0, 200, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(98, "tile_sp_d_1", "tiles",
                8, 200, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(99, "tile_sp_rd_1", "tiles",
                16, 200, 8, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(100, "tile_sp_l_1", "tiles",
                0, 192, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(101, "tile_sp_w_1", "tiles",
                8, 192, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(102, "tile_sp_r_1", "tiles",
                16, 192, 8, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(103, "tile_sp_ul_1", "tiles",
                0, 184, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(104, "tile_sp_u_1", "tiles",
                8, 184, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(105, "tile_sp_ur_1", "tiles",
                16, 184, 8, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(106, "tile_sp_ld_2", "tiles",
                24, 200, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(107, "tile_sp_d_2", "tiles",
                32, 200, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(108, "tile_sp_rd_2", "tiles",
                40, 200, 8, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(109, "tile_sp_l_2", "tiles",
                24, 192, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(110, "tile_sp_w_2", "tiles",
                32, 192, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(111, "tile_sp_r_2", "tiles",
                40, 192, 8, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(112, "tile_sp_ul_2", "tiles",
                24, 184, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(113, "tile_sp_u_2", "tiles",
                32, 184, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(114, "tile_sp_ur_2", "tiles",
                40, 184, 8, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(115, "tile_walkable_1", "tiles",
                48, 192, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(116, "tile_walkable_2", "tiles",
                64, 192, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(117, "tile_walkable_3", "tiles",
                80, 192, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(118, "tile_walkable_4", "tiles",
                96, 192, 16, 16, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(119, "tile_walkable_5", "tiles",
                48, 176, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(120, "tile_walkable_6", "tiles",
                64, 176, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(121, "tile_walkable_7", "tiles",
                80, 176, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(122, "tile_walkable_8", "tiles",
                96, 176, 16, 16, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(123, "tile_up_4uu", "tiles",
                0, 160, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(124, "tile_up_5uu", "tiles",
                16, 160, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(125, "tile_up_6uu", "tiles",
                32, 160, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(126, "tile_up_7uu", "tiles",
                48, 160, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(127, "tile_banner", "tiles",
                64, 160, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(128, "tile_lamp_post", "tiles",
                80, 160, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(129, "tile_dec_5", "tiles",
                96, 160, 16, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(130, "tile_dec_6", "tiles",
                112, 160, 16, 16, new String[]{"005","103","112","224"});
        spriteManager.defineSprite(131, "tile_dec_7", "tiles",
                128, 160, 8, 16, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(132, "tile_dec_8", "tiles",
                136, 160, 8, 16, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(133, "tile_light_1", "tiles",
                64, 152, 8, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(134, "tile_candle_1", "tiles",
                64, 144, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(135, "tile_light_2", "tiles",
                72, 152, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(136, "tile_light_3", "tiles",
                80, 152, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(137, "tile_light_4", "tiles",
                88, 152, 8, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(138, "spell_4p_1", "tiles",
                0, 64, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(139, "spell_egg_1", "tiles",
                8, 64, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(140, "spell_question?_1", "tiles",
                16, 64, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(141, "spell_egg_2", "tiles",
                24, 64, 8, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(142, "sm_money_1", "tiles",
                0, 56, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(143, "sm_money_2", "tiles",
                8, 56, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(144, "sm_money_3", "tiles",
                16, 56, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(145, "sm_money_4", "tiles",
                24, 56, 8, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(146, "money_1", "tiles",
                0, 48, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(147, "money_2", "tiles",
                8, 48, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(148, "money_3", "tiles",
                16, 48, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(149, "money_4", "tiles",
                24, 48, 8, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(150, "spell_castpartpoint_1", "tiles",
                0, 40, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(151, "spell_castpartpoint_2", "tiles",
                8, 40, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(152, "spell_castpartpoint_3", "tiles",
                16, 40, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(153, "spell_castpartpoint_4", "tiles",
                24, 40, 8, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(154, "spell_cast_sm_fire_1", "tiles",
                32, 40, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(155, "spell_cast_sm_fire_2", "tiles",
                40, 40, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(156, "spell_cast_sm_fire_3", "tiles",
                48, 40, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(157, "spell_cast_sm_fire_4", "tiles",
                56, 40, 8, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(158, "spell_cast_fire_1", "tiles",
                0, 32, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(159, "spell_cast_fire_2", "tiles",
                8, 32, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(160, "spell_cast_fire_3", "tiles",
                16, 32, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(161, "spell_cast_fire_4", "tiles",
                24, 32, 8, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(162, "spell_cast_dagger_1", "tiles",
                32, 32, 8, 8, new String[]{"001","103","112","224"});


        spriteManager.defineSprite(163, "walking_shadow_3", "tiles",
                0, 24, 16, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(164, "walking_shadow_1", "tiles",
                0, 16, 16, 8, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(165, "box_1", "tiles",
                16, 16, 16, 16, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(166, "walking_shadow_2", "tiles",
                32, 16, 8, 8, new String[]{"001","103","112","224"});
        spriteManager.defineSprite(167, "spell_effect_1", "tiles",
                40, 16, 16, 16, new String[]{"001","103","112","224"});

        spriteManager.defineSprite(168, "tile_spawn_1u", "tiles",
                32, 136, 16, 24, new String[]{"001","103","112","224"});


    }

    /**
     * Returns a palette code array for the given sprite name, unless there's an override.
     * Each string in the array is 3 digits in [0..5] => "RGB" using a 6-level color system.
     */
    public static String[] getPaletteForGroup(String spriteName) {
        // If there's a per-sprite override, use that.
        String lower = spriteName.toLowerCase();
        if (overridePalettes.containsKey(lower)) {
            return overridePalettes.get(lower);
        }

        // Otherwise, use default group-based logic:
        if (lower.startsWith("player_")) {
            // Reddish gradient
            return new String[]{"000", "000", "432", "321"};
        } else if (lower.startsWith("enemy_demon_")) {
            // Purple gradient
            return new String[]{"203", "304", "405", "555"};
        } else if (lower.startsWith("enemy_ghostsk_")) {
            // Dark gray to lighter
            return new String[]{"111", "222", "333", "555"};
        } else if (lower.startsWith("enemy_ghost_")) {
            // Medium gray to white
            return new String[]{"222", "333", "444", "555"};
        } else if (lower.startsWith("enemy_orc_")) {
            // Green gradient
            return new String[]{"050", "150", "250", "555"};
        } else if (lower.startsWith("enemy_smallorc_")) {
            // Slightly different green gradient
            return new String[]{"040", "140", "240", "555"};
        } else if (lower.startsWith("enemy_horndem_")) {
            // Reddish-brown gradient
            return new String[]{"501", "410", "320", "555"};
        } else if (lower.startsWith("enemy_slime_")) {
            // Bright green
            return new String[]{"010", "110", "210", "555"};
        } else if (lower.startsWith("dead_p_")) {
            // Black to mid-gray
            return new String[]{"000", "111", "222", "555"};
        } else if (lower.startsWith("spike_")) {
            // Gray
            return new String[]{"000", "222", "444", "555"};
        } else if (lower.startsWith("lamp_post_")) {
            // Brownish
            return new String[]{"210", "310", "421", "555"};
        } else if (lower.startsWith("leg_") || lower.startsWith("body_")) {
            // Flesh tone
            return new String[]{"210", "320", "531", "555"};
        } else if (lower.startsWith("hat_")) {
            // Lightish color
            return new String[]{"501", "420", "310", "555"};
        } else if (lower.startsWith("cheste_")) {
            // Grayish
            return new String[]{"432", "541", "520", "555"};
        } else if (lower.startsWith("emerald_")) {
            // Bright green gem
            return new String[]{"050", "150", "250", "555"};
        } else if (lower.startsWith("chesth_")) {
            // Golden-ish
            return new String[]{"540", "530", "520", "555"};
        }

        // Default palette if no group matches
        return new String[]{"100", "250", "000", "555"};
    }
}