package com.racers.util

import android.content.Context
import android.media.MediaPlayer
import com.racers.R

class SoundHelper(private val context: Context) {
    private var engineSound: MediaPlayer? = null
    private var collisionSound: MediaPlayer? = null
    private var backgroundMusic: MediaPlayer? = null

    init {
        try {
            engineSound = MediaPlayer.create(context, R.raw.engine)
            collisionSound = MediaPlayer.create(context, R.raw.crash)
            backgroundMusic = MediaPlayer.create(context, R.raw.background_music)
            
            engineSound?.isLooping = true
            backgroundMusic?.isLooping = true
        } catch (e: Exception) {
            // Log error but don't crash if sound files are missing
            e.printStackTrace()
        }
    }

    fun startBackgroundMusic() {
        backgroundMusic?.start()
    }

    fun playEngineSound(speed: Float) {
        engineSound?.let {
            it.setVolume(speed, speed)
            if (!it.isPlaying) it.start()
        }
    }

    fun playCollisionSound() {
        collisionSound?.start()
    }

    fun release() {
        engineSound?.release()
        collisionSound?.release()
        backgroundMusic?.release()
    }
}