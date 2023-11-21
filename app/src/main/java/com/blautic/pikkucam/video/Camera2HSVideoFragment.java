/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blautic.pikkucam.video;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraConstrainedHighSpeedCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.blautic.pikkucam.R;
import com.blautic.pikkucam.cfg.CfgDef;
import com.blautic.pikkucam.db.ProfileSettings;
import com.blautic.pikkucam.db.ProfileSettingsDao;
import com.blautic.pikkucam.viewmodel.MainViewModel;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Camera2HSVideoFragment extends Fragment {

    public static final String ISO_DEFAULT = "auto";
    public static final int FRONT_CAMERA = CameraCharacteristics.LENS_FACING_FRONT;
    public static final int BACK_CAMERA = CameraCharacteristics.LENS_FACING_BACK;
    public static final int CAMERA_IDLE = 0;
    public static final int CAMERA_OPEN = 1;
    public static final int CAMERA_CAPTURING = 2;
    public static final int CAMERA_DISCONNECTED = 3;
    public static final int CAMERA_ERROR = -1;
    private static final String TAG = "Camera2Video";
    private static final String ARG_CAM2USE = "camera2use";
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final int SENSOR_ORIENTATION_SUP_WIDTH = 0;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray BACK_DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray BACK_INVERSE_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray SUP_WIDTH_ORIENTATIONS = new SparseIntArray();

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 180);//0;
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 0);//180
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 0);//0
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 180);//180
    }

    static {
        SUP_WIDTH_ORIENTATIONS.append(Surface.ROTATION_0, 180);//0
        SUP_WIDTH_ORIENTATIONS.append(Surface.ROTATION_90, 90);
        SUP_WIDTH_ORIENTATIONS.append(Surface.ROTATION_180, 0);//180
        SUP_WIDTH_ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    static {
        BACK_DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        BACK_DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        BACK_DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        BACK_DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        BACK_INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        BACK_INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        BACK_INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        BACK_INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private final CameraCaptureSession.CaptureCallback mCaptureCallback
            = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request,
                                     long timestamp, long frameNumber) {
            Log.w(TAG, "CaptureSession OnCaptureStarted " + session.toString());
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            Log.w(TAG, "CaptureSession OnCaptureCompleted " + session.toString());
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request,
                                    CaptureFailure failure) {
            Log.w(TAG, "CaptureSession OnCaptureFailure " + session.toString() + " failure:" + failure.getReason());
        }

    };
    public String finalName;
    public String cameraForPermission = "";
    long id_match = 0;
    String prefix;
    Rect sensor_rect = new Rect();
    Rect scalar_crop_region;
    int current_zoom_value = 0;
    //VideoTools
    VideoTools videoTools;
    private Context _context;
    private SimpleDateFormat fmt = new SimpleDateFormat("ddMMyyyy_HHmm");
    //CAMERA
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CameraConstrainedHighSpeedCaptureSession mCaptureSessionHighSpeed;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private int cameraToUse = FRONT_CAMERA;
    private int cameraStatus = CAMERA_IDLE;
    private Integer mSensorOrientation;
    private CaptureRequest.Builder mPreviewBuilder;
    private Surface mRecorderSurface;
    //MEDIARECORDER
    private MediaRecorder mMediaRecorder;
    private boolean mIsRecordingVideo;
    private Size mPreviewSize;
    private Size mTextureSize;
    private Size mCaptureSize;
    //THREAD
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    //PATH
    private String mNextVideoAbsolutePath;
    private String lastVideoAbsolutePath = null;
    private boolean highSpeed = false;
    private int quality = 0;
    private int n_video = 0;
    //VIEW
    private AutoFitTextureView mTextureView;
    //Configuration
    private boolean configured = false;
    private int manual_exposure = 0;
    private boolean auto_exposure = true;
    private ProfileSettingsDao profileSettingsDao;
    private ProfileSettings profileSettings;
    private ProfileSettings[] profileSettingsArray;
    private boolean stopped = false;
    /****** ERROR STATUS LISTENER *******************************************************************************************/
    private Cam2HSVideoListener listener;
    //CAMERA STATECALLBACK
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;

            while (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                //ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            initCaptureSystem();

            mCameraOpenCloseLock.release();
            cameraStatus = CAMERA_OPEN;

            if (listener != null) listener.onStart();

            Log.w(TAG, "Camera Opened");

        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            cameraStatus = CAMERA_DISCONNECTED;

            Log.w(TAG, "Camera Disconnected");
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            if (mCameraDevice != null) mCameraDevice.close();
            cameraStatus = CAMERA_ERROR;
            mCameraDevice = null;

            Log.w(TAG, "Camera ERROR ******* " + error);


        }

    };
    /*************** SURFACE TEXTURE LISTENER ***********************************************************************************/
    private TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            Log.w(TAG, "SURFACE AVAILABLE   ****************************");
            mTextureSize = new Size(width, height);
            setCameraAndStart();

            //openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
            //Log.w(TAG, "onSurfaceTextureSizeChanged");

            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            if (mCameraDevice != null) {
                //Log.w(TAG, "Surface Texture Destroyed");
                closeCamera();
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            //Log.w(TAG, "onSurfaceTextureUpdated");

        }

    };

    /********************************************************************************************/

    public static Camera2HSVideoFragment newInstance(int camToUse, Context context) {
        Camera2HSVideoFragment fragment = new Camera2HSVideoFragment();
        fragment._context = context;
        Bundle args = new Bundle();
        args.putInt(ARG_CAM2USE, camToUse);
        fragment.setArguments(args);
        return fragment;
    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<Size>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new Camera2ViewFragment.CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("SET PARAMETERS", "ON CREATE CAMERA2VIDEOS");

        MainViewModel viewModel = new ViewModelProvider(getActivity()).get(MainViewModel.class);

        profileSettingsDao = viewModel.getDatabase().profileSettingsDao();

        Executor myExecutor = Executors.newSingleThreadExecutor();

        SharedPreferences sharedPref = getActivity().getSharedPreferences("Pikkucam", Context.MODE_PRIVATE);

        myExecutor.execute(() -> {
            profileSettingsArray = profileSettingsDao.getProfileSettingsArray();
            try {
                profileSettings = profileSettingsArray[sharedPref.getInt("Selected_mode", 0)];
            } catch (IndexOutOfBoundsException e) {
                profileSettings = profileSettingsArray[0];
            }

            if (videoTools == null) {
                FirebaseCrashlytics.getInstance().log("Es null videoTools");
                //Que pasa cuando es null
                cameraToUse = FRONT_CAMERA;
            } else {

                        int result = videoTools.extractVideoCamera(profileSettings.getCamera());

            if (result == 0) {
                cameraToUse = FRONT_CAMERA;
            } else if (result == 1) {
                cameraToUse = BACK_CAMERA;
            }


               // FirebaseCrashlytics.getInstance().log("No es null videoTools");
                //GreenScreen
                //Log.d("SON NULL LAS PROFILE SETTINGS", String.valueOf(profileSettings.getCamera()));

            }

        });

        //  cameraToUse = getArguments().getInt(ARG_CAM2USE);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera2_video, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        Log.d(TAG, "VIEW CREATED ******* ");
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);


    }

    @Override
    public void onResume() {
        super.onResume();
        setUpCaptureSession();
        //mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        Log.d(TAG, "onResume");
        //openCamera();
        //startBackgroundThread();

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        //closeCamera();
        //stopBackgroundThread();
        mTextureView.setSurfaceTextureListener(null);
        super.onPause();

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        stopBackgroundThread();
        closeCamera();
        mTextureView = null;

        super.onDestroy();

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        //closeCamera();

        super.onDestroyView();
    }

    /**************************************************************************************************************/
    public void setParameters(long idmatch, int qua, String folder, boolean highspeed, VideoTools vt) {

        Log.d("SET PARAMETERS", "SET PARAMETERS");

        id_match = idmatch;
        quality = qua;
        prefix = folder;
        videoTools = vt;
        highSpeed = highspeed;

        setVideoFilePath();
    }

    public void setCameraAndStart() {
        cameraStatus = CAMERA_IDLE;
        openCamera();
    }

    // Assign the listener implementing events interface that will receive the events
    public void setListener(Cam2HSVideoListener listener) {
        this.listener = listener;
    }

    public boolean isSurfaceAvailable() {
        return mTextureView.isAvailable();
    }

    private void openCamera() {//int width, int height) {

        Log.w(TAG, "CAMERA OPEN *********************************");

        CameraManager manager = (CameraManager) _context.getSystemService(Context.CAMERA_SERVICE);
        try {
            // Wait for any previously running session to finish.
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);


                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing != cameraToUse)//CameraCharacteristics.LENS_FACING_BACK)//.LENS_FACING_FRONT)
                {
                    continue;
                }


                if (!configured) {
                    StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                    sensor_rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

                    mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

                    if (highSpeed) mCaptureSize = videoTools.getHighSpeedSize(cameraToUse);
                    else mCaptureSize = chooseVideoSize(map.getOutputSizes(ImageFormat.JPEG));

                    Log.d(TAG, "Capture Size W:" + mCaptureSize.getWidth() + " H:" + mCaptureSize.getHeight());

                    WindowManager wm = (WindowManager) _context.getSystemService(_context.WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();
                    //mPreviewSize = mCaptureSize;
                    mPreviewSize = new Size(display.getWidth(), display.getHeight());

                    int orientation = getResources().getConfiguration().orientation;

                    if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                    } else {
                        mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                    }

                    configureTransform(mTextureSize.getWidth(), mTextureSize.getHeight());//mCaptureSize.getWidth(),mCaptureSize.getHeight());//,mPreviewSize.getWidth(), mPreviewSize.getHeight());
                    configured = true;
                }

                mMediaRecorder = new MediaRecorder();

                while (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Thread.sleep(1000);
                }
                manager.openCamera(cameraId, mStateCallback, mBackgroundHandler);// backgroundHandler);//null);
                break;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        Log.w(TAG, "CAMERA CLOSE *********************************");

        try {
            mCameraOpenCloseLock.acquire();
            closePreviewSession();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release();
        }
    }


    /* BACKGROUND THREAD *******************************************/

    public void closeMediaRecorder() {
        if (null != mMediaRecorder) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    public void startBackThread() {
        startBackgroundThread();
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {

        Log.w(TAG, "BACKSTHREAD STARTS *********************************");

        mBackgroundThread = new HandlerThread("CameraBackground" + n_video);
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**************** CAPTURE SYSTEM ***************************************************************/

    private void initCaptureSystem() {
        n_video++;

        Log.w(TAG, "CAPTURE INITSYSTEM *********************************");

        if (setUpMediaRecorder() == 1) {

            if (highSpeed) setUpCaptureHighSpeedSession();
            else setUpCaptureSession();

        }
    }

    public boolean setUpCaptureSession() {

        if (null == mCameraDevice) {
            return false;
        }

        try {

            Log.w(TAG, "CAPTURE REQUESTCREATE *********************************");

            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            if (texture == null) return false;

            texture.setDefaultBufferSize(mCaptureSize.getWidth(), mCaptureSize.getHeight());

            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);


            // Set up Surface for the MediaRecorder
            try {
                mRecorderSurface = mMediaRecorder.getSurface();
            } catch (IllegalStateException i) {
                i.printStackTrace();
                return false;
            }

            surfaces.add(mRecorderSurface);
            mPreviewBuilder.addTarget(mRecorderSurface);

            Log.w(TAG, " pSurface:" + previewSurface + " MR mSurface:" + mRecorderSurface);

            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.w(TAG, "CaptureSession OnConfigured " + cameraCaptureSession.toString());
                    mCaptureSession = cameraCaptureSession;

                    // Start recording
                    startRecordingVideo();

                }

                @Override
                public void onReady(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.w(TAG, "CaptureSession OnReady " + cameraCaptureSession.toString());

                }

                @Override
                public void onClosed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.w(TAG, "CaptureSession OnClosed " + cameraCaptureSession.toString());
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                    Log.w(TAG, "CaptureSession OnConfiguredFailed" + cameraCaptureSession.toString());
                }


            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }

    @TargetApi(23)
    private void setUpCaptureHighSpeedSession() {
        if (null == mCameraDevice) {
            return;
        }

        try {

            Log.w(TAG, "CAPTURE REQUESTCREATE *********************************");

            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mCaptureSize.getWidth(), mCaptureSize.getHeight());

            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);


            // Set up Surface for the MediaRecorder
            try {
                mRecorderSurface = mMediaRecorder.getSurface();
            } catch (IllegalStateException i) {
                i.printStackTrace();
            }

            surfaces.add(mRecorderSurface);
            mPreviewBuilder.addTarget(mRecorderSurface);

            Log.w(TAG, " pSurface:" + previewSurface + " MR mSurface:" + mRecorderSurface);
            // Start a capture session
            // Once the session starts, we can update the UI and start recording
            mCameraDevice.createConstrainedHighSpeedCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.w(TAG, "CaptureSession OnConfigured " + cameraCaptureSession.toString());
                    mCaptureSessionHighSpeed = (CameraConstrainedHighSpeedCaptureSession) cameraCaptureSession;

                    // Start recording
                    startRecordingVideo();

                }

                @Override
                public void onReady(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.w(TAG, "CaptureSession OnReady " + cameraCaptureSession.toString());


                }

                @Override
                public void onClosed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.w(TAG, "CaptureSession OnClosed " + cameraCaptureSession.toString());
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                    Log.w(TAG, "CaptureSession OnConfiguredFailed" + cameraCaptureSession.toString());
                }


            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void closeCaptureSystem() {

        stopBackgroundThread();
        closePreviewSession();

    }

    @TargetApi(23)
    private void setUpHSCaptureRequest() {
        try {
            if (mCaptureSessionHighSpeed != null) {
                List<CaptureRequest> mPreviewBuilderBurst = mCaptureSessionHighSpeed.createHighSpeedRequestList(mPreviewBuilder.build());
                mCaptureSessionHighSpeed.setRepeatingBurst(mPreviewBuilderBurst, null, mBackgroundHandler);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequest() {
        Log.w(TAG, "CAPTURE REPEATINGREQUEST *********************************");
        if (highSpeed) setUpHSCaptureRequest();
        else {
            try {

                if (mCaptureSession != null && mPreviewBuilder != null)
                    mCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);//mCaptureCallback, mBackgroundHandler);

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {


        if (highSpeed) {

            Range<Integer> fpsRange = videoTools.getHighSpeedFpsRange(cameraToUse);

            builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRange);
        }

        //else
        {
            if (auto_exposure) {
                Log.w(TAG, "CAPTURE BUILDER AUTO:" + auto_exposure);
                builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            } else {
                Log.w(TAG, "CAPTURE BUILDER MANUAL:" + manual_exposure);
                builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, manual_exposure);
            }

            if (current_zoom_value > 0 && scalar_crop_region != null) {
                builder.set(CaptureRequest.SCALER_CROP_REGION, scalar_crop_region);
            }

        }
    }

    private void closePreviewSession() {
        if (mCaptureSession != null) {
            Log.w(TAG, "Closing Preview Session");
            mCaptureSession.close();
            mCaptureSession = null;
        }
    }


    /***************** VIDEO ********************************************************/
    public void startRecordingVideo() {

        setUpCaptureRequestBuilder(mPreviewBuilder);
        //  HandlerThread thread = new HandlerThread("CameraPreview");
        //  thread.start();
        setUpCaptureRequest();

        // Start recording
        if (!mIsRecordingVideo) {
            Log.w(TAG, "MR START *********************************");
            try {
                mMediaRecorder.start();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        mIsRecordingVideo = true;

        Log.d(TAG, "isRecordingVideo " + String.valueOf(mIsRecordingVideo));
        cameraStatus = CAMERA_CAPTURING;

    }

    private void stopRecordingVideo() {

        // Added by Ben Ning, to resolve exception issue when stop recording.
        try {
            if (mCaptureSession != null) {
                if (highSpeed) {
                    mCaptureSessionHighSpeed.stopRepeating();
                    mCaptureSessionHighSpeed.abortCaptures();
                } else {
                    mCaptureSession.stopRepeating();
                    mCaptureSession.abortCaptures();
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        // Stop recording
        if (mIsRecordingVideo && mMediaRecorder != null) {
            Log.w(TAG, "MR STOP *********************************");


            //SystemClock.sleep(100);

            try {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                //stopBackgroundThread();
            } catch (RuntimeException r) {
                r.printStackTrace();
            }


            mIsRecordingVideo = false;

            Log.d(TAG, "Video saved: " + mNextVideoAbsolutePath + "      isRecordingVideo" + String.valueOf(mIsRecordingVideo));
        }

    }

    public void changeVideo() {
        if (mIsRecordingVideo) {
            Log.i(TAG, "CHANGING VIDEO ***********");
            stopRecordingVideo();


            closeCamera();

            deletePrevFile();
            renameCurrentFile();
            openCamera();

        }
    }

    public String saveVideo(String name) {

        finalName = name + "_" + fmt.format(new Date());//+"_"+name;
        renamePrevFile(finalName);

        Log.i("SAVE VIDEO ***********", finalName);
        return finalName;

    }

    public void close() {
        if (mIsRecordingVideo) {
            stopRecordingVideo();
            closeCamera();
        }

    }


    /* MEDIARECORDER ***************************************************************************/
    private int setUpMediaRecorder() {

        try {
            //if(!highSpeed)
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

            mMediaRecorder.setOutputFormat(getOutputFormat());//.MPEG_4);

            if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
                mNextVideoAbsolutePath = getVideoFilePath();
            }

            mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);

            if (highSpeed) {
                mMediaRecorder.setVideoEncodingBitRate(20000000);
                mMediaRecorder.setCaptureRate(videoTools.getHighSpeedFpsRange(cameraToUse).getUpper());
                mMediaRecorder.setVideoFrameRate(videoTools.getHighSpeedFpsRange(cameraToUse).getUpper());
                mMediaRecorder.setVideoSize(mCaptureSize.getWidth(), mCaptureSize.getHeight());

                mMediaRecorder.setVideoEncoder(getVideoEncoder());

                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mMediaRecorder.setAudioEncodingBitRate(16 * 22050);
                mMediaRecorder.setAudioSamplingRate(22050);

            } else {

                mMediaRecorder.setVideoEncodingBitRate(getVideoRate());//512000);//10000000);
                //Eliminamos captureRate por los avisos de google de quitar el audio. Preventivo. Sí que funciona
                //Descomentado para solucionar error en S7 Edge
                mMediaRecorder.setCaptureRate(getFrameRate());

                mMediaRecorder.setVideoFrameRate(getFrameRate());//25);//30
                mMediaRecorder.setVideoSize(getWidth(), getHeight());//wSize,hSize);//mCaptureSize.getWidth(), mCaptureSize.getHeight());

                mMediaRecorder.setVideoEncoder(getVideoEncoder());

                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mMediaRecorder.setAudioEncodingBitRate(16 * 22050);
                mMediaRecorder.setAudioSamplingRate(22050);

            }


            int rotation = ((Activity) _context).getWindowManager().getDefaultDisplay().getRotation();

            if (cameraToUse == FRONT_CAMERA) {
                switch (mSensorOrientation) {
                    case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                        mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                        break;
                    case SENSOR_ORIENTATION_INVERSE_DEGREES:
                        mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                        break;
                    case 0:
                        mMediaRecorder.setOrientationHint(SUP_WIDTH_ORIENTATIONS.get(rotation));
                        break;
                    case 180:
                        mMediaRecorder.setOrientationHint(SUP_WIDTH_ORIENTATIONS.get(rotation));
                        break;

                }
            } else {
                //BACK_CAMERA
                switch (mSensorOrientation) {
                    case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                        mMediaRecorder.setOrientationHint(BACK_DEFAULT_ORIENTATIONS.get(rotation));
                        break;
                    case SENSOR_ORIENTATION_INVERSE_DEGREES:
                        mMediaRecorder.setOrientationHint(BACK_INVERSE_ORIENTATIONS.get(rotation));
                        break;
                }

            }

            Log.d(TAG, "MR Going to prepare ******************* ");
            mMediaRecorder.prepare();

        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());

            return 0;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());

            return 0;
        }
        return 1;
    }


    /******************** PATHS *************************************************************************/
    private boolean setVideoFilePath() {
        //String path = _context.getFilesDir().getAbsolutePath()+"/"+prefix+id_match;
        File defDir = new File(_context.getFilesDir() + "/Pikkucam/");//, "/"+prefix+id_match);
        boolean result = true;

        if (!defDir.exists()) {
            //First Time
            result = defDir.mkdir();
            Log.d("Camera2Video", " Making directory ..." + defDir.getAbsolutePath());
        }

        return result;
    }

    public String getVideoDirPath() {
        return _context.getFilesDir() + File.separator + "Pikkucam" + "";//+prefix+id_match;
        //return _context.getExternalFilesDir(null).getAbsolutePath() + "/"+prefix+id_match+"/";
    }

    private String getVideoFilePath() {

        return _context.getFilesDir() + File.separator + "Pikkucam" + "/" + "pikku_video_current.mp4";
        //+ System.currentTimeMillis() + ".3gp";
    }

    public String getVideoPrevFilePath() {
        return _context.getFilesDir() + File.separator + "Pikkucam/" + "pikku_video_prev.mp4";
        //+ System.currentTimeMillis() + ".3gp";
    }

    private String getVideoSaveFilePath(String name) {
        return _context.getFilesDir() + File.separator + "Pikkucam" + "/" + "pikkucam_"
                + name + ".mp4";
    }


    /****************** GESTION FILES ***************************************/

    public void deletePrevFile() {
        File fdelete = new File(getVideoPrevFilePath());
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                //Log.d("file Deleted :" + getVideoPrevFilePath());
            } else {
                Log.w(TAG, "file not Deleted :" + getVideoPrevFilePath());
            }
        }
    }

    public void deleteCurrentFile() {
        File fdelete = new File(getVideoFilePath());
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                //Log.d(TAG,"file Deleted :" + getVideoFilePath());
            } else {
                Log.w(TAG, "file not Deleted :" + getVideoFilePath());
            }
        }
    }

    private void deleteVideoFile(String path) {
        File fdelete = new File(path);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                //Log.d(TAG,"file Deleted :" + path);
            } else {
                Log.w(TAG, "file not Deleted :" + path);
            }
        }
    }

    public void changeFileName(String src, String dst) {
        File from = new File(src);
        String nameWithDate = dst + "_" + fmt.format(new Date());//+"_"+dst;
        File to = new File(getVideoSaveFilePath(nameWithDate));
        from.renameTo(to);
        lastVideoAbsolutePath = to.toString();

        Log.i("Change from path is", from.toString());
        Log.i("To path is", to.toString());
    }

    private void renameCurrentFile() {
        String currentFileName = getVideoFilePath();

        //File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), MEDIA_NAME);
        File from = new File(currentFileName);
        File to = new File(getVideoPrevFilePath());
        from.renameTo(to);

        Log.i("From path is", from.toString());
        Log.i("To path is", to.toString());
    }

    private void renamePrevFile(String name) {
        String currentFileName = getVideoPrevFilePath();

        //File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), MEDIA_NAME);
        File from = new File(currentFileName);
        File to = new File(getVideoSaveFilePath(name));
        from.renameTo(to);

        Log.i("From path is", from.toString());
        Log.i("To path is", to.toString());

        lastVideoAbsolutePath = to.toString();
    }

    public String getLastVideoAbsolutePath() {
        return lastVideoAbsolutePath;
    }

    public int getCameraStatus() {
        return cameraStatus;
    }

    private Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 16 / 9 && quality == CfgDef.VAL_VIDEOQUALITY_MAX){
                return size;
            }
            if (size.getWidth() == size.getHeight() * 16 / 9 && size.getWidth() <= getTargetWidth()) { //size.getWidth() == size.getHeight() * 4 / 3 &&
                return size;
            }
        }

        for (Size size : choices) {
            if (size.getWidth() > size.getHeight() && size.getWidth() <= getTargetWidth()) {
                return size;
            }
        }

        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    private int getTargetWidth() {
        int q = 0;

        switch (quality) {
            case CfgDef.VAL_VIDEOQUALITY_LOW:
                q = 960;
                break;

            case CfgDef.VAL_VIDEOQUALITY_MED:
                q = 1280;
                break;

            case CfgDef.VAL_VIDEOQUALITY_HIGH:
                q = 1920;
                break;
        }

        return q;

    }

    private int getWidth() {
        int q = 0;
        //return 960;

        switch (quality) {
            case CfgDef.VAL_VIDEOQUALITY_LOW:
                q = mCaptureSize.getWidth();//800;
                break;

            case CfgDef.VAL_VIDEOQUALITY_MED:
                q = mCaptureSize.getWidth();
                break;

            case CfgDef.VAL_VIDEOQUALITY_HIGH:
                q = mCaptureSize.getWidth();
                break;

            case CfgDef.VAL_VIDEOQUALITY_MAX:
                q = mCaptureSize.getWidth();
                break;
        }

        return q;

    }

    private int getHeight() {
        int q = 0;

        switch (quality) {
            case CfgDef.VAL_VIDEOQUALITY_LOW:
                q = mCaptureSize.getHeight();
                break;

            case CfgDef.VAL_VIDEOQUALITY_MED:
                q = mCaptureSize.getHeight();
                break;

            case CfgDef.VAL_VIDEOQUALITY_HIGH:
                q = mCaptureSize.getHeight();
                break;

            case CfgDef.VAL_VIDEOQUALITY_MAX:
                q = mCaptureSize.getHeight();
                break;
        }

        return q;
    }

    private int getVideoRate() {
        int q = 0;
        switch (quality) {
            case CfgDef.VAL_VIDEOQUALITY_LOW:
                q = 1000000;
                break;

            case CfgDef.VAL_VIDEOQUALITY_MED:
                q = 2500000;
                break;

            case CfgDef.VAL_VIDEOQUALITY_HIGH:
                q = 10000000;
                break;

            case CfgDef.VAL_VIDEOQUALITY_MAX:
                q = 10000000;
                break;
        }

        return q;
    }

    private int getFrameRate() {
        int q = 0;
        switch (quality) {
            case CfgDef.VAL_VIDEOQUALITY_LOW:
                q = 15;
                break;

            case CfgDef.VAL_VIDEOQUALITY_MED:
                q = 25;
                break;

            case CfgDef.VAL_VIDEOQUALITY_HIGH:
                q = 30;
                break;
            case CfgDef.VAL_VIDEOQUALITY_MAX:
                q = 30;
                break;
        }

        return q;
    }

    private int getOutputFormat() {
        int q = 0;
        switch (quality) {
            case CfgDef.VAL_VIDEOQUALITY_LOW:
                q = MediaRecorder.OutputFormat.MPEG_4;
                break;

            case CfgDef.VAL_VIDEOQUALITY_MED:
                q = MediaRecorder.OutputFormat.MPEG_4;
                break;

            case CfgDef.VAL_VIDEOQUALITY_HIGH:
                q = MediaRecorder.OutputFormat.MPEG_4;
                break;
            case CfgDef.VAL_VIDEOQUALITY_MAX:
                q = MediaRecorder.OutputFormat.MPEG_4;
                break;
        }

        return q;
    }

    private int getVideoEncoder() {
        int q = 0;
        switch (quality) {
            case CfgDef.VAL_VIDEOQUALITY_LOW:
                q = MediaRecorder.VideoEncoder.H264;
                break;

            case CfgDef.VAL_VIDEOQUALITY_MED:
                q = MediaRecorder.VideoEncoder.H264;
                break;

            case CfgDef.VAL_VIDEOQUALITY_HIGH:
                q = MediaRecorder.VideoEncoder.H264;
                break;
            case CfgDef.VAL_VIDEOQUALITY_MAX:
                q = MediaRecorder.VideoEncoder.H264;
                break;
        }

        return q;
    }

    void setPreviewAspectRatio() {
        int orientation = _context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mTextureView.setAspectRatio(mCaptureSize.getWidth(), mCaptureSize.getHeight());
        } else {
            mTextureView.setAspectRatio(mCaptureSize.getHeight(), mCaptureSize.getWidth());
        }
    }
