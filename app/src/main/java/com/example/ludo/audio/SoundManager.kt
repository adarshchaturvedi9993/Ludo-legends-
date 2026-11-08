package com.example.ludo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Premium procedural sound synthesizer and ambient music engine.
 * Generates tactile wooden clacks, realistic tumbling dice, capture impacts, victory fanfares,
 * and a subtle, non-intrusive ambient background music loop dynamically using AudioTrack PCM.
 */
class SoundManager(private val context: Context) {

    private val audioScope = CoroutineScope(Dispatchers.Default)

    var isSoundEnabled: Boolean = true
    var isHapticsEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
        set(value) {
            field = value
            if (value) {
                resumeMusic()
            } else {
                pauseMusic()
            }
        }

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private val sampleRate = 44100
    private val musicSampleRate = 22050

    private var musicTrack: AudioTrack? = null
    private var isMusicInitialized = false

    private fun playPcm(generator: () -> ShortArray) {
        if (!isSoundEnabled) return
        audioScope.launch {
            try {
                val samples = generator()
                val minBuff = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = maxOf(minBuff, samples.size * 2)

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
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
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(samples, 0, samples.size)
                audioTrack.play()

                val durationMs = (samples.size * 1000L) / sampleRate + 40
                Thread.sleep(durationMs)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Fallback gracefully on any audio hardware limit
            }
        }
    }

    fun startBackgroundMusic() {
        if (!isMusicEnabled) return
        audioScope.launch {
            initAndStartMusic()
        }
    }

    fun resumeMusic() {
        if (!isMusicEnabled) return
        audioScope.launch {
            try {
                if (musicTrack == null) {
                    initAndStartMusic()
                } else if (musicTrack?.playState != AudioTrack.PLAYSTATE_PLAYING) {
                    musicTrack?.play()
                }
            } catch (_: Exception) {}
        }
    }

    fun pauseMusic() {
        try {
            if (musicTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                musicTrack?.pause()
            }
        } catch (_: Exception) {}
    }

    fun stopBackgroundMusic() {
        try {
            musicTrack?.stop()
            musicTrack?.release()
            musicTrack = null
            isMusicInitialized = false
        } catch (_: Exception) {}
    }

