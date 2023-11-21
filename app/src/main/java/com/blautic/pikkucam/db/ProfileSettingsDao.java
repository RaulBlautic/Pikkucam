package com.blautic.pikkucam.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProfileSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProfileSettings profileSettings);

    @Update
    void update(ProfileSettings profileSettings);

    @Query("DELETE FROM PROFILESETTINGS WHERE id = :myid")
    void delete(int myid);

    @Query("DELETE FROM PROFILESETTINGS WHERE name = :myname")
    void delete(String myname);

    @Query("SELECT * FROM PROFILESETTINGS")
    ProfileSettings getProfileSettings();

    @Query("SELECT * FROM PROFILESETTINGS")
    LiveData<List<ProfileSettings>> getProfileSettingsLive();

    @Query("SELECT * FROM PROFILESETTINGS")
    ProfileSettings[] getProfileSettingsArray();

    @Query("SELECT * FROM PROFILESETTINGS WHERE id = :myid")
    ProfileSettings getProfileSettings(int myid);

    @Query("SELECT * FROM PROFILESETTINGS WHERE name = :nombre")
    ProfileSettings getProfileSettings(String nombre);

    @Query("SELECT * FROM PROFILESETTINGS WHERE name = :nombre")
    LiveData<ProfileSettings> getProfileSettingsData(String nombre);

}