/*
    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

*/
    /*private void getCodecInfo_21() {
        textInfo.setText("API >= 21");
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.ALL_CODECS);
        //MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        mediaCodecInfo = mediaCodecList.getCodecInfos();

        textCodecCount.setText("Number of Codec: " + mediaCodecInfo.length);
    }
    */

    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        // RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        RectF bufferRect = new RectF(0, 0, mCaptureSize.getHeight(), mCaptureSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mCaptureSize.getHeight(),
                    (float) viewWidth / mCaptureSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /* EXPOSURE *********************************************************************/
    private boolean setExposureCompensation(CaptureRequest.Builder builder, int ae_exposure_compensation) {

        if (builder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION) == null || ae_exposure_compensation != builder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION)) {

            auto_exposure = false;
            manual_exposure = ae_exposure_compensation;
            Log.w(TAG, "CAPTURE BUILDER MANUAL:" + manual_exposure);
            builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ae_exposure_compensation);
            return true;
        }
        return false;
    }

    public int getExposureCompensation() {
        if (mPreviewBuilder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION) == null)
            return 0;
        return mPreviewBuilder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION);
    }

    public void setAutoExposure() {
        if (mPreviewBuilder != null) {

            auto_exposure = true;
            Log.w(TAG, "CAPTURE BUILDER AUTO:" + auto_exposure + " Lock:" + mPreviewBuilder.get(CaptureRequest.CONTROL_AE_LOCK));


            mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0);

            setUpCaptureRequest();
        }
    }

    public boolean setExposureCompensation(int new_exposure) {

        if (mPreviewBuilder != null) {
            if (setExposureCompensation(mPreviewBuilder, new_exposure)) {

                setUpCaptureRequest();

                return true;
            }
        }
        return false;
    }

    /*********************** Zoom **************************/
    public float setZoom(int value) {
        /*
        if( zoom_ratios == null ) {
            if( MyDebug.LOG )
                Log.d(TAG, "zoom not supported");
            return;
        }
        if( value < 0 || value > zoom_ratios.size() ) {
            if( MyDebug.LOG )
                Log.e(TAG, "invalid zoom value" + value);
            throw new RuntimeException(); // throw as RuntimeException, as this is a programming error
        }
        float zoom = zoom_ratios.get(value)/100.0f;
        */
        if (videoTools.hasZoom(cameraToUse)) {
            if (value > 0 && value < videoTools.zoomRatios(cameraToUse).size()) {
                float zoom = videoTools.zoomRatios(cameraToUse).get(value) / 100.0f;

                int left = sensor_rect.width() / 2;
                int right = left;
                int top = sensor_rect.height() / 2;
                int bottom = top;
                int hwidth = (int) (sensor_rect.width() / (2.0 * zoom));
                int hheight = (int) (sensor_rect.height() / (2.0 * zoom));
                left -= hwidth;
                right += hwidth;
                top -= hheight;
                bottom += hheight;
                /*{
                    Log.d(TAG, "value: " + value);
                    Log.d(TAG, "zoom: " + zoom);
                    Log.d(TAG, "hwidth: " + hwidth);
                    Log.d(TAG, "hheight: " + hheight);
                    Log.d(TAG, "sensor_rect left: " + sensor_rect.left);
                    Log.d(TAG, "sensor_rect top: " + sensor_rect.top);
                    Log.d(TAG, "sensor_rect right: " + sensor_rect.right);
                    Log.d(TAG, "sensor_rect bottom: " + sensor_rect.bottom);
                    Log.d(TAG, "left: " + left);
                    Log.d(TAG, "top: " + top);
                    Log.d(TAG, "right: " + right);
                    Log.d(TAG, "bottom: " + bottom);

                }*/

                scalar_crop_region = new Rect(left, top, right, bottom);

                if (scalar_crop_region != null && mPreviewBuilder != null) {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        mPreviewBuilder.set(CaptureRequest.CONTROL_ZOOM_RATIO, zoom);
                    } else {
                        mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, scalar_crop_region);
                    }
                }

                current_zoom_value = value;

                setUpCaptureRequest();

                return zoom;
            }
        }
        return 0;
    }

    public interface Cam2HSVideoListener {

        public void onStart();

        public void onFinish();

        public void onFailure();

        public void onSuccess();
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

}
