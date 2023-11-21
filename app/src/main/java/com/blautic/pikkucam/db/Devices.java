package com.blautic.pikkucam.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/*
Entidad Devices que le indica a la db como tiene que diseñar la tabla
 */
@Entity
public class Devices {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "MAC1")
    public String mac1;

    @ColumnInfo(name = "MAC2")
    public String mac2;

    @ColumnInfo(name = "MAC3")
    public String mac3;

    @ColumnInfo(name = "MAC4")
    public String mac4;

    @ColumnInfo(name = "DATE")
    public Date date;

    @ColumnInfo(name = "IS_BLE5")
    public boolean isBle5;

    public void setMac1(String mac1) {
        this.mac1 = mac1;
    }

    public void setMac2(String mac2){
        this.mac2 = mac2;
    }

    public void setMac3(String mac3) {
        this.mac3 = mac3;
    }

    public void setMac4(String mac4) {
        this.mac4 = mac4;
    }

    public String getMac1() {
        return mac1;
    }

    public String getMac2() {
        return mac2;
    }

    public String getMac3() {
        return mac3;
    }

    public String getMac4() {
        return mac4;
    }

    public void setDate(Date date){
        this.date = date;
    }

    public Devices (String myMac1){
        mac1 = myMac1;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public boolean getBle5() {
        return isBle5;
    }

    public void setBle5(boolean ble5) {
        isBle5 = ble5;
    }

    public Devices (){

    }
}
