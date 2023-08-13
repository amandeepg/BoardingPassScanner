package ca.amandeep.bcbpscanner

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.mlkit.vision.common.InputImage
import com.ncredinburgh.iata.model.IataCode
import splitties.alertdialog.appcompat.messageResource
import splitties.alertdialog.appcompat.okButton
import splitties.alertdialog.appcompat.titleResource
import splitties.alertdialog.material.materialAlertDialog

/**
 * Handles the logic of importing a barcode from the user's device.
 * It uses the [ActivityResultContracts.PickVisualMedia] contract to pick an image from the user's
 * device, and then uses the [CropImageContract] contract to crop the image to the barcode.
 * Once the image is cropped, it uses the [BarcodeProcessor] to parse the barcode.
 * If the barcode is successfully parsed, the [onBarcodeSelected] callback is invoked with the
 * parsed barcode.
 * If the barcode is not successfully parsed, the [onCancel] callback is invoked.
 * If the user cancels the import process, the [onCancel] callback is invoked.
 *
 * @param activity The activity that will be used to register the activity result contracts.
 * @param onBarcodeSelected The callback that will be invoked when the barcode is successfully parsed.
 * @param onCancel The callback that will be invoked when the import process is cancelled.
 */
class ImportBarcodeLauncher(
    private val activity: ComponentActivity,
    private val onBarcodeSelected: (IataCode) -> Unit,
    private val onCancel: () -> Unit,
) {
    private val pickMediaLauncher =
        activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            it?.let { cropImage(it) }
                ?: onCancel()
        }

    private val cropImageLauncher =
        activity.registerForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                val image = InputImage.fromFilePath(activity, result.uriContent!!)
                BarcodeProcessor.scanner.process(image)
                    .addOnSuccessListener {
                        val barCode = it?.firstOrNull()?.let { BarcodeProcessor.parseBarcode(it) }
                        if (barCode == null) {
                            activity.materialAlertDialog {
                                titleResource = R.string.barcode_fail_parse_title
                                messageResource = R.string.barcode_fail_parse_body
                                okButton { onCancel() }
                            }.show()
                        } else {
                            onBarcodeSelected(barCode)
                        }
                    }
                    .addOnCanceledListener { onCancel() }
                    .addOnFailureListener { onCancel() }
            } else {
                onCancel()
            }
        }

    private fun cropImage(uri: Uri? = null) =
        cropImageLauncher.launch(
            CropImageContractOptions(
                uri = uri,
                cropImageOptions = CropImageOptions(
                    guidelines = CropImageView.Guidelines.ON,
                    imageSourceIncludeCamera = false,
                ),
            ),
        )

    /**
     * Launches the import process.
     */
    fun launch() =
        if (ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(activity)) {
            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            cropImage()
        }
}
