package org.example.engine.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Audio system that manages sound playback using OpenAL.
 */
public class AudioSystem {
    private static AudioSystem instance;

    // OpenAL device and context
    private long device;
    private long context;

    // Sound cache
    private final Map<String, Sound> sounds = new HashMap<>();

    // Music track
    private MusicTrack currentMusic;

    // Audio settings
    private float masterVolume = 1.0f;
    private float sfxVolume = 1.0f;
    private float musicVolume = 1.0f;
    private boolean muted = false;

    // Listener position
    private float listenerX = 0.0f;
    private float listenerY = 0.0f;
    private float listenerZ = 0.0f;

    /**
     * Get the singleton instance
     */
    public static AudioSystem getInstance() {
        if (instance == null) {
            instance = new AudioSystem();
        }
        return instance;
    }

    /**
     * Private constructor for singleton
     */
    private AudioSystem() {
    }

    /**
     * Initialize the audio system
     */
    public void init() {
        // Open the default audio device
        device = alcOpenDevice((ByteBuffer) null);
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default OpenAL device");
        }

        // Create capabilities for the device
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);

        // Create an OpenAL context
        context = alcCreateContext(device, (IntBuffer) null);
        if (context == NULL) {
            alcCloseDevice(device);
            throw new IllegalStateException("Failed to create OpenAL context");
        }

        // Make the context current
        alcMakeContextCurrent(context);

        // Create capabilities for the context
        ALCapabilities alCaps = AL.createCapabilities(deviceCaps);

        // Set default listener properties
        alListener3f(AL_POSITION, 0.0f, 0.0f, 0.0f);
        alListener3f(AL_VELOCITY, 0.0f, 0.0f, 0.0f);

        // Initialize default orientation (facing along negative Z axis, up along positive Y)
        float[] orientation = {0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f};
        alListenerfv(AL_ORIENTATION, orientation);

        // Check for errors
        checkALError();
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        // Stop and clean up any playing sounds
        for (Sound sound : sounds.values()) {
            sound.cleanup();
        }
        sounds.clear();

        // Stop and clean up music
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }

        // Destroy OpenAL context and close device
        if (context != NULL) {
            alcDestroyContext(context);
        }

        if (device != NULL) {
            alcCloseDevice(device);
        }
    }

    /**
     * Update the audio system (call once per frame)
     */
    public void update() {
        // Update listener position
        alListener3f(AL_POSITION, listenerX, listenerY, listenerZ);

        // Update music if playing
        if (currentMusic != null) {
            currentMusic.update();
        }

        // Check for errors
        checkALError();
    }

    /**
     * Load a sound from a file
     */
    public Sound loadSound(String name, String filePath) {
        // Check if sound is already loaded
        if (sounds.containsKey(name)) {
            return sounds.get(name);
        }

        try {
            // Load the sound
            Sound sound = new Sound(filePath);
            sounds.put(name, sound);
            return sound;
        } catch (Exception e) {
            System.err.println("Failed to load sound: " + filePath);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a loaded sound by name
     */
    public Sound getSound(String name) {
        return sounds.get(name);
    }

    /**
     * Play a sound effect
     */
    public SoundInstance playSFX(String name) {
        return playSFX(name, 1.0f, 1.0f, false);
    }

    /**
     * Play a sound effect with parameters
     */
    public SoundInstance playSFX(String name, float volume, float pitch, boolean loop) {
        if (muted) {
            return null;
        }

        Sound sound = sounds.get(name);
        if (sound == null) {
            System.err.println("Sound not found: " + name);
            return null;
        }

        // Calculate final volume
        float finalVolume = masterVolume * sfxVolume * volume;

        // Create and play a new sound instance
        SoundInstance instance = sound.play(finalVolume, pitch, loop);

        // Set position to listener position (for 2D games)
        instance.setPosition(listenerX, listenerY, listenerZ);

        return instance;
    }

    /**
     * Play a sound at a specific position in the world
     */
    public SoundInstance playSFXAt(String name, float x, float y, float z, float volume, float pitch, boolean loop) {
        if (muted) {
            return null;
        }

        Sound sound = sounds.get(name);
        if (sound == null) {
            System.err.println("Sound not found: " + name);
            return null;
        }

        // Calculate final volume
        float finalVolume = masterVolume * sfxVolume * volume;

        // Create and play a new sound instance
        SoundInstance instance = sound.play(finalVolume, pitch, loop);

        // Set position
        instance.setPosition(x, y, z);

        return instance;
    }

    /**
     * Play music
     */
    public void playMusic(String filePath) {
        playMusic(filePath, 1.0f, true);
    }

    /**
     * Play music with parameters
     */
    public void playMusic(String filePath, float volume, boolean loop) {
        // Stop current music if playing
        if (currentMusic != null) {
            currentMusic.stop();
        }

        try {
            // Create a new music track
            currentMusic = new MusicTrack(filePath);

            // Calculate final volume
            float finalVolume = masterVolume * musicVolume * volume;

            // Set properties
            currentMusic.setVolume(finalVolume);
            currentMusic.setLooping(loop);

            // Play the music
            if (!muted) {
                currentMusic.play();
            }
        } catch (Exception e) {
            System.err.println("Failed to load music: " + filePath);
            e.printStackTrace();
            currentMusic = null;
        }
    }

    /**
     * Stop the current music
     */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }

    /**
     * Pause the current music
     */
    public void pauseMusic() {
        if (currentMusic != null) {
            currentMusic.pause();
        }
    }

    /**
     * Resume the current music
     */
    public void resumeMusic() {
        if (currentMusic != null && !muted) {
            currentMusic.resume();
        }
    }

    /**
     * Set the master volume
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateVolumes();
    }

    /**
     * Set the SFX volume
     */
    public void setSFXVolume(float volume) {
        this.sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateVolumes();
    }

    /**
     * Set the music volume
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        updateVolumes();
    }

    /**
     * Get the master volume
     */
    public float getMasterVolume() {
        return masterVolume;
    }

    /**
     * Get the SFX volume
     */
    public float getSFXVolume() {
        return sfxVolume;
    }

    /**
     * Get the music volume
     */
    public float getMusicVolume() {
        return musicVolume;
    }

    /**
     * Update all volumes based on current settings
     */
    private void updateVolumes() {
        // Update music volume if playing
        if (currentMusic != null) {
            currentMusic.setVolume(masterVolume * musicVolume);
        }

        // Note: Sound instances track their own volume settings
        // and would need to be updated individually if needed
    }

    /**
     * Set whether audio is muted
     */
    public void setMuted(boolean muted) {
        this.muted = muted;

        if (muted) {
            // Pause music if playing
            if (currentMusic != null) {
                currentMusic.pause();
            }

            // TODO: Pause all playing sounds if needed
        } else {
            // Resume music if it was playing
            if (currentMusic != null) {
                currentMusic.resume();
            }

            // TODO: Resume paused sounds if needed
        }
    }

    /**
     * Check if audio is muted
     */
    public boolean isMuted() {
        return muted;
    }

    /**
     * Set listener position (usually the camera or player position)
     */
    public void setListenerPosition(float x, float y, float z) {
        this.listenerX = x;
        this.listenerY = y;
        this.listenerZ = z;
    }

    /**
     * Set listener position in 2D (Z=0)
     */
    public void setListenerPosition(float x, float y) {
        setListenerPosition(x, y, 0.0f);
    }

    /**
     * Check for OpenAL errors and log them
     */
    private void checkALError() {
        int error = alGetError();
        if (error != AL_NO_ERROR) {
            System.err.println("OpenAL error: " + error);
        }
    }
}