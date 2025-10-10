package org.example.engine.time;

public final class Time {
    private static double lastTime;
    public static void start(double now) { lastTime = now; }
    public static double delta(double now) {
        double dt = now - lastTime; lastTime = now; return dt;
    }
}
