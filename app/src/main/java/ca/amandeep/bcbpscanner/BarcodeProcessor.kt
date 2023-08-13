package ca.amandeep.bcbpscanner

import android.animation.ValueAnimator
import androidx.annotation.MainThread
import ca.amandeep.bcbpscanner.ScanningViewModel.State
import com.github.ajalt.timberkt.Timber.e
import com.google.android.gms.tasks.Task
import com.google.mlkit.md.barcodedetection.BarcodeLoadingGraphic
import com.google.mlkit.md.barcodedetection.BarcodeReticleGraphic
import com.google.mlkit.md.barcodedetection.BarcodeScrimGraphic
import com.google.mlkit.md.camera.CameraReticleAnimator
import com.google.mlkit.md.camera.FrameProcessorBase
import com.google.mlkit.md.camera.GraphicOverlay
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.ncredinburgh.iata.Parser
import com.ncredinburgh.iata.model.IataCode
import java.io.IOException

/**
 * A processor to run the barcode detector and animate a loading animation when a barcode is valid.
 * When a barcode is detected, it will be parsed and the result will be set in the [ScanningViewModel].
 */
class BarcodeProcessor(
    graphicOverlay: GraphicOverlay,
    private val scanningViewModel: ScanningViewModel,
) :
    FrameProcessorBase<IataCode?>() {

    private val cameraReticleAnimator: CameraReticleAnimator = CameraReticleAnimator(graphicOverlay)

    override fun detectInImage(image: InputImage): Task<IataCode?> =
        scanner.process(image).continueWith {
            if (it.isSuccessful) {
                it.result?.firstOrNull()?.let { parseBarcode(it) }
            } else {
                throw it.exception ?: IOException("Unknown exception!")
            }
        }

    @MainThread
    override fun onSuccess(
        results: IataCode?,
        graphicOverlay: GraphicOverlay,
    ) {
        if (scanningViewModel.state !is State.Detecting) return

        graphicOverlay.clear()
        if (results == null) {
            cameraReticleAnimator.start()
            graphicOverlay += BarcodeReticleGraphic(graphicOverlay, cameraReticleAnimator)
        } else {
            cameraReticleAnimator.cancel()
            if (SHOULD_DELAY) {
                val loadingAnimator = createLoadingAnimator(graphicOverlay, results)
                loadingAnimator.start()
                graphicOverlay += BarcodeLoadingGraphic(graphicOverlay, loadingAnimator)
                scanningViewModel.state = State.Searching
            } else {
                graphicOverlay += BarcodeScrimGraphic(graphicOverlay)
                scanningViewModel.setDetected(results)
            }
        }
        graphicOverlay.invalidate()
    }

    private fun createLoadingAnimator(
        graphicOverlay: GraphicOverlay,
        barcode: IataCode,
    ): ValueAnimator {
        val endProgress = DELAY_LENGTH / 1500f * 1.1f
        return ValueAnimator.ofFloat(0f, endProgress).apply {
            duration = DELAY_LENGTH
            addUpdateListener {
                if ((animatedValue as Float).compareTo(endProgress) >= 0) {
                    graphicOverlay.clear()
                    graphicOverlay += BarcodeScrimGraphic(graphicOverlay)
                    scanningViewModel.setDetected(barcode)
                } else {
                    graphicOverlay.invalidate()
                }
            }
        }
    }

    override fun onFailure(e: Exception) {
        e(e) { "Barcode detection failed!" }
    }

    override fun stop() {
        super.stop()
        try {
            scanner.close()
        } catch (e: IOException) {
            e(e) { "Failed to close barcode detector!" }
        }
    }

    companion object {
        private const val SHOULD_DELAY = true
        private const val DELAY_LENGTH = 1000L

        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_DATA_MATRIX,
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_PDF417,
                    Barcode.FORMAT_AZTEC,
                )
                .build(),
        )

        private val PARSER: Parser = Parser()

        fun parseBarcode(barcode: Barcode): IataCode? =
            try {
                PARSER.parse(barcode.rawValue)
            } catch (e: Exception) {
                null
            }
    }
}
