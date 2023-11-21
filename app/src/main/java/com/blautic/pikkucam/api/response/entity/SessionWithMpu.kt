package com.blautic.trainingapp.entity


import com.blautic.trainingapp.entity.training.CaptureOtherState
import com.blautic.trainingapp.sensor.mpu.Mpu

data class SessionWithMpu(
    var id: Int = 0,
    var file: String,
    var category: Category = Category.MOV,
    var mpu: MutableList<Mpu> = mutableListOf(),
    var ownerID: Int = 0,
    var isOnlyMov: CaptureOtherState = CaptureOtherState.NO_CAPTURE
)
