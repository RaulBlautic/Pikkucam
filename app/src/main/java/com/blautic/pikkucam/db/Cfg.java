package com.blautic.pikkucam.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Cfg {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "Drive")
    public boolean drive;



}
