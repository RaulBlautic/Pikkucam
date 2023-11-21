package com.blautic.pikkucam.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blautic.pikkucam.R
import com.blautic.pikkucam.databinding.ListItemSessionBinding
import com.blautic.pikkucam.model.VideoMetadata

class GalleryAdapter(
    private val onClick: (VideoMetadata) -> Unit,
    private val onDelete: () -> Unit,
    var context: Context,
    private var myVideoIds: List<Long>,
    private var originalList: List<VideoMetadata>
) :
    ListAdapter<VideoMetadata, GalleryAdapter.ViewHolder>(SessionDiffCallback) {

    var selectedItems = mutableListOf<VideoMetadata>()
    var selectAll = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemSessionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            context = context
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))

        if (selectedItems.contains(getItem(position))) {
            holder.bindingHolder.videoSelect.backgroundTintList =
                context.resources.getColorStateList(R.color.PIKKU_ORANGE)
        } else {
            holder.bindingHolder.videoSelect.backgroundTintList =
                context.resources.getColorStateList(R.color.WHITE)
        }

        if (selectAll) {
            holder.bindingHolder.videoSelect.backgroundTintList =
                context.resources.getColorStateList(R.color.PIKKU_ORANGE)
        } else {
            holder.bindingHolder.videoSelect.backgroundTintList =
                context.resources.getColorStateList(R.color.WHITE)
        }
    }


    fun filterByAscending(query: String, list: List<VideoMetadata>?){
        if (query.isEmpty()){
            val results = list?.sortedByDescending {
                it.date
            }
            myVideoIds = myVideoIds.sortedByDescending { it }
            originalList = results ?: emptyList()
            selectedItems.clear()
            super.submitList(originalList)
            notifyDataSetChanged()
        } else {
            val results = list?.filter {
                it.subtitle.lowercase().contains(query.toLowerCase(), false)
            }?.sortedByDescending {
                it.date
            }
            myVideoIds = myVideoIds.sortedByDescending { it }
            originalList = results ?: emptyList()
            selectedItems.clear()
            super.submitList(originalList)
            notifyDataSetChanged()
        }
    }

    fun filterByDescending(query: String, list: List<VideoMetadata>?){
        if (query.isEmpty()){
            val results = list?.sortedBy {
                it.date
            }
            myVideoIds = myVideoIds.sortedBy { it }
            originalList = results ?: emptyList()
            selectedItems.clear()
            super.submitList(originalList)
            notifyDataSetChanged()
        }else {
            val results = list?.filter {
                it.subtitle.lowercase().contains(query.toLowerCase(), false)
            }?.sortedBy {
                it.date
            }
            myVideoIds = myVideoIds.sortedBy { it }
            originalList = results ?: emptyList()
            selectedItems.clear()
            super.submitList(originalList)
            notifyDataSetChanged()
        }
    }

    fun filterByName(query: String, list: List<VideoMetadata>?) {
        val results = list?.filter {
            it.subtitle.toLowerCase().contains(query.toLowerCase(), ignoreCase = true)
        }
        originalList = results ?: emptyList()
        selectedItems.clear()
        super.submitList(originalList)
        notifyDataSetChanged()
    }

    override fun submitList(list: List<VideoMetadata>?) {
        originalList = list ?: emptyList()
        selectedItems.clear()
        super.submitList(list)
        notifyDataSetChanged()
    }

    fun selectAll(isSelected: Boolean) {
        selectAll = isSelected
        selectedItems.clear()
        if (isSelected) {
            selectedItems.addAll(currentList)
        } else {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder constructor(
        var bindingHolder: ListItemSessionBinding,
        var context: Context,
    ) :
        RecyclerView.ViewHolder(bindingHolder.root) {

        fun bind(video: VideoMetadata) {

            if (selectedItems.contains(video)) {
                bindingHolder.videoSelect.backgroundTintList =
                    context.resources.getColorStateList(R.color.PIKKU_ORANGE)
            } else {
                bindingHolder.videoSelect.backgroundTintList =
                    context.resources.getColorStateList(R.color.WHITE)
            }

            bindingHolder.videoSelect.setOnClickListener {
                if (selectedItems.contains(video)) {
                    bindingHolder.videoSelect.backgroundTintList =
                        context.resources.getColorStateList(R.color.WHITE)
                    selectedItems.remove(video)
                    onDelete()
                } else {
                    bindingHolder.videoSelect.backgroundTintList =
                        context.resources.getColorStateList(R.color.PIKKU_ORANGE)
                    selectedItems.add(video)
                    onClick(video)
                }
            }

            bindingHolder.videoName.text = video.subtitle

            getThumb(video)
        }

        private fun getThumb(video: VideoMetadata) {
            val crThumb = context.contentResolver
            val options = BitmapFactory.Options()
            options.inSampleSize = 0
            options.inDensity = 50

            val curThumb = MediaStore.Video.Thumbnails.getThumbnail(crThumb,
               video.idThumb,
                MediaStore.Video.Thumbnails.MINI_KIND,
                options)

            if (curThumb == null) {
                bindingHolder.thumbnail.setImageResource(R.drawable.deporte)
            } else {
                bindingHolder.thumbnail.setImageBitmap(curThumb)
            }
        }
    }
}

object SessionDiffCallback : DiffUtil.ItemCallback<VideoMetadata>() {

    override fun areItemsTheSame(oldItem: VideoMetadata, newItem: VideoMetadata): Boolean {
        return false
    }

    override fun areContentsTheSame(oldItem: VideoMetadata, newItem: VideoMetadata): Boolean {
        return false
    }

}