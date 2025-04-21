package org.example.engine.audio;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Represents a sound resource that can be played multiple times.
 */
public class Sound {
    // OpenAL buffer ID
    private final int bufferId;

    // Sound properties
    private final int channels;
    private final int sampleRate;

    // List of active instances of this sound
    private final List<SoundInstance> activeInstances = new ArrayList<>();

    /**
     * Load a sound from a file
     */
    public Sound(String filePath) throws IOException {
        // Generate a buffer ID
        bufferId = alGenBuffers();

        // Load sound data
        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            ShortBuffer pcm = readVorbis(filePath, info);

            // Store sound properties
            channels = info.channels();
            sampleRate = info.sample_rate();

            // Determine format based on channels
            int format = channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;

            // Upload data to OpenAL
            alBufferData(bufferId, format, pcm, sampleRate);

            // Free the PCM data
            MemoryUtil.memFree(pcm);
        }
    }

    /**
     * Play this sound with default parameters
     */
    public SoundInstance play() {
        return play(1.0f, 1.0f, false);
    }

    /**
     * Play this sound with the specified parameters
     */
    public SoundInstance play(float volume, float pitch, boolean loop) {
        // Create a new sound instance
        SoundInstance instance = new SoundInstance(this, bufferId);

        // Set properties
        instance.setVolume(volume);
        instance.setPitch(pitch);
        instance.setLooping(loop);

        // Play the sound
        instance.play();

        // Add to active instances list
        activeInstances.add(instance);

        return instance;
    }

    /**
     * Stop all active instances of this sound
     */
    public void stopAll() {
        for (SoundInstance instance : new ArrayList<>(activeInstances)) {
            instance.stop();
        }
    }

    /**
     * Remove an instance from the active list
     */
    void removeInstance(SoundInstance instance) {
        activeInstances.remove(instance);
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        // Stop all playing instances
        stopAll();

        // Delete buffer
        alDeleteBuffers(bufferId);
    }

    /**
     * Get the number of channels
     */
    public int getChannels() {
        return channels;
    }

    /**
     * Get the sample rate
     */
    public int getSampleRate() {
        return sampleRate;
    }

    /**
     * Get the buffer ID
     */
    public int getBufferId() {
        return bufferId;
    }

    /**
     * Read Vorbis audio data from a file
     */
    private ShortBuffer readVorbis(String filePath, STBVorbisInfo info) throws IOException {
        ByteBuffer vorbis;

        try {
            vorbis = readFile(filePath);
        } catch (IOException e) {
            throw new IOException("Failed to read Vorbis file: " + filePath, e);
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Create error buffer
            IntBuffer error = stack.mallocInt(1);

            // Create a handle for decoding the Vorbis data
            long decoder = stb_vorbis_open_memory(vorbis, error, null);
            if (decoder == NULL) {
                throw new IOException("Failed to open Vorbis file: " + error.get(0));
            }

            // Get info about the Vorbis data
            stb_vorbis_get_info(decoder, info);

            // Get the total number of samples
            int totalSamples = stb_vorbis_stream_length_in_samples(decoder);

            // Allocate buffer for PCM data
            ShortBuffer pcm = MemoryUtil.memAllocShort(totalSamples * info.channels());

            // Decode the Vorbis data to PCM
            stb_vorbis_get_samples_short_interleaved(decoder, info.channels(), pcm);

            // Close the decoder
            stb_vorbis_close(decoder);

            return pcm;
        }
    }

    /**
     * Read a file into a byte buffer
     */
    private ByteBuffer readFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        ByteBuffer buffer;

        try (SeekableByteChannel channel = Files.newByteChannel(path)) {
            buffer = BufferUtils.createByteBuffer((int) channel.size() + 1);
            while (channel.read(buffer) != -1) {
                // Keep reading until the end of the file
            }
        }

        // Flip the buffer to prepare for reading
        buffer.flip();

        return buffer;
    }

    /**
     * Read a resource file into a byte buffer
     */
    private ByteBuffer readResourceFile(String resourcePath) throws IOException {
        ByteBuffer buffer;

        // Get resource as stream
        InputStream stream = getClass().getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        try (ReadableByteChannel channel = Channels.newChannel(stream)) {
            // Read the stream into a buffer (we don't know the size in advance)
            buffer = BufferUtils.createByteBuffer(8192); // Initial size

            while (true) {
                // Read data
                int bytesRead = channel.read(buffer);
                if (bytesRead == -1) {
                    break; // End of stream
                }

                // Check if we need to resize the buffer
                if (buffer.remaining() == 0) {
                    // Double the buffer size
                    ByteBuffer newBuffer = BufferUtils.createByteBuffer(buffer.capacity() * 2);
                    buffer.flip();
                    newBuffer.put(buffer);
                    buffer = newBuffer;
                }
            }
        }

        // Flip the buffer to prepare for reading
        buffer.flip();

        return buffer;
    }
}