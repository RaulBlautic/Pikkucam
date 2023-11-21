package com.blautic.pikkucam.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.blautic.trainingapp.sensor.mpu.Mpu;


/**
 * Definición de la base de datos. Se le indica las entidades a utilizar, la versión y los conversores a utilizar
 */
@Database(
        entities = {Devices.class, ProfileSettings.class, VideoToProcess.class},
        version = 25, exportSchema = false)

@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract DevicesDao devicesDao();
    public abstract ProfileSettingsDao profileSettingsDao();
    public abstract VideoToProcessDao videoToProcessDao();
}
