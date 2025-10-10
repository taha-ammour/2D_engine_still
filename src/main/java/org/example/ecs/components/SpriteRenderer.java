// src/main/java/org/example/ecs/components/SpriteRenderer.java
package org.example.ecs.components;

import org.example.ecs.Component;
import org.example.ecs.Renderable;
import org.example.gfx.*;
import org.joml.Vector4f;

/**
 * Rendering Component: Draws sprites
 * Dependency Inversion: Depends on Renderer2D abstraction
 */
public final class SpriteRenderer extends Component implements Renderable {
    private final Renderer2D renderer;
    private Material material;
    private TextureAtlas atlas;

    public float width = 32;
    public float height = 48;
    public int frame = 0;

    public SpriteRenderer(Renderer2D renderer) {
        this.renderer = renderer;
        // Default material with WHITE tint (not black!)
        this.material = Material.builder()
                .shader("sprite")
                .tint(1, 1, 1, 1)  // WHITE - very important!
                .build();
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public void setTexture(Texture texture) {
        material.setTexture(texture);
        this.atlas = null;
    }

    public void setAtlas(TextureAtlas atlas) {
        this.atlas = atlas;
        material.setTexture(atlas.texture());
    }

    /**
     * Set tint color (shorthand for material.setTint)
     */
    public void setTint(float r, float g, float b, float a) {
        material.setTint(r, g, b, a);
    }

    /**
     * Set shader effects (shorthand for material.setEffectFlags)
     */
    public void setEffects(int flags) {
        material.setEffectFlags(flags);
    }

    /**
     * Add shader effect (combines with existing)
     */
    public void addEffect(ShaderEffect effect) {
        material.setEffectFlags(material.getEffectFlags() | effect.flag);
    }

    /**
     * Remove shader effect
     */
    public void removeEffect(ShaderEffect effect) {
        material.setEffectFlags(material.getEffectFlags() & ~effect.flag);
    }

    @Override
    public void update(double dt) {}

    @Override
    public void render() {
        Transform transform = owner.getComponent(Transform.class);
        if (transform == null) return;

        float[] uv = atlas != null ? atlas.frameUV(frame) : new float[]{1, 1, 0, 0};
        float texelW = atlas != null ? atlas.texelW() : 1f;
        float texelH = atlas != null ? atlas.texelH() : 1f;

        renderer.drawQuad(
                transform.position.x,
                transform.position.y,
                width * transform.scale.x,
                height * transform.scale.y,
                transform.rotation,
                material,
                uv,
                texelW,
                texelH
        );
    }
}