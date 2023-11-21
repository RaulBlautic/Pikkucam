package com.blautic.pikkucam.ui.training;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blautic.pikkucam.ble.BlueREMDevice;
import com.blautic.pikkucam.databinding.FragmentInferenceBinding;
import com.blautic.pikkucam.viewmodel.MainViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import timber.log.Timber;


public class InferenceFragment extends Fragment {

    FragmentInferenceBinding binding;
   /* MainViewModel viewModel;
    Movement movement;
    Model model;

    private MotionDetector motionDetector;
    private int counterCorrect;
*/
/*
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            motionDetector.onMpuChanged(intent.getParcelableExtra("data"));
        }
    };
*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInferenceBinding.inflate(inflater, container, false);
        //viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

       // viewModel.setCameraStateLiveData(true);

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

     /*   if (this.getArguments() != null) {
            movement = (Movement) this.getArguments().get("Movement");
            model = (Model) this.getArguments().get("Model");
            initMotionDetector(model);
        }

        binding.tittleMovement.setText(movement.label);

        binding.closeButton.setOnClickListener(view1 -> {
            Navigation.findNavController(view).navigateUp();
        });

        binding.slider.addOnChangeListener((slider, value, fromUser) -> {
            binding.threshold.setText((int) value + " %" );
            motionDetector.setThreshold(value);
        });*/

    }

    /*public void initMotionDetector(Model model){
        motionDetector = new MotionDetector(model, requireActivity());

        counterCorrect = 0;
        List<String> movements = new ArrayList<>();
        movements.add(movement.label);

        motionDetector.setSelectMov(movements);

        motionDetector.setMotionDetectorListener(new MotionDetector.MotionDetectorListener() {
            @Override
            public void onCorrectMotionRecognized(String movRecognized, float correctProb) {
                counterCorrect++;
                binding.correctText.setText(counterCorrect + "");
            }

            @Override
            public void onOutputScores(float[] outputScores) {
                for (int i = 0; i < outputScores.length; i++) {
                    Comparator<Movement> comparadorMultiple= Comparator.comparing(Movement::getLabel);
                    List<Movement> sortedMov = Arrays.stream(model.movements).sorted(comparadorMultiple).collect(Collectors.toList());
                    Timber.d("%s %s",sortedMov.get(i).label, outputScores[i]);
                    if (sortedMov.get(i).label.equals(binding.tittleMovement.getText())){
                        binding.sliderCorrect.setValue(outputScores[i]);
                        binding.correctValue.setText((int) outputScores[i] + "%");
                    }
                    if (sortedMov.get(i).label.equals("other")){
                        binding.sliderOther.setValue(outputScores[i]);
                        binding.otherValue.setText((int)  outputScores[i] + "%");
                    }
                }
            }

            @Override
            public void onTry(String movRecognized, float correctProb) {

            }
        });

        motionDetector.start();

        IntentFilter Filter = new IntentFilter(BlueREMDevice.BLE_READ_SENSOR);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver, Filter);

    }

    @Override
    public void onDestroyView() {
        viewModel.setCameraStateLiveData(false);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
        motionDetector.stop();
    }*/
}