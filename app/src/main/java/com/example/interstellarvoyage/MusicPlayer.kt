package com.example.interstellarvoyage

import android.app.ActivityManager
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log

class MusicPlayer() : Service(), MediaPlayer.OnErrorListener {

    private val binder = MusicBinder()
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentMusicResourceId = -1
    private var isMusicEnabled = true
    private val popSoundPlayers = mutableListOf<MediaPlayer>()

    inner class MusicBinder : Binder() {
        fun getService(): MusicPlayer = this@MusicPlayer
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.homepage_music)
        mediaPlayer?.setOnErrorListener(this)
        mediaPlayer?.isLooping = true
        mediaPlayer?.setOnPreparedListener {
            playMusic()
        }
        currentMusicResourceId = R.raw.homepage_music
    }

    companion object {
        const val ACTION_PLAY_MUSIC = "com.example.interstellarvoyage.ACTION_PLAY_MUSIC"
        const val ACTION_PAUSE_MUSIC = "com.example.interstellarvoyage.ACTION_PAUSE_MUSIC"
        const val ACTION_STOP_MUSIC = "com.example.interstellarvoyage.ACTION_STOP_MUSIC"
        const val EXTRA_MUSIC_RESOURCE_ID = "com.example.interstellarvoyage.EXTRA_MUSIC_RESOURCE_ID"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action

            when (action) {
                ACTION_PLAY_MUSIC -> {
                    val musicResourceId = intent.getIntExtra(EXTRA_MUSIC_RESOURCE_ID, -1)
                    if (musicResourceId != -1) {
                        changeMusic(musicResourceId)
                    }
                }
                ACTION_PAUSE_MUSIC -> {
                    pauseMusic()
                }
                ACTION_STOP_MUSIC -> {
                    stopMusic()
                }
                else -> {
                    // Handle other cases or do nothing
                }
            }
        }
        return START_STICKY
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.e("Music Error", "MediaPlayer error: what = $what, extra = $extra")
        return false
    }

    fun isMusicEnabled(): Boolean {
        return isMusicEnabled
    }

    fun setMusicEnabled(enabled: Boolean) {
        isMusicEnabled = enabled
        if (enabled) {
            playMusic()
        } else {
            pauseMusic()
        }
    }

    fun getCurrentMusicResourceId(): Int {
        return currentMusicResourceId
    }

    fun playMusic() {
        if (!isPlaying) {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
                isPlaying = true
            }
        }
    }

    fun pauseMusic() {
        if (isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
        }
    }

    fun stopMusic() {
        if (isPlaying) {
            mediaPlayer?.stop()
            mediaPlayer?.prepare()
            isPlaying = false
        }
    }

    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun changeMusic(newMusicResourceId: Int) {
        stopMusic()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this@MusicPlayer, newMusicResourceId)
        mediaPlayer?.isLooping = true
        playMusic()
        currentMusicResourceId = newMusicResourceId
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun isLooping(): Boolean {
        return mediaPlayer?.isLooping ?: false
    }

    fun setVolume(leftVolume: Float, rightVolume: Float) {
        mediaPlayer?.setVolume(leftVolume, rightVolume)
    }

    fun playPopSound(context: Context) {
        val popSoundPlayer = MediaPlayer()

        popSoundPlayer.setOnPreparedListener { player ->
            player.start()
        }

        popSoundPlayer.setOnCompletionListener { player ->
            player.release()
            popSoundPlayers.remove(player)
        }

        try {
            popSoundPlayer.setDataSource(context, Uri.parse("android.resource://" + context.packageName + "/" + R.raw.pop_sound))
            popSoundPlayer.prepare()
            popSoundPlayers.add(popSoundPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
            popSoundPlayer.release()
            popSoundPlayers.remove(popSoundPlayer)
        }
    }
}
