package ru.netology.nmedia.dto

import java.text.SimpleDateFormat
import java.util.Date

data class Track(
    val id: Long,
    val file: String,
    var isPlaying: Boolean = false,
    var trackInfo: TrackInfo? = null
) {
    companion object {
        private const val TRACK_BASE_URL =
            "https://raw.githubusercontent.com/netology-code/andad-homeworks/master/09_multimedia/data/"
    }

    fun getUrl() = TRACK_BASE_URL + file
}

data class TrackInfo(
    val artist: String = "",
    val album: String = "",
    val duration: Long = 0
) {
    val durationString : String
        get() : String {
            val df = SimpleDateFormat("mm:ss")
            return df.format(Date(duration))
        }
}