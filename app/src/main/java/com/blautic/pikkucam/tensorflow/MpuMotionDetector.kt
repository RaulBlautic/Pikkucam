package com.blautic.trainingapp.android.tensorflow

import android.os.SystemClock
import android.util.Log
import com.blautic.pikkucam.api.response.Model
import com.blautic.pikkucam.tensorflow.ArrayQueue
import com.blautic.trainingapp.sensor.mpu.AccScale
import com.blautic.trainingapp.sensor.mpu.GyrScale
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import timber.log.Timber

class MpuMotionDetector(private val model: Model) {

    interface MotionDetectorListener {
        fun onCorrectMotionRecognized(correctProb: Float)
        fun onOutputScores(outputScores: FloatArray)
        fun onTryMotionMotionRecognized(correctProb: Float)
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val lock = Any()

    private val GESTURE_SAMPLES: Int = model.fldNDuration * 20
    private val NUM_DEVICES: Int = model.devices.size

    private var motionDetectorListener: MotionDetectorListener? = null

    private val outputScores: Array<FloatArray> = Array(1) { FloatArray(model.movements.size) }
    private val recordingData: Array<ArrayQueue?> =
        Array(5) { ArrayQueue(GESTURE_SAMPLES * NUM_CHANNELS) }

    private var inferenceInterface: Interpreter? = null
    private var options: Interpreter.Options? = null

    var isStarted = false
        private set

    var selectMovIndex = 0

    fun setMotionDetectorListener(motionDetectorListener: MotionDetectorListener?) {
        this.motionDetectorListener = motionDetectorListener
    }

    fun start() {
        val conditions = CustomModelDownloadConditions.Builder().build()
        FirebaseModelDownloader.getInstance()
            .getModel("mm_mpu_" + model.id.toString(), DownloadType.LATEST_MODEL, conditions)
            .addOnSuccessListener { customModel: CustomModel ->
                val modelFile = customModel.file
                modelFile?.let { file ->
                    coroutineScope.launch {
                        synchronized(lock) {
                            val compatList = CompatibilityList()
                            options = Interpreter.Options()
                            when {
                                compatList.isDelegateSupportedOnThisDevice -> {
                                    // if the device has a supported GPU, add the GPU delegate
                                    val delegateOptions = compatList.bestOptionsForThisDevice
                                    options!!.addDelegate(GpuDelegate(delegateOptions))
                                }

                                else -> {
                                    // if the GPU is not supported, run on 4 threads
                                    options!!.setNumThreads(Companion.NUM_LITE_THREADS)
                                }
                            }
                            inferenceInterface = Interpreter(file, options)
                            isStarted = true
                        }
                    }
                }
            }.addOnFailureListener { t: Exception? -> Timber.e(t) }
    }

    fun stop() {
        coroutineScope.cancel()
        synchronized(lock) {
            isStarted = false
        }
        inferenceInterface?.let {
            it.close()
            inferenceInterface = null
        }
    }

    var threshold: Float
        get() = RISE_THRESHOLD * 100
        set(threshold) {
            RISE_THRESHOLD = threshold / 100.0f
        }

    fun onMpuChanged(mpu: com.blautic.trainingapp.sensor.mpu.Mpu) {
        if (!isStarted) return

        val device = mpu.nDevice

        recordingData[device]?.let { queue ->
            queue.queueEnqueue(mpu.accX / AccScale.ACC_SCALE_4G.value)
            queue.queueEnqueue(mpu.accY / AccScale.ACC_SCALE_4G.value)
            queue.queueEnqueue(mpu.accZ / AccScale.ACC_SCALE_4G.value)
            queue.queueEnqueue(mpu.gyrX / GyrScale.GYR_SCALE_1000.value)
            queue.queueEnqueue(mpu.gyrY / GyrScale.GYR_SCALE_1000.value)
            queue.queueEnqueue(mpu.gyrZ / GyrScale.GYR_SCALE_1000.value)
        }

        coroutineScope.launch {
            processData()
        }

    }

