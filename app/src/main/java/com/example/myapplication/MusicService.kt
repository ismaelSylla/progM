package com.example.myapplication

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder


class MusicService : Service() {
    private lateinit var mediaPlayer: MediaPlayer
    private var isAppInBackground = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.accueil) // Remplacez R.raw.your_music_file par votre propre fichier audio
        mediaPlayer.isLooping = true

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaPlayer.start()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopMusicIfInBackground()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            isAppInBackground = true
            stopMusicIfInBackground()
        }
    }

    private fun stopMusicIfInBackground() {
        if (isAppInBackground) {
            mediaPlayer.stop()
            mediaPlayer.release()
            stopSelf()
        }
    }
}
