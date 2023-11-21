package com.blautic.pikkucam.video;

import com.blautic.pikkucam.db.ProfileSettings;
import com.blautic.pikkucam.sports.SportsVals;

import java.util.Date;

import javax.annotation.Nullable;

/**
 * Created by jsf on 15/10/17.
 */

public class VideoItem {

    String path;
    long session;
    long db_id=-1;
    boolean slowmotion =false;
    int highspeedFPS=0;
    boolean subtitleEnabled=false;
    String subtitleTxt;
    boolean advertEnabled=false;
    String advertFile;
    int numVideoFile=0;
    int videoQuality;
    ProfileSettings profileSettings;
    String profileName;


    Date date;

    public VideoItem(String path, long session,int highspeedFPS, int subtitle_type,String txt,int numfile) {
        this.path = path;
        this.session = session;
        this.highspeedFPS=highspeedFPS;
        if(highspeedFPS>0) slowmotion =true;
        subtitleEnabled= subtitle_type != SportsVals.FEATURE_SUBTITLE_TYPE_DISABLED;
        subtitleTxt=txt;
        numVideoFile=numfile;
    }



    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSession() {
        return session;
    }

    public void setSession(long session) {
        this.session = session;
    }

    public long getDb_id() {
        return db_id;
    }

    public void setDb_id(Long db_id)
    {
        if(db_id != null) this.db_id = db_id.longValue();
    }

    public boolean isSlowmotion() {
        return slowmotion;
    }

    public void setSlowmotion(boolean slowmotion) {
        this.slowmotion = slowmotion;
    }

    public void setProfileSettings(ProfileSettings profileSettings){
        this.profileSettings = profileSettings;
    }

    public ProfileSettings getProfileSettings(){
        return profileSettings;
    }

    public void setVideoQuality(int videoQuality){
        this.videoQuality = videoQuality;
    }

    public int getVideoQuality(){
        return videoQuality;
    }

    public int getHighspeedFPS() {
        return highspeedFPS;
    }

    public void setHighspeedFPS(int highspeedFPS) {
        this.highspeedFPS = highspeedFPS;
    }

    public boolean isSubtitleEnabled() {
        return subtitleEnabled;
    }

    public void setSubtitleEnabled(boolean subtitleEnabled) {
        this.subtitleEnabled = subtitleEnabled;
    }

    public @Nullable String getSubtitleTxt() {
        return subtitleTxt;
    }

    public void setSubtitleTxt(String subtitleTxt) {
        this.subtitleTxt = subtitleTxt;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getNumVideoFile() {
        return numVideoFile;
    }

    public void setNumVideoFile(int numVideoFile) {
        this.numVideoFile = numVideoFile;
    }

    public boolean isAdvertEnabled() {
        return advertEnabled;
    }

    public void setAdvertEnabled(boolean advertEnabled) {
        this.advertEnabled = advertEnabled;
    }

    public String getAdvertFile() {
        return advertFile;
    }

    public void setAdvertFile(String advertFile) {
        this.advertFile = advertFile;
    }

    public String getProfileName(){
        return  profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }
}
