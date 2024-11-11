package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Album

interface MediaRepository {
    val data: LiveData<Album>

    suspend fun loadAlbum()
}