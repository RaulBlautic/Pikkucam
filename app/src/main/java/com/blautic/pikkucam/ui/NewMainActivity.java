package com.blautic.pikkucam.ui;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.blautic.pikkucam.R;
import com.blautic.pikkucam.databinding.ActivityNewMainBinding;
import com.blautic.pikkucam.service.FullService;
import com.blautic.pikkucam.ui.configactivity.ActivitySelectionFragment;
import com.blautic.pikkucam.ui.configactivity.ContentActivityFragment;
import com.blautic.pikkucam.ui.galery.GalleryFragment;
import com.blautic.pikkucam.ui.inference.InfoInferenceFragment;
import com.blautic.pikkucam.video.OnTrimVideoListener;
import com.blautic.pikkucam.video.PlayVideoFragment;
import com.blautic.pikkucam.viewmodel.MainViewModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import dagger.hilt.android.AndroidEntryPoint;
import pub.devrel.easypermissions.EasyPermissions;
import timber.log.Timber;

@AndroidEntryPoint
public class NewMainActivity extends AppCompatActivity implements PlayVideoFragment.OnPlayVideoListener, OnTrimVideoListener {

    public static final String[] BLUETOOTH_PERMISSIONS_S = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT};

    private static final String[] APP_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.MANAGE_DOCUMENTS
    };

    private static final String[] APP_PERMISSIONS_ANDROID13 = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_DOCUMENTS,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };
    private static final int REQUEST_PERMISSION = 1;
    public boolean permissionCamera = false;
    public boolean permissionRecordAudio = false;
    public boolean permissionFineLocation = false;
    public boolean permissionStorage = false;
    public boolean permissionReadStorage = false;
    public boolean requestedLocationService = false;
    public String logoName_h = "watermark_h.png";
    public String logoName_m = "watermark_m.png";
    public String logoName_l = "watermark_l.png";
    public String BASE_PATH = "/Pikkucam/";
    public String INTERNAL_PATH = "";
    public String FILES_PATH = "";
    boolean mIsBound = false;
    String TAG = "NEW_MAIN_ACTIVITY";
    private ActivityNewMainBinding binding;

    private MainViewModel viewModel;
    private FullService mBoundService;
    /*************************** SERVICE ************************************************/
    ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mBoundService = ((FullService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            mBoundService = null;
        }

    };
    private String advFileSet = "pikku_magenta.png";
    private String advFileGame = "pikku_blue.png";
    private String advFileCoach = "pikku_sponsor.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.bluetooth_no_soportado));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            builder.show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth is not enabled
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.bluetooth_desactivado));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                builder.show();
            }

        }

        //setRequestedOrientation(SCREEN_ORIENTATION_USER_LANDSCAPE);
        BASE_PATH = getFilesDir() + "/" + "Pikku";
        INTERNAL_PATH = getFilesDir() + "/";
        FILES_PATH = getFilesDir() + "/Pikkucam/AdvertisingFiles/";
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Log.d(TAG, "NewMainActivity onCreate");

        if (Build.VERSION.SDK_INT >= 33) {
            checkForKeyPermission13();
        } else {
            checkForKeyPermission();
        }

        StartFullService();
        doBindService();
    }

    @Override
    protected void onDestroy() {
        // super.onDestroy();
        if (mBoundService != null) mBoundService.stopService();
        mBoundService = null;
        viewModel = null;
        super.onDestroy();
    }

    public void StartFullService() {

        startService(new Intent(NewMainActivity.this, FullService.class));
    }

    void doBindService() {
        bindService(new Intent(NewMainActivity.this, FullService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;

    }

    private void checkForKeyPermission() {
        boolean request = false;
        //Camera
        boolean pCamera = false;
        pCamera = ContextCompat.checkSelfPermission(this, APP_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED;
        if (!pCamera) request = true;
        permissionCamera = pCamera;

        //Audio
        boolean pAudio = false;
        pAudio = ContextCompat.checkSelfPermission(this, APP_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
        if (!pAudio) request = true;
        permissionRecordAudio = pAudio;


        //Storage
        boolean pStorage = false;
        pStorage = ContextCompat.checkSelfPermission(this, APP_PERMISSIONS[2]) == PackageManager.PERMISSION_GRANTED;
        if (!pStorage) request = true;
        permissionStorage = pStorage;

        //Location
        boolean pLocation = false;
        pLocation = ContextCompat.checkSelfPermission(this, APP_PERMISSIONS[3]) == PackageManager.PERMISSION_GRANTED;
        if (!pLocation) request = true;
        permissionFineLocation = pLocation;

        boolean pFineLocation = false;
        pFineLocation = ContextCompat.checkSelfPermission(this, APP_PERMISSIONS[4]) == PackageManager.PERMISSION_GRANTED;
        if (!pFineLocation) request = true;
        permissionFineLocation = pFineLocation;

        boolean pReadStorage = false;
        pReadStorage = ContextCompat.checkSelfPermission(this, APP_PERMISSIONS[6]) == PackageManager.PERMISSION_GRANTED;
        if (!pReadStorage) request = true;
        permissionReadStorage = pReadStorage;

        if (request) {
            ActivityCompat.requestPermissions(this,
                    APP_PERMISSIONS,
                    REQUEST_PERMISSION);

        } else {
            binding = ActivityNewMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            createDefaultAdvert();
            viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        }


    }


    private void checkForKeyPermission13() {
        boolean request = false;
        //Camera
        boolean pCamera = false;
        pCamera = ContextCompat.checkSelfPermission(this, APP_PERMISSIONS_ANDROID13[0]) == PackageManager.PERMISSION_GRANTED;
        if (!pCamera) request = true;
        permissionCamera = pCamera;

        //Audio
        boolean pAudio = false;
        pAudio = ContextCompat.checkSelfPermission(this, APP_PERMISSIONS_ANDROID13[1]) == PackageManager.PERMISSION_GRANTED;
        if (!pAudio) request = true;
        permissionRecordAudio = pAudio;


        //Location
        boolean pLocation = false;
        pLocation = ContextCompat.checkSelfPermission(this, APP_PERMISSIONS_ANDROID13[3]) == PackageManager.PERMISSION_GRANTED;
        if (!pLocation) request = true;
        permissionFineLocation = pLocation;

        boolean pFineLocation = false;
        pFineLocation = ContextCompat.checkSelfPermission(this, APP_PERMISSIONS_ANDROID13[4]) == PackageManager.PERMISSION_GRANTED;
        if (!pFineLocation) request = true;
        permissionFineLocation = pFineLocation;

        boolean pManageStorage = false;
        pManageStorage = ContextCompat.checkSelfPermission(this, APP_PERMISSIONS_ANDROID13[7]) == PackageManager.PERMISSION_GRANTED;
        if (!pManageStorage) request = true;

        boolean pVideoStorage = false;
        pVideoStorage = ContextCompat.checkSelfPermission(this, APP_PERMISSIONS_ANDROID13[9]) == PackageManager.PERMISSION_GRANTED;
        if (!pVideoStorage) request = true;

        if (request) {
            ActivityCompat.requestPermissions(this,
                    APP_PERMISSIONS_ANDROID13,
                    REQUEST_PERMISSION);

        } else {
            binding = ActivityNewMainBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            createDefaultAdvert();
            viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, APP_PERMISSIONS_ANDROID13[0]) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, APP_PERMISSIONS_ANDROID13[1]) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, APP_PERMISSIONS_ANDROID13[3]) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, APP_PERMISSIONS_ANDROID13[4]) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, APP_PERMISSIONS_ANDROID13[9]) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.Concede_permisos, Toast.LENGTH_LONG).show();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
            } else {
                binding = ActivityNewMainBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                viewModel = new ViewModelProvider(this).get(MainViewModel.class);
                createDefaultAdvert();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, APP_PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, APP_PERMISSIONS[1]) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, APP_PERMISSIONS[2]) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, APP_PERMISSIONS[3]) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, APP_PERMISSIONS[4]) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, APP_PERMISSIONS[6]) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.Concede_permisos, Toast.LENGTH_LONG).show();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
            } else {
                binding = ActivityNewMainBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                viewModel = new ViewModelProvider(this).get(MainViewModel.class);
                createDefaultAdvert();
            }
        }

    }

    @Override
    public void onTrimStarted() {

    }

    @Override
    public void getResult(Uri uri) {

    }

    @Override
    public void cancelAction() {

    }

    @Override
    public void onError(String message) {

    }

    @Override
    public void onPlayVideoFinish() {

    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void createDefaultAdvert() {
        File defFile = new File(FILES_PATH);

        if (!defFile.exists()) {
            defFile.mkdir();
        }

        File[] files = defFile.listFiles();

        //if(files.length < 2)
        {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pikku_magenta);
            String fileName = advFileSet;
            File dest = new File(FILES_PATH, fileName);
            if (!dest.exists()) {
                try {
                    FileOutputStream out;
                    out = new FileOutputStream(dest);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {

                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pikku_blue);
            fileName = advFileGame;
            dest = new File(FILES_PATH, fileName);
            if (!dest.exists()) {
                try {
                    FileOutputStream out;
                    out = new FileOutputStream(dest);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {

                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pikku_sponsor);
            fileName = advFileCoach;
            dest = new File(FILES_PATH, fileName);
            if (!dest.exists()) {
                try {
                    FileOutputStream out;
                    out = new FileOutputStream(dest);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {

                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.watermark_h);
            fileName = logoName_h;
            dest = new File(FILES_PATH, fileName);
            if (!dest.exists()) {
                try {
                    FileOutputStream out;
                    out = new FileOutputStream(dest);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {

                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.watermark_m);
            fileName = logoName_m;
            dest = new File(FILES_PATH, fileName);
            if (!dest.exists()) {
                try {
                    FileOutputStream out;
                    out = new FileOutputStream(dest);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {

                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.watermark_l);
            fileName = logoName_l;
            dest = new File(FILES_PATH, fileName);
            if (!dest.exists()) {
                try {
                    FileOutputStream out;
                    out = new FileOutputStream(dest);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {

                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {

        boolean back = true;

        for (Fragment navHostFragment : getSupportFragmentManager().getFragments()) {
            for (Fragment cameraFragment : navHostFragment.getChildFragmentManager().getFragments()) {
                for (Fragment f : cameraFragment.getChildFragmentManager().getFragments()) {
                    if (f instanceof SettingsFragment) {
                        back = false;
                        ((SettingsFragment) f).removeFragment();
                    } else if (f instanceof InfoFragment) {
                        back = false;
                        ((InfoFragment) f).removeFragment();
                    } else if (f instanceof GalleryFragment) {
                        back = false;
                        ((GalleryFragment) f).removeFragment();
                    } else if (f instanceof ConnectFragment) {
                        back = false;
                        ((ConnectFragment) f).removeFragment();
                    } else if (f instanceof ContentActivityFragment) {
                        back = false;
                        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity);
                        if (navController.getCurrentDestination().getId() == R.id.loginActivityFragment) {
                            ((ContentActivityFragment) f).removeFragment();
                        }
                        navController.navigateUp();
                    } else if (f instanceof InfoInferenceFragment) {
                        back = false;
                        ((InfoInferenceFragment) f).removeFragment();
                    } else if (f instanceof SecondFragment) {
                        back = false;
                    }
                }
            }
        }

        if (back) {
            super.onBackPressed();
        }
    }

}