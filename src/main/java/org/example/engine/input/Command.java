package org.example.engine.input;

@FunctionalInterface
public interface Command {
    void execute(double dt);
}
