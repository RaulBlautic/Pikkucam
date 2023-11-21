package com.blautic.pikkucam.video;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;

import com.blautic.pikkucam.R;

import java.io.File;

public class PlayVideoFragment extends Fragment
{


    private static final String TAG = "PlayVideoFragment";

    private VideoView videoView;
    private MediaController mediaController;
    private OnPlayVideoListener mListener;


    public static PlayVideoFragment newInstance() {

        return new PlayVideoFragment();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_video, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        videoView = (VideoView) view.findViewById(R.id.playvideoView);

        mediaController = new
                MediaController(getActivity());
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

    }

    @Override
    public void onResume() {
        super.onResume();
      /*
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }*/
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    public void startVideo(String path) {
        Log.i(TAG, "Start VIDEO: " + path);
        File fplay = new File(path);
        if (fplay.exists())
        {
            videoView.setVideoPath(path);
            setListener();

            videoView.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
        stopVideo();
        videoView= null;
    }

    public void stopVideo()
    {
        videoView.suspend();
    }

    private void setListener() {

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer vmp) {
                if(mListener != null) mListener.onPlayVideoFinish();
            }
        });
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) return;
        if (activity instanceof OnPlayVideoListener) {
            mListener = (OnPlayVideoListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPlayVideoListener) {
            mListener = (OnPlayVideoListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public interface OnPlayVideoListener {

        public void onPlayVideoFinish();
    }



}

