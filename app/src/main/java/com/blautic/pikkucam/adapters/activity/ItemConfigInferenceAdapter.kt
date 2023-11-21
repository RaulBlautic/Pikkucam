package com.blautic.pikkucam.adapters.activity

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blautic.pikkucam.R
import com.blautic.pikkucam.api.response.Model
import com.blautic.pikkucam.api.response.entity.Positions
import com.blautic.pikkucam.databinding.ListActivityItemBinding
import com.blautic.pikkucam.databinding.ListConfigInferenceItemBinding
import com.blautic.pikkucam.viewmodel.MainViewModel
import okhttp3.internal.notifyAll

class ItemConfigInferenceAdapter(
    private val onSelect: (Model, Int) -> Unit,
    private val onRemove: (Model, Int) -> Unit,
    private val onSliderChange: (Float, Int) -> Unit,
    private val onSwitchChange: (Boolean, Int) -> Unit,
    private val viewModel: MainViewModel,
) : ListAdapter<Model, ItemConfigInferenceAdapter.ViewHolder>(ModelDiffCallback) {
    private val selectedItems = mutableSetOf<Model>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val binding = ListConfigInferenceItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = getItem(position)
        holder.bind(model)
    }

    inner class ViewHolder(
        val bindingHolder: ListConfigInferenceItemBinding,
        val context: Context,
    ) : RecyclerView.ViewHolder(bindingHolder.root) {

        fun bind(model: Model) {

            //MODEL NAME
            bindingHolder.modelName.text = model.fldSName

            //INFERENCE
            viewModel.resultInferences.observeForever {
                if (viewModel.isSessionActive.value == true){
                    if (model.movements[0].fldSLabel < "Other") {
                        bindingHolder.correctInference.text = "${it[this.adapterPosition][0].toInt()}%"
                        bindingHolder.incorrectInference.text =
                            "${it[this.adapterPosition][1].toInt()}%"
                    } else {
                        bindingHolder.correctInference.text = "${it[this.adapterPosition][1].toInt()}%"
                        bindingHolder.incorrectInference.text =
                            "${it[this.adapterPosition][0].toInt()}%"
                    }
                }
            }

            //SLIDER
            bindingHolder.umbralInference.text =
                "${viewModel.thresholdInferences.value!![this.adapterPosition].toInt()}"
            bindingHolder.slider.value = viewModel.thresholdInferences.value!![this.adapterPosition]
            bindingHolder.slider.addOnChangeListener { slider, value, fromUser ->
                bindingHolder.umbralInference.text = "${value.toInt()}"
                onSliderChange(value, this.adapterPosition)
            }


            //SWITCH
            bindingHolder.check.isChecked = viewModel.captureTry.value!![this.adapterPosition]

            bindingHolder.check.setOnCheckedChangeListener { compoundButton, isChecked ->
                onSwitchChange(isChecked, this.adapterPosition)
            }

            viewModel.captureTry.observeForever {

                Log.d("VALUE SWITCH", it[adapterPosition].toString())

                if (it[this.adapterPosition]){
                    bindingHolder.check.thumbTintList =
                        context.resources.getColorStateList(R.color.PIKKU_GREEN)
                    bindingHolder.check.trackTintList =
                        context.resources.getColorStateList(R.color.PIKKU_GREEN_LIGHT)
                } else {
                    bindingHolder.check.thumbTintList =
                        context.resources.getColorStateList(R.color.PIKKU_RED)
                    bindingHolder.check.trackTintList =
                        context.resources.getColorStateList(R.color.PIKKU_RED_LIGTH)
                }
            }


        }

    }

}
