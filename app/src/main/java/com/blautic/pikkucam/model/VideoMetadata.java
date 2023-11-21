package com.blautic.pikkucam.model;

import android.net.Uri;

import java.io.File;
import java.util.Date;

public class VideoMetadata {

    private Uri file;
    private String name;
    private String subtitle;
    private String profile;
    private String isSlowMo;
    private Date date;
    private int duration;
    private String quality;
    private Long idThumb;

    public VideoMetadata(Uri file, String name, String subtitle, String profile, String isSlowMo, Date date, int duration, String quality,  Long idThumb){
        this.file = file;
        this.name = name;
        this.subtitle = subtitle;
        this.profile = profile;
        this.isSlowMo = isSlowMo;
        this.date = date;
        this.duration = duration;
        this.quality = quality;
        this.idThumb = idThumb;
    }

    public VideoMetadata(){

    }

    public Long getIdThumb() {
        return idThumb;
    }

    public void setIdThumb(Long idThumb) {
        this.idThumb = idThumb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setFile(Uri file) {
        this.file = file;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public void setIsSlowMo(String isSlowMo) {
        this.isSlowMo = isSlowMo;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public Uri getFile() {
        return file;
    }

    public int getDuration() {
        return duration;
    }

    public Date getDate() {
        return date;
    }

    public String getProfile() {
        return profile;
    }

    public String getQuality() {
        return quality;
    }

    public String getIsSlowMo() {
        return isSlowMo;
    }
}
