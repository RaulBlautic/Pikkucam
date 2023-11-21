package com.blautic.pikkucam.api.response

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class Model(
    @SerialName("devices")
    val devices: List<Device>,
    @SerialName("fkOwner")
    val fkOwner: Int,
    @SerialName("fkTipo")
    val fkTipo: Int,
    @SerialName("fldBAutoTraining")
    val fldBAutoTraining: Boolean,
    @SerialName("fldDTimeCreateTime")
    val fldDTimeCreateTime: String,
    @SerialName("fldNDuration")
    val fldNDuration: Int,
    @SerialName("fldNProgress")
    val fldNProgress: Int? = null,
    @SerialName("fldSDescription")
    val fldSDescription: String,
    @SerialName("fldSImage")
    val fldSImage: String? = null,
    @SerialName("fldSName")
    val fldSName: String,
    @SerialName("fldSStatus")
    val fldSStatus: Int,
    @SerialName("fldSUrl")
    val fldSUrl: String? = null,
    @SerialName("fldSVideo")
    val fldSVideo: String? = null,
    @SerialName("id")
    val id: Int,
    @SerialName("movements")
    val movements: List<Movement>,
    @SerialName("versions")
    val versions: List<Version>,
)