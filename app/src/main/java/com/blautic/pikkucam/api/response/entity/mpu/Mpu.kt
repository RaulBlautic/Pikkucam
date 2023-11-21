package com.blautic.trainingapp.sensor.mpu

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import com.blautic.pikkucam.api.response.entity.mpu.BleBytesParser
import com.blautic.pikkucam.api.response.entity.mpu.BleBytesParser.Companion.FORMAT_SINT16
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Mpu(
    var sample: Int = 0,

    var accX: Float = 0.0f,
    var accY: Float = 0.0f,
    var accZ: Float = 0.0f,

    var gyrX: Float = 0f,
    var gyrY: Float = 0f,
    var gyrZ: Float = 0f,

    var nDevice: Int,
): Parcelable {
    @kotlinx.serialization.Transient
    private val accScale = AccScale.ACC_SCALE_4G.ratio

    @kotlinx.serialization.Transient
    private val gyrScale: Float = GyrScale.GYR_SCALE_1000.ratio

    var OFFSET_ACX = 0.25f //-0.25f;

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readInt()
    ) {
        OFFSET_ACX = parcel.readFloat()
    }

    fun setData(parse: BleBytesParser) {

        if (parse.getValue().size >= 8) {
            sample = parse.getIntValue(FORMAT_SINT16)
            // sample++;
            accX = (parse.getIntValue(FORMAT_SINT16) * accScale)
            accY = parse.getIntValue(FORMAT_SINT16) * accScale
            accZ = parse.getIntValue(FORMAT_SINT16) * accScale
        }

        if (parse.getValue().size >= 14) {
            gyrX = parse.getIntValue(FORMAT_SINT16) * gyrScale
            gyrY = parse.getIntValue(FORMAT_SINT16) * gyrScale
            gyrZ = parse.getIntValue(FORMAT_SINT16) * gyrScale
        }

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(sample)
        parcel.writeFloat(accX)
        parcel.writeFloat(accY)
        parcel.writeFloat(accZ)
        parcel.writeFloat(gyrX)
        parcel.writeFloat(gyrY)
        parcel.writeFloat(gyrZ)
        parcel.writeInt(nDevice)
        parcel.writeFloat(OFFSET_ACX)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Mpu> {
        override fun createFromParcel(parcel: Parcel): Mpu {
            return Mpu(parcel)
        }

        override fun newArray(size: Int): Array<Mpu?> {
            return arrayOfNulls(size)
        }
    }


}
