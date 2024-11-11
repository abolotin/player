package ru.netology.nmedia.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.databinding.ListItemBinding
import ru.netology.nmedia.dto.Track

interface OnInteractionListener {
    fun onPlay(track: Track)
    fun onPause(track: Track)
}

class TracksAdapter(
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.Adapter<TracksAdapter.ViewHolder>() {
    private val diffUtil = object : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            if (oldItem.id == newItem.id) return true
            if (oldItem.isPlaying == newItem.isPlaying) return true
            if (oldItem.trackInfo == newItem.trackInfo) return true
            return false
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem == newItem
        }
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    class ViewHolder(
        private val binding: ListItemBinding,
        private val onInteractionListener: OnInteractionListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(track: Track) {
            with(binding) {
                title.text = track.file
                startButton.isVisible = !track.isPlaying
                pauseButton.isVisible = track.isPlaying

                title.text = track.trackInfo?.artist ?: track.file
                duration.text = track.trackInfo?.durationString ?: ""

                startButton.setOnClickListener {
                    onInteractionListener.onPlay(track)
                }
                pauseButton.setOnClickListener {
                    onInteractionListener.onPause(track)
                }
            }
        }
    }

    fun setTracks(tracks: List<Track>) {
        asyncListDiffer.submitList(tracks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding = binding, onInteractionListener = onInteractionListener)
    }

    override fun getItemCount() = asyncListDiffer.currentList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(asyncListDiffer.currentList[position])
    }
}