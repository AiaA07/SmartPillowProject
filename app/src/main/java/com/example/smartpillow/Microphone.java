package com.example.smartpillow;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.content.ContextCompat;
public class Microphone {
    private static final String TAG = "Microphone";

    // Audio capture configuration
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final double REFERENCE_AMPLITUDE = 1.0;

    private final Context context;
    private AudioRecord audioRecord;
    private int bufferSize;
    private boolean isRecording;
    private double lastDecibelLevel;

    public Microphone(Context context) {
        this.context = context.getApplicationContext();
        this.isRecording = false;
        this.lastDecibelLevel = 0.0;
    }

    public boolean hasPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Initialises AudioRecord and begins capturing audio samples.
     *
     * @return true if recording started successfully, false if permission is
     *         missing or the hardware could not be acquired.
     */
    public boolean start() {
        if (!hasPermission()) {
            Log.e(TAG, "RECORD_AUDIO permission not granted. Cannot start.");
            return false;
        }

        if (isRecording) {
            Log.w(TAG, "Already recording.");
            return true;
        }

        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR) {
            Log.e(TAG, "Invalid buffer size: " + bufferSize);
            return false;
        }

        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize
            );
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException creating AudioRecord", e);
            return false;
        }

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord failed to initialise.");
            audioRecord.release();
            audioRecord = null;
            return false;
        }

        audioRecord.startRecording();
        isRecording = true;
        Log.i(TAG, "Microphone recording started.");
        return true;
    }

    /**
     * Stops recording and releases the microphone hardware.
     */
    public void stop() {
        if (audioRecord != null && isRecording) {
            try {
                audioRecord.stop();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error stopping AudioRecord", e);
            }
            isRecording = false;
            Log.i(TAG, "Microphone recording stopped.");
        }
    }

    /**
     * Releases all resources. Call this when the Microphone instance is no
     * longer needed (e.g. in Activity.onDestroy).
     */
    public void release() {
        stop();
        if (audioRecord != null) {
            audioRecord.release();
            audioRecord = null;
            Log.i(TAG, "AudioRecord released.");
        }
    }

    /**
     * Reads the current audio buffer and computes an approximate dB SPL value.
     *
     * The value is derived from the RMS (root-mean-square) of the PCM samples
     * and converted to a logarithmic decibel scale:
     *     dB = 20 * log10(rms / REFERENCE_AMPLITUDE)
     *
     * @return decibel level (0 if silent or not recording). Typical range
     *         for sleep environments is roughly 20-60 dB.
     */
    public double getDecibelLevel() {
        if (!isRecording || audioRecord == null) {
            return 0.0;
        }

        short[] buffer = new short[bufferSize / 2]; // 16-bit samples = 2 bytes each
        int readCount = audioRecord.read(buffer, 0, buffer.length);

        if (readCount <= 0) {
            return lastDecibelLevel;
        }

        double sumSquares = 0;
        for (int i = 0; i < readCount; i++) {
            sumSquares += (double) buffer[i] * buffer[i];
        }

        double rms = Math.sqrt(sumSquares / readCount);

        if (rms < REFERENCE_AMPLITUDE) {
            lastDecibelLevel = 0.0;
        } else {
            lastDecibelLevel = 20.0 * Math.log10(rms / REFERENCE_AMPLITUDE);
        }

        return lastDecibelLevel;
    }

    /**
     * Returns the most recent decibel reading without performing a new
     * buffer read. Useful for UI updates on a faster timer.
     */
    public double getLastDecibelLevel() {
        return lastDecibelLevel;
    }

    /**
     * Convenience: reads a decibel sample and persists it to the raw sensor
     * data staging table via the existing DatabaseManager pipeline.
     *
     * Sensor type 3 = Microphone (matches Accel=1, Gyro=2 convention).
     *
     * @param dbManager   an open DatabaseManager instance
     * @param sessionId   the current sleep session ID
     */
    public void sampleAndSave(DatabaseManager dbManager, long sessionId) {
        double db = getDecibelLevel();
        if (db > 0 && sessionId != -1) {
            dbManager.saveRawSensorData(sessionId, 3, db);
        }
    }
    public boolean isRecording() {
        return isRecording;
    }
}
