package org.example.engine.audio;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.AL_SEC_OFFSET;

/**
 * Represents a specific instance of a sound being played.
 * Allows control over individual sound playback.
 */
public class SoundInstance {
    // Parent sound
    private final Sound parent;

    // OpenAL source ID
    private final int sourceId;

    // Sound buffer ID
    private final int bufferId;

    // State flags
    private boolean isPlaying = false;
    private boolean isPaused = false;

    /**
     * Create a new sound instance
     */
    SoundInstance(Sound parent, int bufferId) {
        this.parent = parent;
        this.bufferId = bufferId;

        // Generate a source
        sourceId = alGenSources();

        // Attach the buffer
        alSourcei(sourceId, AL_BUFFER, bufferId);

        // Set default properties
        alSourcef(sourceId, AL_GAIN, 1.0f);
        alSourcef(sourceId, AL_PITCH, 1.0f);
        alSourcei(sourceId, AL_LOOPING, AL_FALSE);

        // Set position at origin (will be updated later)
        alSource3f(sourceId, AL_POSITION, 0.0f, 0.0f, 0.0f);
    }

    /**
     * Play the sound
     */
    public void play() {
        alSourcePlay(sourceId);
        isPlaying = true;
        isPaused = false;
    }

    /**
     * Pause the sound
     */
    public void pause() {
        if (isPlaying) {
            alSourcePause(sourceId);
            isPlaying = false;
            isPaused = true;
        }
    }

    /**
     * Resume the sound
     */
    public void resume() {
        if (isPaused) {
            alSourcePlay(sourceId);
            isPlaying = true;
            isPaused = false;
        }
    }

    /**
     * Stop the sound
     */
    public void stop() {
        alSourceStop(sourceId);
        isPlaying = false;
        isPaused = false;

        // Detach from parent sound
        parent.removeInstance(this);

        // Delete the source
        alDeleteSources(sourceId);
    }

    /**
     * Set the volume for this sound instance
     */
    public void setVolume(float volume) {
        alSourcef(sourceId, AL_GAIN, Math.max(0.0f, Math.min(1.0f, volume)));
    }

    /**
     * Set the pitch for this sound instance
     */
    public void setPitch(float pitch) {
        alSourcef(sourceId, AL_PITCH, Math.max(0.5f, Math.min(2.0f, pitch)));
    }

    /**
     * Set whether this sound instance should loop
     */
    public void setLooping(boolean loop) {
        alSourcei(sourceId, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
    }

    /**
     * Set the position of this sound instance in 3D space
     */
    public void setPosition(float x, float y, float z) {
        alSource3f(sourceId, AL_POSITION, x, y, z);
    }

    /**
     * Set the velocity of this sound instance in 3D space
     */
    public void setVelocity(float x, float y, float z) {
        alSource3f(sourceId, AL_VELOCITY, x, y, z);
    }

    /**
     * Set the maximum distance at which the sound can be heard
     */
    public void setMaxDistance(float distance) {
        alSourcef(sourceId, AL_MAX_DISTANCE, distance);
    }

    /**
     * Set the reference distance (where volume is at maximum)
     */
    public void setReferenceDistance(float distance) {
        alSourcef(sourceId, AL_REFERENCE_DISTANCE, distance);
    }

    /**
     * Set the rolloff factor (how quickly the sound attenuates with distance)
     */
    public void setRolloffFactor(float factor) {
        alSourcef(sourceId, AL_ROLLOFF_FACTOR, factor);
    }

    /**
     * Check if this sound is currently playing
     */
    public boolean isPlaying() {
        // Check the actual state from OpenAL
        int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
        boolean actuallyPlaying = state == AL_PLAYING;

        // Update our internal flag
        isPlaying = actuallyPlaying;

        // If it was playing and now it's not, and it's not paused, it must have finished
        if (!actuallyPlaying && !isPaused && parent != null) {
            // Remove from parent
            parent.removeInstance(this);

            // Delete the source
            alDeleteSources(sourceId);
        }

        return actuallyPlaying;
    }

    /**
     * Check if this sound is currently paused
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Get the playback position in seconds
     */
    public float getPlaybackPosition() {
        return alGetSourcef(sourceId, AL_SEC_OFFSET);
    }

    /**
     * Set the playback position in seconds
     */
    public void setPlaybackPosition(float seconds) {
        alSourcef(sourceId, AL_SEC_OFFSET, seconds);
    }
}