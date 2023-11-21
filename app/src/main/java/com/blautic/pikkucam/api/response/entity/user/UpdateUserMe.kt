package com.blautic.exermeter.entity.user

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class UpdateUserMe(
    @SerialName("email")
    val email: String,
    @SerialName("full_name")
    val full_name: String,
    @SerialName("password")
    val password: String,
)