package com.example.podcastpoc.modules.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.podcastpoc.databinding.ItemSwipeBinding
import com.example.podcastpoc.modules.main.data.model.PodcastDataModel
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import javax.inject.Inject

class SwipePodcastAdapter @Inject constructor() : RecyclerView.Adapter<SwipePodcastAdapter.SwipePodcastHolder>() {

    class SwipePodcastHolder(val binding: ItemSwipeBinding) : RecyclerView.ViewHolder(binding.root)

    private val diffCallback = object : DiffUtil.ItemCallback<PodcastDataModel.Podcast>() {
        override fun areItemsTheSame(oldItem: PodcastDataModel.Podcast, newItem: PodcastDataModel.Podcast): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PodcastDataModel.Podcast, newItem: PodcastDataModel.Podcast): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var podcasts: List<PodcastDataModel.Podcast>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipePodcastHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSwipeBinding.inflate(layoutInflater, parent, false)
        return SwipePodcastHolder(binding)
    }

    override fun onBindViewHolder(holder: SwipePodcastHolder, position: Int) {
        holder.binding.apply {
            val data = podcasts[position]
            tvPrimary.text = data.title + ", " + data.subtitle + ", " + data.totalListeners
            tvPrimary.isSelected = true
            root.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(data)
                }
            }
        }
    }

    private var onItemClickListener: ((PodcastDataModel.Podcast) -> Unit)? = null

    fun setOnItemClickListener(listener: (PodcastDataModel.Podcast) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return podcasts.size
    }
}