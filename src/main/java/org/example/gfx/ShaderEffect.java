// src/main/java/org/example/gfx/ShaderEffect.java
package org.example.gfx;

/**
 * Enum for shader effect flags (bitwise operations)
 */
public enum ShaderEffect {
    NONE(0),
    GRAYSCALE(1),
    PULSATE(2),
    WOBBLE(4),
    OUTLINE(8),
    FLASH(16),
    DISSOLVE(32);

    public final int flag;

    ShaderEffect(int flag) {
        this.flag = flag;
    }

    /**
     * Combine multiple effects
     */
    public static int combine(ShaderEffect... effects) {
        int flags = 0;
        for (ShaderEffect effect : effects) {
            flags |= effect.flag;
        }
        return flags;
    }

    /**
     * Check if effect is active
     */
    public static boolean hasEffect(int flags, ShaderEffect effect) {
        return (flags & effect.flag) != 0;
    }
}