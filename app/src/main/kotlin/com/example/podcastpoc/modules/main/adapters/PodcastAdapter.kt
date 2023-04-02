package com.example.podcastpoc.modules.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.podcastpoc.databinding.ItemPodcastBinding
import com.example.podcastpoc.modules.main.data.model.PodcastDataModel
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.RequestManager
import javax.inject.Inject

class PodcastAdapter @Inject constructor(
    private val glide: RequestManager
) : RecyclerView.Adapter<PodcastAdapter.PodcastHolder>() {

    class PodcastHolder(val binding: ItemPodcastBinding) : RecyclerView.ViewHolder(binding.root)

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPodcastBinding.inflate(layoutInflater, parent, false)
        return PodcastHolder(binding)
    }

    override fun onBindViewHolder(holder: PodcastHolder, position: Int) {
        holder.binding.apply {
            val data = podcasts[position]
            titleTv.text = data.title
            listenersTv.text = "${data.totalListeners} Listeners"
//            iconIv.setImage(data.url)
            glide.load(data.icon).into(iconIv)
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
//
//class (
//    private val data: PodcastDataModel
//) :
//    RecyclerView.Adapter<PodcastHolder>() {
////
////    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastHolder {
////        val layoutInflater = LayoutInflater.from(parent.context)
////        val binding = ItemPodcastBinding.inflate(layoutInflater, parent, false)
////        return PodcastHolder(binding)
////    }
//
//    override fun onBindViewHolder(holder: PodcastHolder, position: Int) {
//        holder.binding.apply {
//            val data = data[position]
//            titleTv.text = data.title
//            listenersTv.text = "${data.totalListeners} Listeners"
//            iconIv.setImage(data.url)
//        }
//
//    }
//
//    override fun getItemCount() = data.size
//}

//class PodcastHolder(val binding: ItemPodcastBinding) : RecyclerView.ViewHolder(binding.root)