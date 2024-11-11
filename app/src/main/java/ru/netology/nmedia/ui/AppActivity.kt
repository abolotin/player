package ru.netology.nmedia.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import ru.netology.nmedia.R
import ru.netology.nmedia.adapters.OnInteractionListener
import ru.netology.nmedia.adapters.TracksAdapter
import ru.netology.nmedia.databinding.ActivityAppBinding
import ru.netology.nmedia.dto.Track
import ru.netology.nmedia.viewmodel.AppViewModel

class AppActivity : AppCompatActivity(R.layout.activity_app) {
    val viewModel : AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)


        lifecycle.addObserver(viewModel.mediaObserver)

        viewModel.state.observe(this) { state ->
            binding.progressBar.isVisible = state.isLoading
            if (state.isError) {
                Toast.makeText(this, "Ошибка загрузки альбома", Toast.LENGTH_SHORT).show()
            }
        }

        val adapter = TracksAdapter(object: OnInteractionListener {
            override fun onPlay(track: Track) {
                viewModel.play(track)
            }

            override fun onPause(track: Track) {
                viewModel.pause()
            }

        })
        binding.list.adapter = adapter

        viewModel.album.observe(this) {
            with(binding) {
                artist.text = it.artist
                album.text = it.artist
                genre.text = it.genre
            }
        }

        viewModel.tracks.observe(this) { tracks ->
            tracks?.let {
                adapter.setTracks(it.map { it.copy() })
                val isStopped = tracks.filter { it.isPlaying }.isEmpty()
                binding.pauseButton.isVisible = !isStopped
                binding.startButton.isVisible = isStopped
            }
        }

        binding.startButton.setOnClickListener {
            viewModel.tracks.value?.firstOrNull()?.let {
                viewModel.play(it)
            }
        }

        binding.pauseButton.setOnClickListener {
            viewModel.pause()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.pause()
    }
}



