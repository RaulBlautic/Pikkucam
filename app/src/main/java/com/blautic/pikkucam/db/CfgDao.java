package com.blautic.pikkucam.db;

import android.bluetooth.BluetoothClass;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface CfgDao {

    @Insert
    void insert(Cfg cfg);

    @Update
    void update(Cfg cfg);


}
