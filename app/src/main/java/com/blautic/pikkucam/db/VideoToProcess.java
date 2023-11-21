package com.blautic.pikkucam.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class VideoToProcess {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "source")
    private Integer source;

    @ColumnInfo(name = "path")
    private String path;

    @ColumnInfo(name = "type")
    private Integer type;

    @ColumnInfo(name = "session_id")
    private Long session_id;

    @ColumnInfo(name = "status")
    private Integer status;

    @ColumnInfo(name = "date")
    private java.util.Date date;

    @ColumnInfo(name = "subtitle_type")
    private Integer subtitle_type;

    @ColumnInfo(name = "subtitle_txt")
    private String subtitle_txt;

    @ColumnInfo(name = "adv_type")
    private Integer adv_type;

    @ColumnInfo(name = "adv_file")
    private String adv_file;

    @ColumnInfo(name = "profile")
    private String profile;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getAdv_type() {
        return adv_type;
    }

    public void setAdv_type(Integer adv_type) {
        this.adv_type = adv_type;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Integer getSubtitle_type() {
        return subtitle_type;
    }

    public void setSubtitle_type(Integer subtitle_type) {
        this.subtitle_type = subtitle_type;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getSession_id() {
        return session_id;
    }

    public void setSession_id(Long session_id) {
        this.session_id = session_id;
    }

    public String getAdv_file() {
        return adv_file;
    }

    public void setAdv_file(String adv_file) {
        this.adv_file = adv_file;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSubtitle_txt() {
        return subtitle_txt;
    }

    public void setSubtitle_txt(String subtitle_txt) {
        this.subtitle_txt = subtitle_txt;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}

