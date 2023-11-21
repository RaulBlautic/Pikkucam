package com.blautic.pikkucam.video;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Range;
import android.util.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jsf on 17/02/18.
 */

public class VideoTools
{
    String TAG = "VideoTools";

    private Context context;

    public static final int FRONT_CAMERA  = CameraCharacteristics.LENS_FACING_FRONT;
    public static final int BACK_CAMERA  = CameraCharacteristics.LENS_FACING_BACK;

    private final int tonemap_max_curve_points_c = 64;



    CameraManager manager;
    private VideoCameraInfo[] camerasInfo=new VideoCameraInfo[2];

    public VideoTools(Context context) {
        this.context = context;
        for(int i=0;i<2;i++)
        {
            camerasInfo[i]=new VideoCameraInfo();
        }
    }

    public void getCameraCapabilities()
    {
        manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        try {
            String[] cameras = manager.getCameraIdList();
            int which=0;
            for (String camera : cameras) {
                CameraCharacteristics cc = manager.getCameraCharacteristics(camera);
                if(CameraCharacteristics.LENS_FACING_FRONT == cc.get(CameraCharacteristics.LENS_FACING)){
                    Log.e(TAG, "Camera: FRONT");
                    which=FRONT_CAMERA;
                }
                else  if(CameraCharacteristics.LENS_FACING_BACK == cc.get(CameraCharacteristics.LENS_FACING)){
                    Log.e(TAG, "Camera: BACK");
                    which=BACK_CAMERA;
                }

                CameraCharacteristics.Key<int[]> aa = cc.REQUEST_AVAILABLE_CAPABILITIES;
                for (int i = 0; i < cc.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES).length; i++) {
                    if(cc.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)[i] == cc.REQUEST_AVAILABLE_CAPABILITIES_CONSTRAINED_HIGH_SPEED_VIDEO){
                        Log.e(TAG, "HIGH SPEED Capability: " + cc.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)[i]);
                        camerasInfo[which].setHighspeedAvailable(true);
                    }
                    else{
                        Log.e(TAG, "Capability: " + cc.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)[i]);

                    }
                }

                Range<Integer>[] fpsRange = cc.get(cc.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
                for (int i = 0; i < fpsRange.length; i++) {
                    Log.e(TAG, "Range: " + fpsRange[i]);
                }

                if(camerasInfo[which].isHighspeedAvailable()) {
                    StreamConfigurationMap map = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    //Range<Integer>[] fpsRange = map.getHighSpeedVideoFpsRanges();
                    /*for (int i = 0; i < fpsRange.length; i++) {
                        Log.e(TAG, "Range (MAP): " + fpsRange[i]);
                    }*/
                    Size[] sizes = map.getHighSpeedVideoSizes();
                    for (int i = 0; i < sizes.length; i++) {
                        Log.e(TAG, "Size (MAP): " + sizes[i]);
                    }
                    Size targetSize = getHSVideoSize(sizes);
                    if(targetSize != null)
                    {
                        camerasInfo[which].setHsCaptureSize(targetSize);
                        Log.e(TAG, "Target Size: " + targetSize);
                        Range<Integer>[] fpsXSize = map.getHighSpeedVideoFpsRangesFor(targetSize);
                        for (int j = 0; j < fpsXSize.length; j++) {
                            Log.e(TAG, "Range x Size: " + targetSize + " --> " + fpsXSize[j]);
                        }
                        Range range = getHighestFpsRange(fpsXSize);
                        Log.e(TAG,"Max:"+range);
                        camerasInfo[which].setHsFpsRange(range);
                    }
                }

                getExtraCameraFeatures(cc,which);

            }

        }catch(CameraAccessException c) {

        }
    }

    public boolean isHighSpeedAvailable(int cam)
    {
        return camerasInfo[cam].isHighspeedAvailable();
    }

    public Range<Integer> getHighSpeedFpsRange(int cam){
        return camerasInfo[cam].getHsFpsRange();
    }

    public Size getHighSpeedSize(int cam)
    {
        return camerasInfo[cam].getHsCaptureSize();
    }

    private Range<Integer> getHighestFpsRange(Range<Integer>[] fpsRanges)
    {
        Range<Integer> fpsRange = Range.create(fpsRanges[0].getLower(), fpsRanges[0].getUpper());
        for (Range<Integer> r : fpsRanges) {
            if (r.getUpper() >= fpsRange.getUpper()) {
                //fpsRange=fpsRange.extend(fpsRange.getLower(), r.getUpper());
                fpsRange=Range.create(r.getUpper(),r.getUpper());//fpsRange.extend(r.getUpper(), r.getUpper()); //min y max iguales desde cambio inferior
            }
        }
/*
 NO calculamos mínimo desde error en Xiaomi con algoritmo de autoexposure
        for (Range<Integer> r : fpsRanges) {
            if (r.getUpper() == fpsRange.getUpper()) {
                if (r.getLower() < fpsRange.getLower()) {
                    fpsRange=fpsRange.extend(r.getLower(), fpsRange.getUpper());
                }
            }
        }
*/
        return fpsRange;
    }

    private Size getHSVideoSize(Size[] sizes)
    {
        for (Size size : sizes) {
            if ( size.getWidth() > size.getHeight() && size.getWidth() <= 1280){//1280) { //size.getWidth() == size.getHeight() * 4 / 3 &&
                return size;
            }
        }
        return null;

    }

    public byte extractVideoCamera(int cam)
    {
        //Quitamos bit de slow motion
        byte result = (byte)((byte)cam & 0x7F);
        return result;
    }

    public boolean isEnabledVideoCameraHS(int cam)
    {
        if(((byte)cam & 0x80) != 0) return true;
        else return false;
    }

    public int buildVideoCamera(int cam,boolean slow)
    {
        int result=0;
        if(slow) result=((byte)(cam & 0xFF) | 0x80);
        else result= cam;
        return result;
    }

    public boolean supportsISORange(int cam) {

        return camerasInfo[cam].supports_iso_range;
    }
