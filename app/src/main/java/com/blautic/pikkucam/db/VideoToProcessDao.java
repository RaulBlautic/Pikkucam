package com.blautic.pikkucam.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface VideoToProcessDao {

    @Insert
    long insert(VideoToProcess videoToProcess);

    @Update
    public void update(VideoToProcess videoToProcess);

    @Query("SELECT * FROM VideoToProcess")
    public VideoToProcess getVideoToProcess();

    @Query("SELECT * FROM VideoToProcess")
    public List<VideoToProcess> getVideoListToProcess();

    @Query("DELETE FROM VideoToProcess WHERE id=:id")
    public void deleteByKey(float id);
}
