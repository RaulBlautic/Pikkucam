package com.blautic.pikkucam.video;

import android.util.Range;
import android.util.Size;

import java.util.List;

/**
 * Created by jsf on 17/02/18.
 */

public class VideoCameraInfo {

    boolean highspeedAvailable=false;
    Size           hsCaptureSize;
    Range<Integer> hsFpsRange;



    public boolean is_zoom_supported;
    public int max_zoom;
    public List<Integer> zoom_ratios;
    public List<String> supported_flash_values;
    public List<String> supported_focus_values;
    public int max_num_focus_areas;
    public float minimum_focus_distance;
    public boolean is_exposure_lock_supported;
    public boolean is_video_stabilization_supported;
    public boolean supports_white_balance_temperature;
    public int min_temperature;
    public int max_temperature;
    public boolean supports_iso_range;
    public int min_iso;
    public int max_iso;
    public boolean supports_exposure_time;
    public long min_exposure_time;
    public long max_exposure_time;
    public int min_exposure;
    public int max_exposure;
    public float exposure_step;
    public boolean can_disable_shutter_sound;
    public int tonemap_max_curve_points;
    public boolean supports_tonemap_curve;
    public boolean supports_expo_bracketing; // whether setBurstTye(BURSTTYPE_EXPO) can be used
    public int max_expo_bracketing_n_images;
    public boolean supports_focus_bracketing; // whether setBurstTye(BURSTTYPE_FOCUS) can be used
    public boolean supports_burst; // whether setBurstTye(BURSTTYPE_NORMAL) can be used
    public boolean supports_raw;

    public boolean isHighspeedAvailable() {
        return highspeedAvailable;
    }

    public void setHighspeedAvailable(boolean highspeedAvailable) {
        this.highspeedAvailable = highspeedAvailable;
    }

    public Range<Integer> getHsFpsRange() {
        return hsFpsRange;
    }

    public void setHsFpsRange(Range<Integer> fpsRange) {
        this.hsFpsRange = fpsRange;
    }

    public Size getHsCaptureSize() {
        return hsCaptureSize;
    }

    public void setHsCaptureSize(Size hsCaptureSize) {
        this.hsCaptureSize = hsCaptureSize;
    }
}
