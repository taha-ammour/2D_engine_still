// src/main/java/org/example/Main.java
package org.example;

import org.example.engine.GameEngine;
import org.example.core.scenes.PlayScene;
import org.example.engine.input.Input;
import org.example.engine.input.Mouse;

/**
 * Application entry point with proper initialization order
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("🎮 Starting MarioRev with Window Resize + Mouse Support...");

        GameEngine engine = new GameEngine(720, 480, "Unbeatable");

        engine.init();

        Input input = new Input(engine.getWindow().handle());
        Mouse mouse = new Mouse(engine.getWindow().handle());

        System.out.println("✅ Input systems initialized (Keyboard + Mouse)");

        // Now it's safe to create scenes that use the renderer
        PlayScene playScene = new PlayScene(
                input,
                mouse,  // ✅ Pass mouse to scene
                engine.getRenderer(),
                engine.getCamera()
        );

        engine.start(playScene);
        engine.close();

        System.out.println("👋 MarioRev closed");
    }
}