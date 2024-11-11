package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.netology.nmedia.dto.Album
import java.util.concurrent.TimeUnit

class MediaRepositoryImpl : MediaRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<Album>() {}

    companion object {
        private const val ALBUM_URL =
            "https://github.com/netology-code/andad-homeworks/raw/master/09_multimedia/data/album.json"
        private val jsonType = "application/json".toMediaType()
    }

    private val _data: MutableLiveData<Album> = MutableLiveData()
    override val data: LiveData<Album>
        get() = _data

    override suspend fun loadAlbum() {
        val request: Request = Request.Builder()
            .url(ALBUM_URL)
            .build()

        withContext(Dispatchers.IO) {
            _data.postValue(
                client.newCall(request)
                    .execute()
                    .let {
                        it.body?.string() ?: throw RuntimeException("body is null")
                    }
                    .let {
                        gson.fromJson(it, typeToken.type)
                    }
            )
        }
    }
}