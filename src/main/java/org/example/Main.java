// Updated Main.java to use new engine
package org.example;

import org.example.engine.GameEngine;
import org.example.core.scenes.PlayScene;
import org.example.engine.input.Input;

public class Main {
    public static void main(String[] args) {
        GameEngine engine = new GameEngine(1280, 720, "MarioRev");
        engine.init(); // Initialize window and OpenGL context first!

        // Now it's safe to create scenes that use the renderer
        PlayScene playScene = new PlayScene(
                new Input(engine.getWindow().handle()),
                engine.getRenderer(),
                engine.getCamera()
        );

        engine.start(playScene);
        engine.close();
    }
}