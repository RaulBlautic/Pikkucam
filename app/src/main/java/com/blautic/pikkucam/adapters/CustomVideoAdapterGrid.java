package com.blautic.pikkucam.adapters;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.blautic.pikkucam.R;

public class CustomVideoAdapterGrid extends ArrayAdapter {

    private Activity context;
    private Long[] myVideoIds;

    public CustomVideoAdapterGrid(@NonNull Activity applicationContext, Long[] videoIds) {
        super(applicationContext, R.layout.grid_item, videoIds);
        this.context = applicationContext;
        this.myVideoIds = videoIds;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View item = convertView;

        LayoutInflater inflater = context.getLayoutInflater();

        if (convertView == null)
            item = inflater.inflate(R.layout.grid_item, parent, false);

        ImageView thumbnail = (ImageView) item.findViewById(R.id.imageViewGrid); // get the reference of ImageView

        ContentResolver crThumb = context.getContentResolver();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 0;
        options.inDensity = 0;

        Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(crThumb, myVideoIds[position], MediaStore.Video.Thumbnails.MICRO_KIND, options);

        if (curThumb == null) {
            thumbnail.setImageResource(R.drawable.deporte);
        } else {
            thumbnail.setImageBitmap(curThumb);
        }

        return item;
    }
}
