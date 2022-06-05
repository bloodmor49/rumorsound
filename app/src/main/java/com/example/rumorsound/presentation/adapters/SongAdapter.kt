package com.example.rumorsound.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.rumorsound.databinding.SongInfoBinding
import com.example.rumorsound.domain.entities.Song

open class SongAdapter : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    private var onItemClickListener: ((Song) -> Unit)? = null

    class SongViewHolder(val binding: SongInfoBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var listOfSongs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SongViewHolder(SongInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = listOfSongs[position]
        with(holder.binding) {
            tvPrimary.text = song.title
            tvSecondary.text = song.subtitle
//            glide.load(song.imageUrl).into(ivItemImage)
            root.setOnClickListener {
                onItemClickListener?.let { click -> click(song) }
            }
        }
    }


    override fun getItemCount(): Int {
        return listOfSongs.size
    }

    fun setItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener
    }
}
