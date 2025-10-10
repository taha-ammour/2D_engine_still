package org.example.core;

public interface Scene {
    void load();
    void unload();
    void update(double dt);
    void render();
    void handleInput();

    // Optional lifecycle hooks
    default void onEnter() {}
    default void onExit() {}
}
