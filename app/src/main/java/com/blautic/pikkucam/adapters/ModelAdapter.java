package com.blautic.pikkucam.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.blautic.pikkucam.api.response.Movement;
import com.blautic.pikkucam.databinding.ListCreateItemBinding;
import com.blautic.pikkucam.databinding.ListItemBinding;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.functions.Function1;

public final class ModelAdapter extends ListAdapter<ModelAdapter.CustomPair, ViewHolder> {

    public static final DiffUtil.ItemCallback<CustomPair> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<CustomPair>() {

                @Override
                public boolean areItemsTheSame(@NonNull CustomPair oldItem, @NonNull CustomPair newItem) {
                    return oldItem.movement.getId() == newItem.movement.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull CustomPair oldItem, @NonNull CustomPair newItem) {
                    return false;
                }
            };
    @NotNull
    private final Function1 onClick;

    public ModelAdapter(@NotNull Function1 onClick) {
        super(DIFF_CALLBACK);
        this.onClick = onClick;
    }

    @NotNull
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        if (viewType == ModelAdapter.TypeHolder.TYPE_CREATE.ordinal()) {
            ListCreateItemBinding listCreateItemBinding = ListCreateItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            viewHolder = new CreateViewHolder(listCreateItemBinding);
        } else {
            ListItemBinding listItemBinding = ListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            viewHolder = new ItemViewHolder(listItemBinding);
        }

        return viewHolder;
    }

    public int getItemViewType(int position) {
        return position == 0 ? ModelAdapter.TypeHolder.TYPE_CREATE.ordinal() : ModelAdapter.TypeHolder.TYPE_ITEM.ordinal();
    }

    public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
        int itemViewType = this.getItemViewType(position);
        ViewHolder viewHolder;
        if (itemViewType == ModelAdapter.TypeHolder.TYPE_CREATE.ordinal()) {
            viewHolder = holder;
            if (!(holder instanceof ModelAdapter.CreateViewHolder)) {
                viewHolder = null;
            }

            ModelAdapter.CreateViewHolder createViewHolder = (ModelAdapter.CreateViewHolder) viewHolder;
            if (createViewHolder != null) {
                createViewHolder.bind();
            }
        } else if (itemViewType == ModelAdapter.TypeHolder.TYPE_ITEM.ordinal()) {
            viewHolder = holder;
            if (!(holder instanceof ModelAdapter.ItemViewHolder)) {
                viewHolder = null;
            }

            ModelAdapter.ItemViewHolder var5 = (ModelAdapter.ItemViewHolder) viewHolder;
            if (var5 != null) {
                var5.bind(this.getItem(position));
            }
        }

    }

    @NotNull
    public final Function1 getOnClick() {
        return this.onClick;
    }


    public enum TypeHolder {
        TYPE_CREATE,
        TYPE_ITEM;
    }

    public static class CustomPair {
        public Boolean enable;
        public Movement movement;

        public CustomPair(Boolean enable, Movement movement) {
            this.enable = enable;
            this.movement = movement;
        }
    }

    public final class ItemViewHolder extends ViewHolder {
        @NotNull
        private final ListItemBinding binding;

        public ItemViewHolder(@NotNull ListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public final void bind(CustomPair movement) {
            TextView modelName = this.binding.modelName;
            modelName.setText(movement.movement.getFldSLabel());
            binding.fondo.setActivated(movement.enable);
            binding.cameraIcon.setVisibility(movement.enable ? View.VISIBLE : View.INVISIBLE);

            this.binding.getRoot().setOnClickListener(it -> {
                movement.enable = !movement.enable;

                binding.cameraIcon.setVisibility(movement.enable ? View.VISIBLE : View.INVISIBLE);

                binding.fondo.setActivated(movement.enable);
                ModelAdapter.this.getOnClick().invoke(movement);
            });
        }

        @NotNull
        public final ListItemBinding getBinding() {
            return this.binding;
        }
    }

    public final class CreateViewHolder extends ViewHolder {
        @NotNull
        private final ListCreateItemBinding binding;

        public CreateViewHolder(@NotNull ListCreateItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public final void bind() {
            this.binding.getRoot().setOnClickListener(it -> ModelAdapter.this.getOnClick().invoke(null));
        }

        @NotNull
        public final ListCreateItemBinding getBinding() {
            return this.binding;
        }
    }
}


