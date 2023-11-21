package com.blautic.pikkucam.ui.userguide;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blautic.pikkucam.databinding.FragmentGuideBinding;
import com.blautic.pikkucam.ui.CameraFragment;
import com.blautic.pikkucam.viewmodel.MainViewModel;

public class GuideFragment extends DialogFragment {

    FragmentGuideBinding binding;
    MainViewModel viewModel;
    Fragment thisfragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        thisfragment = this;

        binding = FragmentGuideBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void initViewPager() {
        PlotsCollectionAdapter plotsCollectionAdapter = new PlotsCollectionAdapter(this);
        binding.viewPager.setAdapter(plotsCollectionAdapter);
        binding.viewPager.setUserInputEnabled(false);
        binding.viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setAlpha(0f);
                page.setVisibility(View.VISIBLE);

                // Start Animation for a short period of time
                page.animate()
                        .alpha(1f)
                        .setDuration(80);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        initViewPager();

        viewModel.getStepLiveData().observe(getViewLifecycleOwner(), step -> {
            Log.d("STEP", step+"");

            if(step == 5){
                getDialog().dismiss();
            }
            if (step == 7){
                getDialog().dismiss();
            }
            if (step == 9){
                getDialog().dismiss();
            }
            if(step == 12){
                getDialog().dismiss();
            }
                binding.viewPager.setCurrentItem(step);
        });

        binding.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                if (viewModel.getStepLiveData().getValue() > 10){
                    getDialog().dismiss();
                }else{
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                            .setTitle("Dialogo de Confirmación")
                            .setMessage("Estás seguro que quieres salir de la guía?")
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }).setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    getDialog().dismiss();
                                }
                            }).show();

                }
            }
        });
    }

    public static class PlotsCollectionAdapter extends FragmentStateAdapter {
        public PlotsCollectionAdapter(Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return FirstPageFragment.newInstance(1);
                case 1:
                    return FirstPageFragment.newInstance(2);
                case 2:
                    return FirstPageFragment.newInstance(3);
                case 3:
                    return FirstPageFragment.newInstance(4);
                case 4:
                    return FirstPageFragment.newInstance(5);
                case 5:
                    return FirstPageFragment.newInstance(6);
                case 6:
                    return FirstPageFragment.newInstance(7);
                case 7:
                    return FirstPageFragment.newInstance(8);
                case 8:
                    return FirstPageFragment.newInstance(9);
                case 9:
                    return FirstPageFragment.newInstance(10);
                case 10:
                    return FirstPageFragment.newInstance(11);
                case 11:
                    return FirstPageFragment.newInstance(12);
                default:
                    return FirstPageFragment.newInstance(11);
            }
        }

        @Override
        public int getItemCount() {
            return 12;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}

