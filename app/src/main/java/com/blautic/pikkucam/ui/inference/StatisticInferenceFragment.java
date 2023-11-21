package com.blautic.pikkucam.ui.inference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.blautic.pikkucam.R;
import com.blautic.pikkucam.databinding.FragmentStadisticInferenceBinding;
import com.blautic.pikkucam.viewmodel.MainViewModel;
import com.blautic.pikkucam.viewmodel.TrainingViewModel;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;
import io.opencensus.resource.Resource;

@AndroidEntryPoint
public class StatisticInferenceFragment extends Fragment {

    FragmentStadisticInferenceBinding binding;

    TrainingViewModel trainingViewModel;

    MainViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        trainingViewModel = new ViewModelProvider(this).get(TrainingViewModel.class);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        binding = FragmentStadisticInferenceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}