package com.blautic.exermeter.entity.user

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class CreateUser(
    @SerialName("fkRol")
    val fkRol: Int,
    @SerialName("fldBActive")
    val fldBActive: Boolean = true,
    @SerialName("fldSDireccion")
    val fldSDireccion: String = "",
    @SerialName("fldSEmail")
    val fldSEmail: String,
    @SerialName("fldSFullName")
    val fldSFullName: String,
    @SerialName("fldSHashedPassword")
    val fldSHashedPassword: String,
    @SerialName("fldSImagen")
    val fldSImagen: String = "",
    @SerialName("fldSTelefono")
    val fldSTelefono: String = "",
)