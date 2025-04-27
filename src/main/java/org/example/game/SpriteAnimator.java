package org.example.game;

import org.example.engine.core.Component;
import org.example.engine.rendering.Sprite;
import org.example.engine.rendering.Texture;


public class SpriteAnimator extends Component {
    private final Sprite sprite;
    private final Texture[] frames;
    private final float frameDuration; // en secondes

    private float timer = 0f;
    private int current = 0;

    public SpriteAnimator(Sprite sprite, Texture[] frames, float frameDuration) {
        this.sprite = sprite;
        this.frames = frames;
        this.frameDuration = frameDuration;
    }

    @Override
    protected void onInit() {
        // On démarre sur la première image
        sprite.setTexture(frames[0]);
    }

    @Override
    protected void onUpdate(float dt) {
        timer += dt;
        if (timer >= frameDuration) {
            timer -= frameDuration;
            current = (current + 1) % frames.length;
            sprite.setTexture(frames[current]);
        }
    }
}
