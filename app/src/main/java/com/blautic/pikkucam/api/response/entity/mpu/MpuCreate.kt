package com.blautic.trainingapp.entity.mpu

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class MpuCreate(
    @SerialName("fldNSample")
    var fldNSample: Int = 0,
    @SerialName("fldFAccX")
    var fldFAccX: Float = 0f,
    @SerialName("fldFAccY")
    var fldFAccY: Float = 0f,
    @SerialName("fldFAccZ")
    var fldFAccZ: Float = 0f,
    @SerialName("fldFGyrX")
    var fldFGyrX: Float = 0f,
    @SerialName("fldFGyrY")
    var fldFGyrY: Float = 0f,
    @SerialName("fldFGyrZ")
    var fldFGyrZ: Float = 0f,
    @SerialName("fkDevice")
    var fkDevice: Int = 0,
)