    private fun initAndStartMusic() {
        try {
            if (musicTrack != null) {
                if (musicTrack?.playState != AudioTrack.PLAYSTATE_PLAYING && isMusicEnabled) {
                    musicTrack?.play()
                }
                return
            }

            // Synthesize a calming 8-second ambient acoustic kalimba/marimba loop
            val loopDurationSec = 8.0
            val totalSamples = (musicSampleRate * loopDurationSec).toInt()
            val samples = ShortArray(totalSamples)

            // Warm, peaceful pentatonic melody sequence (time in seconds, frequency in Hz)
            val notes = listOf(
                Pair(0.0, 261.63), // C4
                Pair(0.7, 392.00), // G4
                Pair(1.4, 329.63), // E4
                Pair(2.1, 440.00), // A4
                Pair(3.0, 293.66), // D4
                Pair(3.7, 392.00), // G4
                Pair(4.4, 523.25), // C5
                Pair(5.2, 440.00), // A4
                Pair(6.0, 392.00), // G4
                Pair(6.8, 329.63)  // E4
            )

            for (note in notes) {
                val startSample = (note.first * musicSampleRate).toInt()
                val freq = note.second
                val noteDurationSamples = (musicSampleRate * 1.6).toInt()

                for (i in 0 until noteDurationSamples) {
                    val destIdx = startSample + i
                    if (destIdx >= totalSamples) break
                    val t = i.toDouble() / musicSampleRate
                    // Organic acoustic pluck envelope with gentle exponential decay
                    val envelope = exp(-3.8 * t) * (1.0 - exp(-60.0 * t))
                    // Fundamental + subtle warm 2nd harmonic
                    val wave = (sin(2.0 * PI * freq * t) * 0.75 + sin(2.0 * PI * (freq * 2.0) * t) * 0.25) * envelope
                    val sampleValue = (wave * 0.16 * Short.MAX_VALUE).toInt()
                    val current = samples[destIdx].toInt()
                    samples[destIdx] = (current + sampleValue).coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(musicSampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(samples, 0, samples.size)
            track.setLoopPoints(0, samples.size, -1)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                track.setVolume(0.24f) // Gentle, non-intrusive background volume
            }

            musicTrack = track
            isMusicInitialized = true

            if (isMusicEnabled) {
                track.play()
            }
        } catch (_: Exception) {}
    }

    fun playButtonTap() {
        triggerHaptic(15)
        playPcm {
            val numSamples = (sampleRate * 0.035).toInt()
            val samples = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val decay = 1.0 - (i.toDouble() / numSamples)
                val wave = sin(2.0 * PI * 850.0 * t) * decay * 0.35
                samples[i] = (wave * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            samples
        }
    }

    fun playDiceRoll() {
        triggerHapticPattern(longArrayOf(0, 20, 35, 20, 30, 25))
        playPcm {
            val durationSec = 0.45
            val numSamples = (sampleRate * durationSec).toInt()
            val samples = ShortArray(numSamples)
            val clicks = intArrayOf(
                0, (numSamples * 0.12).toInt(), (numSamples * 0.28).toInt(),
                (numSamples * 0.45).toInt(), (numSamples * 0.65).toInt(),
                (numSamples * 0.82).toInt(), (numSamples * 0.95).toInt()
            )

            for (i in 0 until numSamples) {
                val overallDecay = 1.0 - (i.toDouble() / numSamples) * 0.5
                var value = 0.0
                for (clickStart in clicks) {
                    val delta = i - clickStart
                    if (delta in 0..1200) {
                        val clickT = delta.toDouble() / sampleRate
                        val clickDecay = (1.0 - delta.toDouble() / 1200).coerceAtLeast(0.0)
                        val freq = 450.0 + (clickStart % 3) * 120.0
                        value += sin(2.0 * PI * freq * clickT) * clickDecay * 0.7
                    }
                }
                val t = i.toDouble() / sampleRate
                value += sin(2.0 * PI * 180.0 * t) * 0.08 * overallDecay
                samples[i] = (value.coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.75).toInt().toShort()
            }
            samples
        }
    }

    fun playTokenStep() {
        triggerHaptic(25)
        playPcm {
            val numSamples = (sampleRate * 0.08).toInt()
            val samples = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val decay = (1.0 - (i.toDouble() / numSamples))
                val wave = (sin(2.0 * PI * 540.0 * t) * 0.75 + sin(2.0 * PI * 1080.0 * t) * 0.25) * decay * decay
                samples[i] = (wave * Short.MAX_VALUE * 0.7).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            samples
        }
    }

    fun playCapture() {
        triggerHapticPattern(longArrayOf(0, 50, 40, 70))
        playPcm {
            val numSamples = (sampleRate * 0.32).toInt()
            val samples = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val decay = (1.0 - progress)
                val freq = 760.0 - (progress * 360.0)
                val wave = (sin(2.0 * PI * freq * t) * 0.6 + sin(2.0 * PI * (freq * 1.5) * t) * 0.4) * decay
                samples[i] = (wave * Short.MAX_VALUE * 0.85).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            samples
        }
    }

    fun playTokenHome() {
        triggerHaptic(40)
        playPcm {
            val numSamples = (sampleRate * 0.35).toInt()
            val samples = ShortArray(numSamples)
            val subDuration = numSamples / 3
            val notes = doubleArrayOf(523.25, 659.25, 783.99) // C5, E5, G5

            for (i in 0 until numSamples) {
                val noteIdx = (i / subDuration).coerceIn(0, 2)
                val noteFreq = notes[noteIdx]
                val noteOffset = i % subDuration
                val noteDecay = 1.0 - (noteOffset.toDouble() / subDuration)
                val t = noteOffset.toDouble() / sampleRate
                val wave = sin(2.0 * PI * noteFreq * t) * noteDecay * 0.65
                samples[i] = (wave * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            samples
        }
    }

    fun playVictory() {
        triggerHapticPattern(longArrayOf(0, 60, 60, 60, 60, 120))
        playPcm {
            val numSamples = (sampleRate * 0.85).toInt()
            val samples = ShortArray(numSamples)
            val subDuration = numSamples / 6
            val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51, 1567.98) // C5, E5, G5, C6, E6, G6

            for (i in 0 until numSamples) {
                val noteIdx = (i / subDuration).coerceIn(0, 5)
                val noteFreq = notes[noteIdx]
                val noteOffset = i % subDuration
                val noteDecay = 1.0 - (noteOffset.toDouble() / subDuration) * 0.85
                val t = noteOffset.toDouble() / sampleRate
                val wave = (sin(2.0 * PI * noteFreq * t) * 0.7 + sin(2.0 * PI * (noteFreq * 2.0) * t) * 0.3) * noteDecay * 0.75
                samples[i] = (wave * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            samples
        }
    }

    private fun triggerHaptic(durationMs: Long) {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun triggerHapticPattern(timings: LongArray) {
        if (!isHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings, -1)
            }
        } catch (_: Exception) {}
    }
}
