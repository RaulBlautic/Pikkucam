package com.blautic.pikkucam.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ProfileSettings {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    @ColumnInfo(name = "NAME")
    private String name;

    @ColumnInfo(name = "CAMERA")
    private int camera;

    @ColumnInfo(name = "SLOWMO")
    private boolean slowMotion;

    @ColumnInfo(name = "SOUND")
    private boolean sound;

    @ColumnInfo(name = "GREENSCREEN")
    private boolean greenScreen;

    @ColumnInfo(name = "ISSUBTITLE")
    private boolean isSubtitle;

    @ColumnInfo(name = "SUBTITLE")
    private String subtitle;

    @ColumnInfo(name = "SHOWINFO")
    private boolean showInfo;

    @ColumnInfo(name = "VIDEOQUALITY")
    private int videoQuality;

    @ColumnInfo(name = "UPLOAD2DRIVE")
    private boolean uploadToDrive;

    @ColumnInfo(name = "SHOWPUBLI")
    private boolean showPubli;

    @ColumnInfo(name = "ADVERT_FILE")
    private String advert_file;

    @ColumnInfo(name = "VIDEO_DURATION")
    private String video_duration;

    @ColumnInfo(name = "ISSUBTITLEBUTTONS")
    private boolean isSubtitleButtons;

    @ColumnInfo(name = "BUTTON1_SHORT_SUBTITLE")
    private String button1_short_subtitle;

    @ColumnInfo(name = "BUTTON2_SHORT_SUBTITLE")
    private String button2_short_subtitle;

    @ColumnInfo(name = "BUTTON1_LONG_SUBTITLE")
    private String button1_long_subtitle;

    @ColumnInfo(name = "BUTTON2_LONG_SUBTITLE")
    private String button2_long_subtitle;

    @ColumnInfo(name = "MOVEMENTS")
    private boolean movimientos;

    @ColumnInfo(name = "MOV_RECOGNIZED")
    private String mov_recognized;

    @ColumnInfo(name = "IS_MOV_RECOGNIZED")
    private Boolean is_mov_recognized;
    @ColumnInfo(name = "CAMERABUTTONS")
    private int cameraButtons;
    @ColumnInfo(name = "firmwareVersion")
    private int firmwareVersion;
    @ColumnInfo(name = "isBle5")
    private boolean isBl5;

    public int getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(int firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public boolean isBl5() {
        return isBl5;
    }

    public void setBl5(boolean bl5) {
        isBl5 = bl5;
    }

    public int getCameraButtons() {
        return cameraButtons;
    }

    public void setCameraButtons(int cameraButtons) {
        this.cameraButtons = cameraButtons;
    }

    public Boolean getIs_mov_recognized() {
        return is_mov_recognized;
    }

    public void setIs_mov_recognized(Boolean is_mov_recognized) {
        this.is_mov_recognized = is_mov_recognized;
    }

    public String getMov_recognized() {
        return mov_recognized;
    }

    public void setMov_recognized(String mov_recognized) {
        this.mov_recognized = mov_recognized;
    }

    public void setIsSubtitle(boolean isSubtitle) {
        this.isSubtitle = isSubtitle;
    }

    public int getVideoQuality() {
        return videoQuality;
    }

    public void setVideoQuality(int videoQuality) {
        this.videoQuality = videoQuality;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCamera() {
        return camera;
    }

    public void setCamera(int camera) {
        this.camera = camera;
    }

    public boolean isGreenScreen() {
        return greenScreen;
    }

    public void setGreenScreen(boolean greenScreen) {
        this.greenScreen = greenScreen;
    }

    public boolean isShowInfo() {
        return showInfo;
    }

    public void setShowInfo(boolean showInfo) {
        this.showInfo = showInfo;
    }

    public boolean isShowPubli() {
        return showPubli;
    }

    public void setShowPubli(boolean showPubli) {
        this.showPubli = showPubli;
    }

    public boolean isSlowMotion() {
        return slowMotion;
    }

    public void setSlowMotion(boolean slowMotion) {
        this.slowMotion = slowMotion;
    }

    public boolean isSound() {
        return sound;
    }

    public void setSound(boolean sound) {
        this.sound = sound;
    }

    public boolean isSubtitle() {
        return isSubtitle;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public boolean isUploadToDrive() {
        return uploadToDrive;
    }

    public void setUploadToDrive(boolean uploadToDrive) {
        this.uploadToDrive = uploadToDrive;
    }

    public String getAdvert_file() {
        return advert_file;
    }

    public void setAdvert_file(String advert_file) {
        this.advert_file = advert_file;
    }

    public String getVideo_duration() {
        return video_duration;
    }

    public void setVideo_duration(String video_duration) {
        this.video_duration = video_duration;
    }

    public String getButton1_long_subtitle() {
        return button1_long_subtitle;
    }

    public void setButton1_long_subtitle(String button1_long_subtitle) {
        this.button1_long_subtitle = button1_long_subtitle;
    }

    public String getButton1_short_subtitle() {
        return button1_short_subtitle;
    }

    public void setButton1_short_subtitle(String button1_short_subtitle) {
        this.button1_short_subtitle = button1_short_subtitle;
    }

    public String getButton2_long_subtitle() {
        return button2_long_subtitle;
    }

    public void setButton2_long_subtitle(String button2_long_subtitle) {
        this.button2_long_subtitle = button2_long_subtitle;
    }

    public String getButton2_short_subtitle() {
        return button2_short_subtitle;
    }

    public void setButton2_short_subtitle(String button2_short_subtitle) {
        this.button2_short_subtitle = button2_short_subtitle;
    }

    public boolean isSubtitleButtons() {
        return isSubtitleButtons;
    }

    public void setSubtitleButtons(boolean subtitleButtons) {
        isSubtitleButtons = subtitleButtons;
    }

    public boolean getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(boolean movimientos) {
        this.movimientos = movimientos;
    }
}

