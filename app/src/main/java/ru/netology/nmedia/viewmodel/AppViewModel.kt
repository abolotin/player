package ru.netology.nmedia.viewmodel

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.netology.nmedia.dto.Track
import ru.netology.nmedia.dto.TrackInfo
import ru.netology.nmedia.repository.MediaRepositoryImpl
import ru.netology.nmedia.ui.MediaLifecycleObserver

class AppViewModel : ViewModel() {
    val mediaObserver = MediaLifecycleObserver()
    private val repository = MediaRepositoryImpl()
    private val _state = MutableLiveData(State(State.Status.READY))

    val state: LiveData<State>
        get() = _state

    val album = repository.data
    val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> = _tracks

    class State(val status: Status) {
        enum class Status {
            READY,
            LOADING,
            ERROR
        }

        val isReady
            get() = status == Status.READY
        val isLoading
            get() = status == Status.LOADING
        val isError
            get() = status == Status.ERROR
    }

    init {
        viewModelScope.launch {
            _state.postValue(State(State.Status.LOADING))
            try {
                repository.loadAlbum()
                repository.data.value?.tracks?.let {
                    _tracks.postValue(it)
                }
                _state.postValue(State(State.Status.READY))
            } catch (e: Exception) {
                println(e.message)
                _state.postValue(State(State.Status.ERROR))
            }
        }

        tracks.observeForever { list ->
            list?.let {
                viewModelScope.launch {
                    it.filter { it.trackInfo == null }.firstOrNull()?.let { track ->
                        withContext(Dispatchers.IO) {
                            val retriver = MediaMetadataRetriever()
                            retriver.setDataSource(track.getUrl())
                            track.trackInfo = TrackInfo(
                                artist = retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                    ?: "",
                                album = retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                                    ?: "",
                                duration = retriver.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                    ?.toLong() ?: 0L
                            )
                        }
                        _tracks.postValue(tracks.value)
                    }
                }
            }
        }
    }

    private fun preparePlayer() : MediaPlayer {
        return MediaPlayer().apply {
            setOnPreparedListener {
                it.start()
            }

            setOnCompletionListener {
                stop()
                release()
                mediaObserver.player = null
                val list = tracks.value
                val playedIndex = list?.indexOfFirst { it.isPlaying }
                if ((playedIndex == null) || (playedIndex == -1)) {
                    // No playing ??? Starting first track
                    list?.firstOrNull()?.let {
                        it.isPlaying = true
                        play(it)
                    }
                } else {
                    if (playedIndex >= list.lastIndex) {
                        // Playing first track
                        list.firstOrNull()?.let {
                            it.isPlaying = true
                            play(it)
                        }
                    } else {
                        // Playing next track
                        list[playedIndex + 1].let {
                            it.isPlaying = true
                            play(it)
                        }
                    }

                }
            }
        }
    }

    fun play(track: Track) {
        if (mediaObserver.player == null) {
            mediaObserver.player = preparePlayer()
        }

        tracks.value?.forEach {
            if (it.id == track.id) it.isPlaying = true
            else it.isPlaying = false
        }
        _tracks.postValue(tracks.value)

        mediaObserver.player?.let {
            it.stop()
            it.reset()
            it.setDataSource(track.getUrl())
            it.prepareAsync()
        }
    }

    fun pause() {
        tracks.value?.forEach {
            it.isPlaying = false
        }
        _tracks.postValue(tracks.value)

        mediaObserver.player?.pause()
    }
}