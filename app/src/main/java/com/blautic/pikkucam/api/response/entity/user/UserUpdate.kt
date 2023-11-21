package com.blautic.exermeter.entity.user

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class UserUpdate(
    @SerialName("fkRol")
    val fkRol: Int,
    @SerialName("fldBActive")
    val fldBActive: Boolean,
    @SerialName("fldSDireccion")
    val fldSDireccion: String,
    @SerialName("fldSEmail")
    val fldSEmail: String,
    @SerialName("fldSFcmToken")
    val fldSFcmToken: String,
    @SerialName("fldSFullName")
    val fldSFullName: String,
    @SerialName("fldSImagen")
    val fldSImagen: String,
    @SerialName("fldSTelefono")
    val fldSTelefono: String,
    @SerialName("password")
    val password: String,
)