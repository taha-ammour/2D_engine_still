package org.example.engine.events;

/**
 * Interface for objects that can receive events.
 */
public interface EventListener {
    /**
     * Called when an event is received.
     */
    void onEvent(Object event);
}