package com.blautic.pikkucam.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import static android.icu.text.MessagePattern.ArgType.SELECT;
import static androidx.room.OnConflictStrategy.REPLACE;

/*
Objeto Dao que define las consultas a realizar con la base de datos
 */
@Dao
public interface DevicesDao {

    @Query("SELECT * FROM Devices")
    Devices loadAll();

    @Query("SELECT MAC1 FROM Devices")
    String getMac1();

    @Query("SELECT MAC2 FROM Devices")
    String getMac2();

    @Query("SELECT MAC3 FROM Devices")
    String getMac3();

    @Query("SELECT MAC4 FROM Devices")
    String getMac4();

    @Query("UPDATE devices SET MAC1 = :mac1 , IS_BLE5 = :isBle5")
    void setMac1(String mac1, boolean isBle5);

    @Query("UPDATE devices SET MAC2 = :mac2")
    void setMac2(String mac2);

    @Query("UPDATE devices SET MAC3 = :mac3")
    void setMac3(String mac3);

    @Query("UPDATE devices SET MAC4 = :mac4")
    void setMac4(String mac4);

    @Update
    void update(Devices devices);

    @Insert(onConflict = REPLACE)
    void insert(Devices devices);

}
