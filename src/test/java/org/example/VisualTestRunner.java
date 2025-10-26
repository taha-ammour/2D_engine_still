// src/main/java/org/example/VisualTestRunner.java
package org.example;

import org.example.core.scenes.VisualTestScene;
import org.example.engine.GameEngine;
import org.example.engine.input.Input;
import org.example.engine.input.Mouse;

/**
 * Visual Test Runner - Watch your tests in action!
 *
 * Run this to see collision system tests executing visually on screen.
 *
 * Controls:
 * - Press 1-9: Run different test cases
 * - Press R: Reset current test
 * - Press ESC: Exit
 */
public class VisualTestRunner {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   VISUAL TEST RUNNER - COLLISION      ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║  Press number keys 1-9 to run tests   ║");
        System.out.println("║  Green boxes = Collision debug info   ║");
        System.out.println("║  Red boxes = Collision boundaries      ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println();

        GameEngine engine = new GameEngine(1280, 720, "Collision System - Visual Tests");
        engine.init();

        VisualTestScene testScene = new VisualTestScene(
                new Input(engine.getWindow().handle()),
                new Mouse(engine.getWindow().handle()),
                engine.getRenderer(),
                engine.getCamera()
        );

        engine.start(testScene);
        engine.close();
    }
}