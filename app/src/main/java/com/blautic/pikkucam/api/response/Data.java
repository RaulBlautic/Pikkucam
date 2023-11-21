package com.blautic.pikkucam.api.response;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Data implements Parcelable {
    public int sample;
    @SerializedName("acc_x")
    public float accX;
    @SerializedName("acc_y")
    public float accY;
    @SerializedName("acc_z")
    public float accZ;
    @SerializedName("gyr_x")
    public float gyrX;
    @SerializedName("gyr_y")
    public float gyrY;
    @SerializedName("gyr_z")
    public float gyrZ;
    @SerializedName("n_device")
    public int nDevice;

    protected Data(Parcel in) {
        sample = in.readInt();
        accX = in.readFloat();
        accY = in.readFloat();
        accZ = in.readFloat();
        gyrX = in.readFloat();
        gyrY = in.readFloat();
        gyrZ = in.readFloat();
        nDevice = in.readInt();
    }

    public static final Creator<Data> CREATOR = new Creator<Data>() {
        @Override
        public Data createFromParcel(Parcel in) {
            return new Data(in);
        }

        @Override
        public Data[] newArray(int size) {
            return new Data[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(sample);
        parcel.writeFloat(accX);
        parcel.writeFloat(accY);
        parcel.writeFloat(accZ);
        parcel.writeFloat(gyrX);
        parcel.writeFloat(gyrY);
        parcel.writeFloat(gyrZ);
        parcel.writeInt(nDevice);
    }
}