/*
    public List<String> getSupportedISOs(int cam) {

        return camerasInfo[cam].isos;
    }
*/
    /** Returns minimum ISO value. Only relevant if supportsISORange() returns true.
     */
    public int getMinimumISO(int cam) {

        return camerasInfo[cam].min_iso;
    }

    /** Returns maximum ISO value. Only relevant if supportsISORange() returns true.
     */
    public int getMaximumISO(int cam) {

        return camerasInfo[cam].max_iso;
    }

    public float getMinimumFocusDistance(int cam) {
        return camerasInfo[cam].minimum_focus_distance;
    }

    public boolean supportsExposureTime(int cam) {

        return camerasInfo[cam].supports_exposure_time;
    }

    public long getMinimumExposureTime(int cam) {

        return camerasInfo[cam].min_exposure_time;
    }

    public long getMaximumExposureTime(int cam) {

        long max = camerasInfo[cam].max_exposure_time;
        /*
        if( applicationInterface.isExpoBracketingPref() || applicationInterface.isFocusBracketingPref() || applicationInterface.isCameraBurstPref() ) {
            // doesn't make sense to allow exposure times more than 0.5s in these modes
            max = Math.min(max_exposure_time, 1000000000L/2);
        }

      */
        return max;
    }


    public boolean supportsExposures(int cam)
    {
        //JSF adaptado ... comprobar

        if(camerasInfo[cam].min_exposure != 0 || camerasInfo[cam].max_exposure != 0 )
        {
            return true;
        }else return false;

    }

    public int getMinimumExposure(int cam) {

        return camerasInfo[cam].min_exposure;
    }

    public int getMaximumExposure(int cam) {

        return camerasInfo[cam].max_exposure;
    }

    public int getMaxZoom(int cam) {

        return camerasInfo[cam].max_zoom;
    }

    public boolean hasZoom(int cam) {

        return camerasInfo[cam].is_zoom_supported;
    }

    public List<Integer> zoomRatios(int cam){

        return camerasInfo[cam].zoom_ratios;
    }
    /******************* getCameraFeatures **********************************************************/

    public void getExtraCameraFeatures(CameraCharacteristics characteristics,int which){//} throws CameraControllerException {


        //if( MyDebug.LOG )
        {
            int hardware_level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            switch (hardware_level) {
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                    Log.d(TAG, "Hardware Level: LEGACY");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                    Log.d(TAG, "Hardware Level: LIMITED");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                    Log.d(TAG, "Hardware Level: FULL");
                    break;
                case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                    Log.d(TAG, "Hardware Level: Level 3");
                    break;
                default:
                    Log.e(TAG, "Unknown Hardware Level: " + hardware_level);
                    break;
            }

            int [] nr_modes = characteristics.get(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES);
            Log.d(TAG, "nr_modes:");
            if( nr_modes == null ) {
                Log.d(TAG, "    none");
            }
            else {
                for(int i=0;i<nr_modes.length;i++) {
                    Log.d(TAG, "    " + i + ": " + nr_modes[i]);
                }
            }
        }

        float max_zoom = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        camerasInfo[which].is_zoom_supported = max_zoom > 0.0f;
        Log.d(TAG, "Zoom supported:"+camerasInfo[which].is_zoom_supported+" max_zoom:"+max_zoom);

        if(camerasInfo[which].is_zoom_supported ) {

            // set 20 steps per 2x factor
            final int steps_per_2x_factor = 20;
            //final double scale_factor = Math.pow(2.0, 1.0/(double)steps_per_2x_factor);
            int n_steps =(int)( (steps_per_2x_factor * Math.log(max_zoom + 1.0e-11)) / Math.log(2.0));
            final double scale_factor = Math.pow(max_zoom, 1.0/(double)n_steps);

            camerasInfo[which].zoom_ratios = new ArrayList<>();
            camerasInfo[which].zoom_ratios.add(100);
            double zoom = 1.0;
            for(int i=0;i<n_steps-1;i++) {
                zoom *= scale_factor;
                camerasInfo[which].zoom_ratios.add((int)(zoom*100));
            }
            camerasInfo[which].zoom_ratios.add((int)(max_zoom*100));
            camerasInfo[which].max_zoom = camerasInfo[which].zoom_ratios.size()-1;

        }





        if( characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ) {
            camerasInfo[which].supported_flash_values = new ArrayList<>();
            camerasInfo[which].supported_flash_values.add("flash_off");
            camerasInfo[which].supported_flash_values.add("flash_auto");
            camerasInfo[which].supported_flash_values.add("flash_on");
            camerasInfo[which].supported_flash_values.add("flash_torch");

        }/*
        else if( isFrontFacing() ) {
            supported_flash_values = new ArrayList<>();
            supported_flash_values.add("flash_off");
            supported_flash_values.add("flash_frontscreen_auto");
            supported_flash_values.add("flash_frontscreen_on");
            supported_flash_values.add("flash_frontscreen_torch");
        }*/

        Float minimum_focus_distance = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE); // may be null on some devices
        if( minimum_focus_distance != null ) {
            minimum_focus_distance = minimum_focus_distance;

        }
        else {
            minimum_focus_distance = 0.0f;
        }

        int [] supported_focus_modes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES); // Android format
        camerasInfo[which].supported_focus_values = convertFocusModesToValues(supported_focus_modes, minimum_focus_distance); // convert to our format (also resorts)
        if( camerasInfo[which].supported_focus_values != null && camerasInfo[which].supported_focus_values.contains("focus_mode_manual2") ) {
            camerasInfo[which].supports_focus_bracketing = true;
        }
        camerasInfo[which].max_num_focus_areas = characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);

        camerasInfo[which].is_exposure_lock_supported = true;

        camerasInfo[which].is_video_stabilization_supported = false;
        int [] supported_video_stabilization_modes = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES);
        if( supported_video_stabilization_modes != null ) {
            for(int supported_video_stabilization_mode : supported_video_stabilization_modes) {
                if( supported_video_stabilization_mode == CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_ON ) {
                    camerasInfo[which].is_video_stabilization_supported = true;
                }
            }
        }




        Range<Integer> iso_range = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE); // may be null on some devices
        if( iso_range != null ) {
            camerasInfo[which].supports_iso_range = true;
            camerasInfo[which].min_iso = iso_range.getLower();
            camerasInfo[which].max_iso = iso_range.getUpper();
            // we only expose exposure_time if iso_range is supported
            Range<Long> exposure_time_range = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE); // may be null on some devices
            if( exposure_time_range != null ) {
                camerasInfo[which].supports_exposure_time = true;
                camerasInfo[which].supports_expo_bracketing = true;
                camerasInfo[which].max_expo_bracketing_n_images = camerasInfo[which].max_expo_bracketing_n_images;
                camerasInfo[which].min_exposure_time = exposure_time_range.getLower();
                camerasInfo[which].max_exposure_time = exposure_time_range.getUpper();
            }
        }

        Range<Integer> exposure_range = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
        camerasInfo[which].min_exposure = exposure_range.getLower();
        camerasInfo[which].max_exposure = exposure_range.getUpper();
        camerasInfo[which].exposure_step = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP).floatValue();

        camerasInfo[which].can_disable_shutter_sound = true;

        Integer tonemap_max_curve_points = characteristics.get(CameraCharacteristics.TONEMAP_MAX_CURVE_POINTS);
        if( tonemap_max_curve_points != null ) {

            tonemap_max_curve_points = tonemap_max_curve_points;
            camerasInfo[which].supports_tonemap_curve = tonemap_max_curve_points >= tonemap_max_curve_points_c;
        }
        else {

        }

    }

    private List<String> convertFocusModesToValues(int [] supported_focus_modes_arr, float minimum_focus_distance) {

        if( supported_focus_modes_arr.length == 0 )
            return null;
        List<Integer> supported_focus_modes = new ArrayList<>();
        for(Integer supported_focus_mode : supported_focus_modes_arr)
            supported_focus_modes.add(supported_focus_mode);
        List<String> output_modes = new ArrayList<>();
        // also resort as well as converting
        if( supported_focus_modes.contains(CaptureRequest.CONTROL_AF_MODE_AUTO) ) {
            output_modes.add("focus_mode_auto");

        }
        if( supported_focus_modes.contains(CaptureRequest.CONTROL_AF_MODE_MACRO) ) {
            output_modes.add("focus_mode_macro");

        }
        if( supported_focus_modes.contains(CaptureRequest.CONTROL_AF_MODE_AUTO) ) {
            output_modes.add("focus_mode_locked");

        }
        if( supported_focus_modes.contains(CaptureRequest.CONTROL_AF_MODE_OFF) ) {
            output_modes.add("focus_mode_infinity");
            if( minimum_focus_distance > 0.0f ) {
                output_modes.add("focus_mode_manual2");

            }
        }
        if( supported_focus_modes.contains(CaptureRequest.CONTROL_AF_MODE_EDOF) ) {
            output_modes.add("focus_mode_edof");

        }
        if( supported_focus_modes.contains(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE) ) {
            output_modes.add("focus_mode_continuous_picture");

        }
        if( supported_focus_modes.contains(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO) ) {
            output_modes.add("focus_mode_continuous_video");

        }
        return output_modes;
    }
}
