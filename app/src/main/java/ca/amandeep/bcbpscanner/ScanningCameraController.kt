@file:Suppress("DEPRECATION")

package ca.amandeep.bcbpscanner

import android.Manifest
import android.hardware.Camera
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import com.github.ajalt.timberkt.d
import com.github.ajalt.timberkt.e
import com.google.mlkit.md.camera.CameraSource
import com.google.mlkit.md.camera.CameraSourcePreview
import com.google.mlkit.md.camera.GraphicOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import splitties.experimental.ExperimentalSplittiesApi
import splitties.permissions.hasPermission
import java.io.IOException

/**
 * A controller to manage the camera preview and detection pipeline.
 *
 * This controller uses a [CameraSource] to continuously stream images from the camera and
 * detect barcodes in the images. The [CameraSource] uses a [CameraSourcePreview] to
 * display the images from the camera.
 *
 * The controller also manages the state of the camera preview and the detected barcodes. It
 * automatically stops and restarts the camera preview when the activity is paused and resumed.
 */
@OptIn(ExperimentalSplittiesApi::class)
class ScanningCameraController(
    private val lifecycleOwner: LifecycleOwner,
    viewModelProvider: ViewModelProvider
) : DefaultLifecycleObserver {
    private val viewModel = viewModelProvider.get<ScanningViewModel>()

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null

    init {
        lifecycleOwner.lifecycle.addObserver(this)

        lifecycleOwner.lifecycleScope.launch {
            viewModel.stateFlow.collectLatest { state ->
                d { "Current workflow state: ${state.javaClass}" }
                when (state) {
                    ScanningViewModel.State.Stopped,
                    is ScanningViewModel.State.DetectedAndStopped ->
                        preview?.stop()

                    ScanningViewModel.State.Detecting ->
                        if (cameraSource == null || !hasPermission(Manifest.permission.CAMERA)) {
                            viewModel.setStateStopped()
                        } else {
                            startCameraPreview()
                        }

                    ScanningViewModel.State.Searching,
                    is ScanningViewModel.State.Detected ->
                        Unit
                }
            }
        }
        lifecycleOwner.lifecycleScope.launch {
            viewModel.flashOnFlow.collectLatest(::setFlash)
        }
    }

    private fun setFlash(isFlashOn: Boolean) {
        d { "update flash $isFlashOn" }
        cameraSource?.updateFlashMode(
            if (isFlashOn) {
                Camera.Parameters.FLASH_MODE_TORCH
            } else {
                Camera.Parameters.FLASH_MODE_OFF
            }
        )
    }

    private fun releasePreview() {
        cameraSource?.release()
        cameraSource = null
    }

    fun attachPreview(
        cameraPreview: CameraSourcePreview,
        cameraPreviewGraphicOverlay: GraphicOverlay
    ) {
        releasePreview()
        preview = cameraPreview
        graphicOverlay = cameraPreviewGraphicOverlay.apply {
            val graphicOverlay = this
            cameraSource = CameraSource(this).apply {
                setFrameProcessor(BarcodeProcessor(graphicOverlay, viewModel))
            }
        }
        if (!viewModel.state.isStopped) {
            startCameraPreview()
        }
    }

    private fun startCameraPreview() {
        val cameraSource = this.cameraSource ?: return

        try {
            preview?.start(cameraSource)
            lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                setFlash(viewModel.flashOn)
            }
        } catch (e: IOException) {
            e(e) { "Failed to start camera preview!" }
            cameraSource.release()
            this.cameraSource = null
        }
    }

    override fun onResume(owner: LifecycleOwner) = viewModel.setStateDetecting()

    override fun onPause(owner: LifecycleOwner) = viewModel.setStateStopped()

    override fun onDestroy(owner: LifecycleOwner) = releasePreview()
}