    private val gestureSendScore: Long = -1

    private fun processData() {

        var correctProbability = 0f
        var gestureTimeMs: Long
        var scores = FloatArray(0)

        synchronized(lock) {
            inferenceInterface?.takeIf { isStarted }?.let { interpreter ->

                val testArray = Array(1) {
                    Array(model.fldNDuration * 20) {
                        Array(model.devices.size * NUM_CHANNELS) { FloatArray(1) }
                    }
                }

                val sampleArray =
                    Array(GESTURE_SAMPLES) { Array(NUM_DEVICES * NUM_CHANNELS) { FloatArray(1) } }

                for (sample in 0 until GESTURE_SAMPLES) {
                    for (device in 0 until NUM_DEVICES) {
                        for (sensor in 0 until NUM_CHANNELS) {
                            val value =
                                recordingData[1]!!.queue[sample * 6 + sensor]
                            sampleArray[sample][device * 6 + sensor] = floatArrayOf(value)
                        }
                    }
                }

                testArray[0] = sampleArray

                Log.d("DATO ENRIC", "${outputScores[0][0]} \n\n ${outputScores[0][1]}")

                interpreter.run(testArray, outputScores)

                correctProbability = outputScores[0][selectMovIndex]
                gestureTimeMs = (SystemClock.elapsedRealtimeNanos() - gestureSendScore) / 1000000
                if (gestureTimeMs > model.fldNDuration * MIN_GESTURE_TIME_MS) {
                    scores = FloatArray(model.movements.size)
                    for (i in 0 until model.movements.size) {
                        scores[i] = outputScores[0][i] * 100
                    }
                }
            }
        }

        coroutineScope.launch(Dispatchers.Main) {
            detectGestures(correctProbability)
            if (scores.isNotEmpty()) {
                motionDetectorListener?.onOutputScores(scores)
            }
        }
    }

    private var gestureStartTime: Long = -1
    private var gestureStartTimeTry: Long = -1

    private var tryFlag = false
    private var correctFlag = false

    private fun detectGestures(correctProb: Float) {

        if (correctProb >= RISE_THRESHOLD) {
            correctFlag = true
            val gestureTimeMs = (SystemClock.elapsedRealtimeNanos() - gestureStartTime) / 1000000
            if (gestureTimeMs > model.fldNDuration * MIN_GESTURE_TIME_MS) {
                gestureStartTime = SystemClock.elapsedRealtimeNanos()
                //motionDetectorListener!!.onCorrectMotionRecognized(correctProb, angles)
            }
        } else if (correctProb < RISE_THRESHOLD && correctProb > (RISE_THRESHOLD - 0.3f)) {
            tryFlag = true

            val gestureTimeMs = (SystemClock.elapsedRealtimeNanos() - gestureStartTimeTry) / 1000000
            if (gestureTimeMs > model.fldNDuration * MIN_GESTURE_TIME_MS) {
                gestureStartTimeTry = SystemClock.elapsedRealtimeNanos()
                //motionDetectorListener!!.onTryMotionRecognized(correctProb, angles)
            }

        } else {
            if(correctFlag){
                motionDetectorListener!!.onCorrectMotionRecognized(correctProb)
                correctFlag = false
                tryFlag = false
                Timber.d("onCorrectMotionRecognized")
            } else if(tryFlag){
                motionDetectorListener!!.onTryMotionMotionRecognized(correctProb)
                tryFlag = false
                correctFlag = false
                Timber.d("onTryMotionRecognized")
            }
        }
    }

    companion object {
        private const val NUM_CHANNELS = 6
        private const val MIN_GESTURE_TIME_MS: Long = 800
        private var RISE_THRESHOLD = 0.80f
        private var SPACE_BETWEEN_SAMPLES = 10
        private val NUM_LITE_THREADS = 4
    }

    init {
        model.movements.sortedBy { it.fldSLabel }.forEachIndexed { index, movement ->
            if (movement.fldSLabel != "Other") selectMovIndex = index
        }
    }

}