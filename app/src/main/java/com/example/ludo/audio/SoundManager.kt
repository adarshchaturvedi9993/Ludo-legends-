package com.example.ludo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Audio and haptics manager for Ludo Legends.
 * Plays background music using ludo1.mp3 and realistic dice roll sounds using diceroll.mp3,
 * combined with tactile haptics, audio focus handling, and procedural game sonification.
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

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var hasAudioFocus = false
    private var audioFocusRequest: AudioFocusRequest? = null

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                hasAudioFocus = false
                pauseMusic()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                pauseMusic()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                hasAudioFocus = true
                if (isMusicEnabled) {
                    resumeMusic()
                }
            }
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

    // Background Music (ludo1.mp3)
    private var backgroundMediaPlayer: MediaPlayer? = null

    // Dice Roll Sound Effect (diceroll.mp3) via SoundPool
    private var soundPool: SoundPool? = null
    private var diceRollSoundId: Int = 0
    private var isDiceRollLoaded: Boolean = false

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val pool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attributes)
                .build()

            soundPool = pool
            diceRollSoundId = pool.load(context, R.raw.diceroll, 1)
            pool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0 && sampleId == diceRollSoundId) {
                    isDiceRollLoaded = true
                }
            }
        } catch (_: Exception) {
            // Graceful fallback if SoundPool encounters native errors
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (audioManager == null) return true
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()
                audioFocusRequest = req
                val result = audioManager.requestAudioFocus(req)
                hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            } else {
                @Suppress("DEPRECATION")
                val result = audioManager.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN
                )
                hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            }
            hasAudioFocus
        } catch (_: Exception) {
            true
        }
    }

    private fun abandonAudioFocus() {
        if (audioManager == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(audioFocusChangeListener)
            }
            hasAudioFocus = false
        } catch (_: Exception) {}
    }

    // ==========================================
    // Background Music: ludo1.mp3
    // ==========================================

    fun startBackgroundMusic() {
        if (!isMusicEnabled) return
        if (!requestAudioFocus()) return
        try {
            if (backgroundMediaPlayer == null) {
                backgroundMediaPlayer = MediaPlayer.create(context, R.raw.ludo1)?.apply {
                    isLooping = true
                    setVolume(0.35f, 0.35f)
                    start()
                }
            } else {
                if (backgroundMediaPlayer?.isPlaying != true) {
                    backgroundMediaPlayer?.start()
                }
            }
        } catch (_: Exception) {}
    }

    fun resumeMusic() {
        if (!isMusicEnabled) return
        if (!requestAudioFocus()) return
        try {
            if (backgroundMediaPlayer == null) {
                startBackgroundMusic()
            } else if (backgroundMediaPlayer?.isPlaying != true) {
                backgroundMediaPlayer?.start()
            }
        } catch (_: Exception) {}
    }

    fun pauseMusic() {
        try {
            if (backgroundMediaPlayer?.isPlaying == true) {
                backgroundMediaPlayer?.pause()
            }
        } catch (_: Exception) {}
    }

    fun stopBackgroundMusic() {
        try {
            backgroundMediaPlayer?.stop()
            backgroundMediaPlayer?.release()
            backgroundMediaPlayer = null
        } catch (_: Exception) {}
        abandonAudioFocus()
    }

    fun release() {
        stopBackgroundMusic()
        try {
            soundPool?.release()
            soundPool = null
            isDiceRollLoaded = false
        } catch (_: Exception) {}
    }

    // ==========================================
    // Dice Roll Sound: diceroll.mp3
    // ==========================================

    fun playDiceRoll() {
        triggerHapticPattern(longArrayOf(0, 20, 35, 20, 30, 25))
        if (!isSoundEnabled) return

        try {
            if (isDiceRollLoaded && diceRollSoundId != 0) {
                soundPool?.play(diceRollSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
                return
            }

            // Fallback to MediaPlayer if SoundPool load not yet completed
            val mp = MediaPlayer.create(context, R.raw.diceroll)
            mp?.setOnCompletionListener { player ->
                try {
                    player.release()
                } catch (_: Exception) {}
            }
            mp?.start()
        } catch (_: Exception) {
            // Procedural safety fallback
            playProceduralDiceRoll()
        }
    }

    private fun playProceduralDiceRoll() {
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

    // ==========================================
    // Tactical SFX & Synthesized Effects
    // ==========================================

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
            } catch (_: Exception) {}
        }
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

    /**
     * Tactile "pop" sound when picking up/selecting a pawn directly on the board.
     */
    fun playPawnPick() {
        triggerHaptic(12)
        playPcm {
            val numSamples = (sampleRate * 0.045).toInt()
            val samples = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                val decay = (1.0 - progress) * (1.0 - progress)
                val freq = 600.0 + (progress * 500.0)
                val wave = sin(2.0 * PI * freq * t) * decay * 0.70
                samples[i] = (wave * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            samples
        }
    }

    fun playTurnStart() {
        playPcm {
            val numSamples = (sampleRate * 0.08).toInt()
            val samples = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val decay = 1.0 - (i.toDouble() / numSamples)
                val wave = sin(2.0 * PI * 680.0 * t) * decay * 0.30
                samples[i] = (wave * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
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
