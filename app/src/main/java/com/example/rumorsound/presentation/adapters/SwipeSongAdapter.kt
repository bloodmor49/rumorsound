package com.example.rumorsound.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.rumorsound.databinding.SongInfoBinding
import com.example.rumorsound.databinding.SwipeInfoBinding
import com.example.rumorsound.domain.entities.Song
import javax.inject.Inject

class SwipeSongAdapter : RecyclerView.Adapter<SwipeSongAdapter.SwipeSongViewHolder>() {

    var onItemClickListener: ((Song) -> Unit)? = null

    private val diffCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    var listOfSongs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    class SwipeSongViewHolder(val binding: SwipeInfoBinding) : RecyclerView.ViewHolder(binding.root)

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SwipeSongViewHolder, position: Int) {
        val song = listOfSongs[position]
        with(holder.binding) {
            val text = song.title
            tvPrimary.text = text
            root.setOnClickListener {
                onItemClickListener?.let { click -> click(song) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SwipeSongViewHolder(SwipeInfoBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false))

    override fun getItemCount(): Int {
        return listOfSongs.size
    }

    fun setItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener
    }

}


