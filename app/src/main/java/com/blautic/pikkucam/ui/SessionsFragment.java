package com.blautic.pikkucam.ui;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.blautic.pikkucam.R;
import com.blautic.pikkucam.adapters.CustomVideoAdapterGrid;
import com.blautic.pikkucam.databinding.FragmentSessionsBinding;
import com.blautic.pikkucam.model.VideoMetadata;
import com.blautic.pikkucam.viewmodel.MainViewModel;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class SessionsFragment extends Fragment {

    private static final String TAG = "SessionsFragment";
    FragmentSessionsBinding binding;
    CameraFragment cameraFragment;
    SecondFragment secondFragment;
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();

                }
            });
    private ArrayList<Uri> videos;
    private ArrayList<String> videoNames;
    private ArrayList<Date> videoDates;
    private ArrayList<Long> videoIds;
    private List<VideoMetadata> listVideosWithMetadata;
    private CustomVideoAdapterGrid adapterGrid;
    private NewMainActivity act;
    private Uri selectedUri = null;
    private int OPEN_EXPLORER = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSessionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        act = (NewMainActivity) getActivity();
        MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        videoNames = new ArrayList<>();
        videoIds = new ArrayList<>();
        listVideosWithMetadata = new ArrayList<VideoMetadata>();
        cameraFragment = (CameraFragment) getParentFragment();
        secondFragment = (SecondFragment) cameraFragment.getChildFragmentManager().findFragmentByTag("SecondFragment");

        //videos = getVideos();
        initUI();

        binding.btnSearchVideoSession.callOnClick();
        binding.buttonPlaySessions.setEnabled(false);
        binding.imageViewBateria.setImageDrawable(secondFragment.binding.imageViewBateria.getDrawable());
        binding.imageViewConexion.setImageDrawable(secondFragment.binding.imageViewConexion.getDrawable());
        binding.buttonDeleteSessions.setOnClickListener(v -> {

            if (selectedUri != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(act);
                builder.setTitle(getString(R.string.video_eliminar));

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        if (Build.VERSION.SDK_INT >= 29) {

                            try {

                                act.getApplicationContext().getContentResolver().delete(selectedUri, null, null);
                                binding.btnSearchVideoSession.callOnClick();

                            } catch (SecurityException securityException) {

                                RecoverableSecurityException recoverableSecurityException;
                                if (securityException instanceof RecoverableSecurityException) {
                                    recoverableSecurityException =
                                            (RecoverableSecurityException) securityException;
                                } else {
                                    throw new RuntimeException(
                                            securityException.getMessage(), securityException);
                                }

                                IntentSender intentSender = recoverableSecurityException.getUserAction()
                                        .getActionIntent().getIntentSender();
                                try {
                                    startIntentSenderForResult(intentSender, 12, null, 0, 0, 0, null);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        } else {
                            act.getApplicationContext().getContentResolver().delete(selectedUri, null, null);
                            binding.btnSearchVideoSession.callOnClick();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        viewModel.getStepLiveData().observe(getViewLifecycleOwner(), step -> {

            if (step == 9) {
                binding.buttonSiguienteGaleria.setVisibility(View.VISIBLE);

            } else {
                binding.buttonSiguienteGaleria.setVisibility(View.GONE);
            }
        });

        binding.buttonSiguienteGaleria.setOnClickListener(view1 -> {
            viewModel.setStepLiveData(10);
            getActivity().onBackPressed();
        });

        binding.buttonOpenFileExplorer.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            intent.setData(imagesUri);
            startActivity(intent);
        });
    }

    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = act.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initUI() {

        String[] videoNamesArray = new String[videoNames.size()];
        Long[] videoIdsArray = new Long[videoIds.size()];
        ;
        videoNames.toArray(videoNamesArray);
        videoIds.toArray(videoIdsArray);

        adapterGrid = new CustomVideoAdapterGrid(act, videoIdsArray);
        binding.gridView.setAdapter(adapterGrid);

        binding.gridView.setOnItemClickListener((parent, view, position, id) ->
                readMetaData(videos.get(position), getFileName(videos.get(position))));

        binding.buttonPlaySessions.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, selectedUri);
            intent.setDataAndType(selectedUri, "video/mp4");
            startActivity(intent);
        });

        binding.btnSearchVideoSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoNames = new ArrayList<>();
                videoIds = new ArrayList<>();
                videoDates = new ArrayList<Date>();
                listVideosWithMetadata = new ArrayList<>();
                videos = getVideos();

                for (int i = 0; i < listVideosWithMetadata.size(); i++) {
                    /*boolean searchSlowmo = !rbSlowmoBoth.isChecked();
                    boolean isSlowmo = rbSlowmoSi.isChecked();*/
                    boolean searchSubtitulo = !binding.editTextSubtitle.getText().toString().equals("");
                    VideoMetadata file = listVideosWithMetadata.get(i);
                    Log.d(TAG, "File name" + listVideosWithMetadata.get(i).getName());

                    //Check subtitle
                    if (file.getSubtitle() == null) {
                        if (searchSubtitulo) {
                            videos.remove(videoNames.indexOf(file.getName()));
                            videoIds.remove(videoNames.indexOf(file.getName()));
                            videoNames.remove(file.getName());
                        }
                        continue;
                    } else if (searchSubtitulo && !file.getSubtitle().contains(binding.editTextSubtitle.getText().toString())) {
                        videos.remove(videoNames.indexOf(file.getName()));
                        videoIds.remove(videoNames.indexOf(file.getName()));
                        videoNames.remove(file.getName());
                        continue;
                    }

                }

                String[] videoDatesArray = new String[videoDates.size()];
                String[] videoNamesArray = new String[videoNames.size()];
                Long[] videoIdsArray = new Long[videoNames.size()];
                videoNames.toArray(videoNamesArray);
                videoIds.toArray(videoIdsArray);
                videoDates.toArray(videoDatesArray);
                try {
                    //videoIdsArray = orderArray(videoIdsArray,videoDatesArray);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                adapterGrid = new CustomVideoAdapterGrid(act, videoIdsArray);
                binding.gridView.setAdapter(adapterGrid);

                binding.tvVideoTitle.setText(null);
                binding.tvVideoProfile.setText(null);
                binding.tvVideoSlowmo.setText(null);
                binding.tvVideoFecha.setText(null);
                binding.tvVideoDuration.setText(null);
                binding.tvVideoQuality.setText(null);

                selectedUri = null;
            }
        });
    }

    public Long[] orderArray(Long[] arrayVids, String[] arrayDates) throws ParseException {

        Long[] localVideoIdsArray = new Long[arrayVids.length];

        ArrayList<Long> aux = new ArrayList<>();
        ArrayList<Uri> aux2 = new ArrayList<>();
        ArrayList<Date> dateAux = new ArrayList<>();


        for (int i = 0; i < arrayVids.length; i++) {
            try {
                Date date = new SimpleDateFormat("dd/MM/yyyy").parse(arrayDates[i]);
                String time = arrayDates[i].substring(12);
                Log.d(TAG, time);
                String time2 = time.substring(0, time.indexOf(":"));
                date.setHours(Integer.parseInt(time2));
                time = time.substring(time.indexOf(":") + 1);
                time2 = time.substring(0, 2);
                date.setMinutes(Integer.parseInt(time2));
                time = time.substring(time.lastIndexOf(":") + 1);
                date.setSeconds(Integer.parseInt(time));
                if (i == 0 || dateAux.isEmpty()) {
                    dateAux.add(date);
                    aux.add(arrayVids[0]);
                    aux2.add(videos.get(0));
                    continue;
                }
                for (int j = 0; j < dateAux.size(); j++) {
                    if (date.after(dateAux.get(j))) {
                        dateAux.add(j, date);
                        aux2.add(j, videos.get(i));
                        aux.add(j, arrayVids[i]);
                        break;
                    } else if (j == dateAux.size() - 1) {
                        aux2.add(videos.get(i));
                        aux.add(arrayVids[i]);
                        dateAux.add(date);
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                aux2.add(videos.get(i));
                aux.add(arrayVids[i]);
            }
        }

        aux.toArray(localVideoIdsArray);
        videos = aux2;

        return localVideoIdsArray;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<Uri> getVideos() {

        ArrayList<Uri> filteredFiles = new ArrayList<>();

        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE,
                MediaStore.MediaColumns.DATA,
        };

        String selection;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            selection = MediaStore.MediaColumns.RELATIVE_PATH + " LIKE ? ";
        } else {
            selection = MediaStore.Images.Media.DATA + " LIKE ? ";
        }

        //selection = MediaStore.Video.Media.DISPLAY_NAME +" LIKE ?";

        String[] selectionArgs = new String[]{"%PikkuCam%"};

        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        try {
            Cursor cursor = act.getApplicationContext().getContentResolver().query(
                    collection,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
            );

            // Cache column indices.
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                //int size = cursor.getInt(sizeColumn);

                File f = new File(cursor.getString(3));

                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);

                //boolean pikku_file=name.contains("pikku");

                boolean exists = f.exists();

                //if(name.contains("pikku") && f.exists())


                //readMetaData(contentUri, name);

                if (exists) {
                    filteredFiles.add(contentUri);
                    videoNames.add(name);
                    videoIds.add(id);
                }
            }
        } catch (Exception e) {
        }

        return filteredFiles;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void readMetaData(Uri file, String name) {

        File sdcard = Environment.getExternalStorageDirectory();

        Log.d(TAG, name);

        Log.d(TAG, file.getPath());

        selectedUri = file;
        binding.buttonPlaySessions.setEnabled(true);

        if (!file.getPath().isEmpty()) {
            Log.i(TAG, ".mp4 file Exist");

            //Added in API level 10
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(act, file);
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String profile = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String slowmo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
            //Date date = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);

            if (slowmo != null) {
                if (slowmo.equals("0") || slowmo.equals("Blues") || slowmo.equals("no")) {
                    slowmo = "No";
                } else {
                    slowmo = "Sí";
                }
            } else {
                slowmo = "No";
            }

            if (title != null) {
                if (title.equals("")) {
                    title = "--";
                }
            } else {
                title = "--";
            }

            //videoDates.add(date);

            int duration = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
            float longDuration = Float.parseFloat(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
            if (longDuration % 1 >= 0.5) {
                duration++;
            }

            String qualityValue = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
            String quality;
            if (qualityValue != null) {
                if (qualityValue.equals("0")) {
                    quality = getString(R.string.baja);
                } else if (qualityValue.equals("1")) {
                    quality = getString(R.string.media);
                } else {
                    quality = getString(R.string.alta);
                }
            } else {
                quality = "";
            }

            //VideoMetadata videoMetadata = new VideoMetadata(file, name, title, profile, slowmo, "date", duration, quality);
            //listVideosWithMetadata.add(videoMetadata);

            binding.tvVideoTitle.setText(title);
            binding.tvVideoProfile.setText(profile);
            binding.tvVideoSlowmo.setText(slowmo);
            binding.tvVideoFecha.setText("date");
            binding.tvVideoDuration.setText(duration + "sec");
            binding.tvVideoQuality.setText(quality);
        } else {
            Log.e(TAG, ".mp4 file doesn´t exist.");
        }
    }


    public void removeFragment() {
        FragmentTransaction transaction = cameraFragment.getChildFragmentManager().beginTransaction();
        transaction.remove(SessionsFragment.this);
        transaction.show(secondFragment);
        transaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12) {
            Log.d("Result Code is-->", String.valueOf(resultCode)); //This gives RESULT_OK
            if (resultCode == RESULT_OK) {
                ContentResolver contentResolver = act.getApplicationContext().getContentResolver();
                contentResolver.delete(selectedUri, null, null);
                binding.btnSearchVideoSession.callOnClick();
            }
        }

        if (requestCode == OPEN_EXPLORER) {

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("ONRESUME");
    }
}