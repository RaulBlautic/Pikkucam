package com.blautic.exermeter.entity.ecg

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class Ecg(
    @SerialName("fldNSample")
    var fldNSample: Int = 0,
    @SerialName("fldNTime")
    var fldNTime: Int = 0,
    @SerialName("fldFData")
    var fldFData: Float = 0f,
    @SerialName("fldSType")
    var fldSType: String = " ",
    @SerialName("fkDevice")
    var fkDevice: Int = 0,
    @SerialName("fkOwner")
    var fkOwner: Int = 0,
)
