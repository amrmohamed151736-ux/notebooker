package com.example.focus

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.media.AudioManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundEffectManager {
    private var isMuted = false
    private var volume = 0.8f

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun isMuted(): Boolean = isMuted

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
    }

    fun getVolume(): Float = volume

    /**
     * Synthesize and play a sequence of frequency tones offline.
     * Frequencies in Hz, durations in ms.
     */
    fun playFrequencies(frequencies: List<Pair<Int, Int>>) {
        if (isMuted || volume <= 0.01f) return

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val sampleRate = 44100
                var totalDurationMs = 0
                frequencies.forEach { totalDurationMs += it.second }
                val totalSamples = (sampleRate * (totalDurationMs / 1000.0)).toInt()
                val buffer = ShortArray(totalSamples)

                var currentSampleIdx = 0
                for ((freq, durationMs) in frequencies) {
                    val count = (sampleRate * (durationMs / 1000.0)).toInt()
                    for (i in 0 until count) {
                        if (currentSampleIdx >= buffer.size) break
                        // Smooth envelope to prevent audio popping
                        val envelope = when {
                            i < 200 -> i / 200.0
                            i > count - 400 -> (count - i) / 400.0
                            else -> 1.0
                        }
                        val angle = 2.0 * Math.PI * i / (sampleRate.toDouble() / freq)
                        val sampleVal = (sin(angle) * Short.MAX_VALUE * volume * envelope).toInt()
                        buffer[currentSampleIdx++] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                Thread.sleep(totalDurationMs.toLong() + 50)
                audioTrack.release()
            } catch (e: Exception) {
                // Fallback to ToneGenerator if AudioTrack has platform constraints
                try {
                    val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, (volume * 100).toInt())
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                } catch (_: Exception) {}
            }
        }
    }

    // --- Specialized Sound Effect Triggers ---

    fun playSessionStart() {
        // Tranquil ascending two-tone bell (C5 -> G5)
        playFrequencies(listOf(523 to 180, 784 to 320))
    }

    fun playSessionComplete() {
        // Victory Arpeggio (C5 -> E5 -> G5 -> C6)
        playFrequencies(listOf(523 to 120, 659 to 120, 784 to 150, 1046 to 400))
    }

    fun playPointsEarned() {
        // High sparkling dual chime (E6 -> B6)
        playFrequencies(listOf(1318 to 90, 1975 to 220))
    }

    fun playItemPurchased() {
        // Joyful purchase chime (G5 -> C6)
        playFrequencies(listOf(784 to 120, 1046 to 280))
    }

    fun playAchievementUnlocked() {
        // Fanfare flourish (G4 -> C5 -> E5 -> G5 -> C6)
        playFrequencies(listOf(392 to 100, 523 to 100, 659 to 100, 784 to 120, 1046 to 450))
    }

    fun playCyberPulse() {
        // Futuristic sci-fi blip
        playFrequencies(listOf(880 to 60, 440 to 80, 1760 to 140))
    }

    fun playZenBell() {
        // Resonant meditation bowl tone (A4)
        playFrequencies(listOf(440 to 600))
    }

    fun playNatureRain() {
        // Gentle soft chord
        playFrequencies(listOf(330 to 250, 392 to 350))
    }

    fun previewSound(soundId: String) {
        when (soundId) {
            "SND_ZEN_BELL" -> playZenBell()
            "SND_NATURE" -> playNatureRain()
            "SND_POINTS_CHIME" -> playPointsEarned()
            "SND_CYBER" -> playCyberPulse()
            "SND_FANFARE" -> playAchievementUnlocked()
            else -> playPointsEarned()
        }
    }
}
