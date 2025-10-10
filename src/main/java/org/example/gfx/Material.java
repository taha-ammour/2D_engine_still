// src/main/java/org/example/gfx/Material.java
package org.example.gfx;

import org.joml.Vector4f;

/**
 * Material encapsulates rendering properties
 * Makes it easier to share rendering settings between sprites
 */
public final class Material {
    private String shaderName = "sprite";
    private Vector4f tint = new Vector4f(1, 1, 1, 1);
    private int effectFlags = 0;
    private Texture texture;

    public Material() {}

    public Material(Texture texture) {
        this.texture = texture;
    }

    public Material(String shaderName, Vector4f tint) {
        this.shaderName = shaderName;
        this.tint = new Vector4f(tint);
    }

    // Builder pattern for fluent API
    public static class Builder {
        private final Material material = new Material();

        public Builder shader(String name) {
            material.shaderName = name;
            return this;
        }

        public Builder texture(Texture texture) {
            material.texture = texture;
            return this;
        }

        public Builder tint(float r, float g, float b, float a) {
            material.tint.set(r, g, b, a);
            return this;
        }

        public Builder tint(Vector4f tint) {
            material.tint.set(tint);
            return this;
        }

        public Builder effects(ShaderEffect... effects) {
            material.effectFlags = ShaderEffect.combine(effects);
            return this;
        }

        public Builder addEffect(ShaderEffect effect) {
            material.effectFlags |= effect.flag;
            return this;
        }

        public Material build() {
            return material;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and setters
    public String getShaderName() { return shaderName; }
    public void setShaderName(String name) { this.shaderName = name; }

    public Vector4f getTint() { return tint; }
    public void setTint(Vector4f tint) { this.tint.set(tint); }
    public void setTint(float r, float g, float b, float a) { this.tint.set(r, g, b, a); }

    public int getEffectFlags() { return effectFlags; }
    public void setEffectFlags(int flags) { this.effectFlags = flags; }

    public Texture getTexture() { return texture; }
    public void setTexture(Texture texture) { this.texture = texture; }
}

// ====================================

// USAGE EXAMPLES:

/*
// Example 1: Create a material with effects
Material invincibleMaterial = Material.builder()
    .shader("effects")
    .texture(marioTexture)
    .tint(1, 1, 1, 1)
    .effects(ShaderEffect.FLASH, ShaderEffect.OUTLINE)
    .build();

// Example 2: Create a damaged enemy material
Material damagedMaterial = Material.builder()
    .shader("effects")
    .tint(1, 0.5f, 0.5f, 1)
    .addEffect(ShaderEffect.PULSATE)
    .build();

// Example 3: Ghost effect
Material ghostMaterial = Material.builder()
    .shader("effects")
    .tint(1, 1, 1, 0.5f)
    .effects(ShaderEffect.WOBBLE, ShaderEffect.DISSOLVE)
    .build();

// Example 4: Using in SpriteRenderer
SpriteRenderer sprite = new SpriteRenderer(renderer, shaderManager);
sprite.setMaterial(invincibleMaterial);

// Example 5: Temporarily add effect
if (mario.isInvincible()) {
    sprite.getMaterial().setEffectFlags(
        ShaderEffect.combine(ShaderEffect.FLASH, ShaderEffect.OUTLINE)
    );
} else {
    sprite.getMaterial().setEffectFlags(ShaderEffect.NONE.flag);
}
*/