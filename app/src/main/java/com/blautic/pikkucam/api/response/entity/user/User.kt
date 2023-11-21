package com.blautic.exermeter.entity.user

import kotlinx.serialization.SerialName


@kotlinx.serialization.Serializable
data class User(
    @SerialName("estado")
    val estado: Int,
    @SerialName("fkRol")
    val fkRol: Int,
    @SerialName("fldBActive")
    val fldBActive: Boolean,
    @SerialName("fldSDireccion")
    val fldSDireccion: String? = "",
    @SerialName("fldSEmail")
    val fldSEmail: String,
    @SerialName("fldSFullName")
    val fldSFullName: String,
    @SerialName("fldSImagen")
    val fldSImagen: String? = "",
    @SerialName("fldSTelefono")
    val fldSTelefono: String? = "",
    @SerialName("id")
    val id: Int,
    @SerialName("idRelacion")
    val idRelacion: Int,
)