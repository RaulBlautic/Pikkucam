package com.blautic.trainingapp.entity

enum class Status(
    val value: Int,
    val text: String,
) {
    NO_TRAINING(0, "No entrenado"),
    TRAINING_STARTED(1, "Entrenando"),
    TRAINING_SUCCEEDED(2, "Entrenado"),
    TRAINING_FAILED(3, "Fallido");

}