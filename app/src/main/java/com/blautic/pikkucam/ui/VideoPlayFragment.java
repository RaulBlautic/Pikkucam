package com.blautic.pikkucam.ui;


import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.blautic.pikkucam.databinding.FragmentVideoPlayBinding;

public class VideoPlayFragment extends Fragment {

    String TAG = "VideoPlayFragment";
    FragmentVideoPlayBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVideoPlayBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    public void PlayVideo(String filePath){
        VideoPlayFragment thisFragment = this;

        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), Uri.parse(filePath));
        SurfaceView sv = binding.videoView;
        SurfaceHolder sh = sv.getHolder();

        mediaPlayer.setDisplay(sh);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                getParentFragment().getChildFragmentManager().beginTransaction().hide(thisFragment).commit();
            }
        });
        mediaPlayer.start();
    }
}