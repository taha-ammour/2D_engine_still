package org.example.engine.audio;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Represents a music track that can be played in the background.
 * Streams audio data for efficient memory usage.
 */
public class MusicTrack {
    // OpenAL buffer count (for streaming)
    private static final int NUM_BUFFERS = 4;
    private static final int BUFFER_SIZE = 65536; // 64KB chunks

    // Path to the music file
    private final String filePath;

    // OpenAL source ID
    private final int sourceId;

    // OpenAL buffer IDs
    private final int[] bufferIds = new int[NUM_BUFFERS];

    // Vorbis decoder handle
    private long vorbisHandle;

    // Audio properties
    private int channels;
    private int sampleRate;

    // State
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private boolean isLooping = true;
    private float volume = 1.0f;

    // PCM data buffer for streaming
    private ShortBuffer pcmBuffer;

    /**
     * Create a new music track from a file
     */
    public MusicTrack(String filePath) throws IOException {
        this.filePath = filePath;

        // Generate source
        sourceId = alGenSources();

        // Set source properties
        alSourcef(sourceId, AL_GAIN, volume);
        alSourcef(sourceId, AL_PITCH, 1.0f);
        alSource3f(sourceId, AL_POSITION, 0.0f, 0.0f, 0.0f);
        alSource3f(sourceId, AL_VELOCITY, 0.0f, 0.0f, 0.0f);

        // Generate buffers
        for (int i = 0; i < NUM_BUFFERS; i++) {
            bufferIds[i] = alGenBuffers();
        }

        // Open the Vorbis file
        openVorbisFile();

        // Allocate PCM buffer
        pcmBuffer = BufferUtils.createShortBuffer(BUFFER_SIZE);
    }

    /**
     * Open the Vorbis file and initialize the decoder
     */
    private void openVorbisFile() throws IOException {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Read the entire Vorbis file into memory
            ByteBuffer vorbisData = readFile(filePath);

            // Create error buffer
            IntBuffer error = stack.mallocInt(1);

            // Open the Vorbis data for decoding
            vorbisHandle = stb_vorbis_open_memory(vorbisData, error, null);
            if (vorbisHandle == NULL) {
                throw new IOException("Failed to open Vorbis file: " + error.get(0));
            }

            // Get info about the audio
            STBVorbisInfo info = STBVorbisInfo.malloc(stack);
            stb_vorbis_get_info(vorbisHandle, info);

            // Store audio properties
            channels = info.channels();
            sampleRate = info.sample_rate();
        }
    }

    /**
     * Read a file into a byte buffer
     */
    private ByteBuffer readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] data = Files.readAllBytes(path);

        ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
        buffer.put(data);
        buffer.flip();

        return buffer;
    }

    /**
     * Play the music track
     */
    public void play() {
        if (isPlaying && !isPaused) {
            return; // Already playing
        }

        if (isPaused) {
            // Resume from pause
            alSourcePlay(sourceId);
            isPlaying = true;
            isPaused = false;
            return;
        }

        // Reset the decoder to the beginning
        stb_vorbis_seek_start(vorbisHandle);

        // Clear any existing queued buffers
        alSourcei(sourceId, AL_BUFFER, 0);

        // Fill and queue initial buffers
        int buffersQueued = 0;
        for (int i = 0; i < NUM_BUFFERS; i++) {
            if (fillBuffer(bufferIds[i])) {
                alSourceQueueBuffers(sourceId, bufferIds[i]);
                buffersQueued++;
            } else {
                break; // No more data
            }
        }

        if (buffersQueued > 0) {
            // Start playback
            alSourcePlay(sourceId);
            isPlaying = true;
            isPaused = false;
        }
    }

    /**
     * Pause the music track
     */
    public void pause() {
        if (isPlaying && !isPaused) {
            alSourcePause(sourceId);
            isPlaying = false;
            isPaused = true;
        }
    }

    /**
     * Resume the music track from pause
     */
    public void resume() {
        if (isPaused) {
            alSourcePlay(sourceId);
            isPlaying = true;
            isPaused = false;
        }
    }

    /**
     * Stop the music track
     */
    public void stop() {
        if (isPlaying || isPaused) {
            alSourceStop(sourceId);
            isPlaying = false;
            isPaused = false;
        }
    }

    /**
     * Update the streaming (call each frame)
     */
    public void update() {
        if (!isPlaying) {
            return;
        }

        // Check the source state
        int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
        if (state != AL_PLAYING) {
            // If it's stopped but we think it's playing, it might have run out of buffers
            isPlaying = false;

            // Check if there are any processed buffers
            int processed = alGetSourcei(sourceId, AL_BUFFERS_PROCESSED);
            if (processed == 0) {
                // No processed buffers, track is done
                if (isLooping) {
                    // Reset and restart
                    stb_vorbis_seek_start(vorbisHandle);
                    play();
                }
                return;
            } else {
                // There are processed buffers, let's continue playing
                isPlaying = true;
            }
        }

        // Process any finished buffers
        int processed = alGetSourcei(sourceId, AL_BUFFERS_PROCESSED);
        while (processed > 0) {
            // Get a processed buffer
            int buffer = alSourceUnqueueBuffers(sourceId);

            // Fill it with new data
            if (fillBuffer(buffer)) {
                // Queue it back
                alSourceQueueBuffers(sourceId, buffer);
            } else if (isLooping) {
                // Reset to beginning and try again
                stb_vorbis_seek_start(vorbisHandle);
                if (fillBuffer(buffer)) {
                    alSourceQueueBuffers(sourceId, buffer);
                }
            }

            processed--;
        }

        // Check if the source stopped playing because it ran out of buffers
        state = alGetSourcei(sourceId, AL_SOURCE_STATE);
        if (state != AL_PLAYING && isPlaying) {
            alSourcePlay(sourceId);
        }
    }

    /**
     * Fill a buffer with audio data
     */
    private boolean fillBuffer(int buffer) {
        // Clear the PCM buffer
        pcmBuffer.clear();

        // Read PCM data from the Vorbis file
        int samplesRead = stb_vorbis_get_samples_short_interleaved(vorbisHandle, channels, pcmBuffer);

        // If we couldn't read any samples, we're at the end of the file
        if (samplesRead == 0) {
            return false;
        }

        // Flip the buffer so it can be read from
        pcmBuffer.flip();

        // Determine the format based on the number of channels
        int format = (channels == 1) ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;

        // Fill the OpenAL buffer
        alBufferData(buffer, format, pcmBuffer, sampleRate);

        return true;
    }

    /**
     * Set the volume of the music track
     */
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        alSourcef(sourceId, AL_GAIN, this.volume);
    }

    /**
     * Get the current volume
     */
    public float getVolume() {
        return volume;
    }

    /**
     * Set whether the music track should loop
     */
    public void setLooping(boolean loop) {
        this.isLooping = loop;
    }

    /**
     * Check if the music track is set to loop
     */
    public boolean isLooping() {
        return isLooping;
    }

    /**
     * Check if the music track is currently playing
     */
    public boolean isPlaying() {
        return isPlaying && !isPaused;
    }

    /**
     * Check if the music track is currently paused
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        // Stop playback
        stop();

        // Delete buffers and source
        alDeleteBuffers(bufferIds);
        alDeleteSources(sourceId);

        // Close Vorbis file
        if (vorbisHandle != NULL) {
            stb_vorbis_close(vorbisHandle);
            vorbisHandle = NULL;
        }

        // Free PCM buffer
        if (pcmBuffer != null) {
            MemoryUtil.memFree(pcmBuffer);
            pcmBuffer = null;
        }
    }
}