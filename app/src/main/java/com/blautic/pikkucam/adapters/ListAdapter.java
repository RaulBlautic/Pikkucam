package com.blautic.pikkucam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blautic.pikkucam.R;
import com.blautic.pikkucam.databinding.SimpleListItemIaBinding;

import java.util.List;



public class ListAdapter extends ArrayAdapter<ModelCustomPair> {

    private int resourceLayout;
    private Context mContext;
    List<ModelCustomPair> items;

    public ListAdapter(@NonNull Context context, int resource, @NonNull List<ModelCustomPair> items) {
        super(context, resource, items);
        this.mContext = context;
        this.resourceLayout = resource;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
       SimpleListItemIaBinding binding = SimpleListItemIaBinding.inflate(LayoutInflater.from(getContext()), null, false);

        if (items.get(position).enable){
            binding.isRecording.setVisibility(View.VISIBLE);
        }else {
            binding.isRecording.setVisibility(View.INVISIBLE);
        }

        binding.titulo.setText(items.get(position).movement.getFldSLabel());

        return binding.getRoot().getRootView();
    }
}
