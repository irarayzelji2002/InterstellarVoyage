package com.example.interstellarvoyage

interface MusicPlayerCallback {
    fun playMusic()
    fun pauseMusic()
    fun isPlaying(): Boolean
    fun changeMusic(newMusicResourceId: Int)
}