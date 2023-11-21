package com.blautic.pikkucam.adapters.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blautic.pikkucam.api.response.Model
import com.blautic.pikkucam.api.response.entity.Positions
import com.blautic.pikkucam.databinding.ListActivityItemBinding
import com.blautic.pikkucam.viewmodel.MainViewModel


class ItemModelsAdapter(
    private val onSelect: (Model) -> Unit,
    private val onRemove: (Model) -> Unit,
) : ListAdapter<Model, ItemModelsAdapter.ViewHolder>(ModelDiffCallback) {

    private val selectedItems = mutableListOf<Model>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val binding = ListActivityItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model)

        holder.bindingHolder.check.isChecked = selectedItems.contains(model)
    }

    inner class ViewHolder(
        val bindingHolder: ListActivityItemBinding,
        val context: Context,
    ) : RecyclerView.ViewHolder(bindingHolder.root) {

        fun bind(model: Model) {
            bindingHolder.modelName.text = model.fldSName
            bindingHolder.modelPosition.text = "Ubicación: ${Positions.values()[model.devices[0].fkPosition].tittle}"
            bindingHolder.modelDate.text = "Creado: ${model.fldDTimeCreateTime}"

            bindingHolder.check.setOnCheckedChangeListener(null)
            bindingHolder.check.isChecked = selectedItems.contains(model)

            bindingHolder.check.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(model)
                    onSelect(model)
                } else {
                    selectedItems.remove(model)
                    onRemove(model)
                }
            }
        }
    }
}


object ModelDiffCallback : DiffUtil.ItemCallback<Model>() {

    override fun areItemsTheSame(oldItem: Model, newItem: Model): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Model, newItem: Model): Boolean {
        return oldItem.id == newItem.id
    }

}