package ca.amandeep.bcbpscanner

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.amandeep.bcbpscanner.ScanningViewModel.State
import ca.amandeep.bcbpscanner.databinding.CameraBinding
import ca.amandeep.bcbpscanner.ui.boardingpass.BoardingPass
import ca.amandeep.bcbpscanner.ui.camera.CameraOverlay
import ca.amandeep.bcbpscanner.ui.camera.PermissionRequestScreen
import ca.amandeep.bcbpscanner.ui.theme.BCBPScannerTheme
import com.github.ajalt.timberkt.d
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class ScanningActivity : ComponentActivity() {
    private val viewModel by viewModels<ScanningViewModel>()
    private val importBarcodeLauncher = ImportBarcodeLauncher(
        activity = this,
        onBarcodeSelected = { viewModel.setDetected(it) },
        onCancel = { viewModel.setStateDetecting() }
    )

    @OptIn(
        ExperimentalMaterialApi::class,
        ExperimentalPermissionsApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scanningCameraController = ScanningCameraController(
            lifecycleOwner = this,
            viewModelProvider = ViewModelProvider(this)
        )

        setContent {
            val state by viewModel.stateFlow.collectAsStateWithLifecycle()
            val flashOn by viewModel.flashOnFlow.collectAsStateWithLifecycle()

            val cameraPermissionState =
                rememberPermissionState(permission = Manifest.permission.CAMERA)
            // Start detecting when the permission is granted.
            LaunchedEffect(cameraPermissionState) {
                if (cameraPermissionState.status.isGranted) {
                    viewModel.setStateDetecting()
                }
            }

            BCBPScannerTheme {
                val sheetState = rememberModalBottomSheetState(
                    initialValue = ModalBottomSheetValue.Hidden,
                    skipHalfExpanded = true
                )
                // Show the sheet when there is a barcode detected.
                LaunchedEffect(state) {
                    if (state is State.Detected) {
                        sheetState.show()
                    }
                }
                // When the sheet is hidden, go back to detecting.
                LaunchedEffect(sheetState.isVisible) {
                    if (!sheetState.isVisible && state is State.Detected) {
                        viewModel.state = State.Detecting
                    }
                }

                // The bottom sheet is used to display the boarding pass.
                ModalBottomSheetLayout(
                    sheetState = sheetState,
                    scrimColor = Color.Transparent,
                    sheetShape = MaterialTheme.shapes.medium,
                    sheetContent = {
                        Crossfade(state) {
                            Box {
                                // Hack to prevent a crash when the sheet is hidden and has no content
                                when (it) {
                                    is State.Detected -> BoardingPass(it.code)
                                    else -> Spacer(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(100.dp)
                                    )
                                }
                            }
                        }
                    }
                ) {
                    if (cameraPermissionState.status.isGranted) {
                        Box(Modifier.fillMaxSize()) {
                            // The camera preview is an Android view
                            AndroidViewBinding(
                                modifier = Modifier.fillMaxSize(),
                                factory = { inflater, parent, attachToParent ->
                                    CameraBinding.inflate(inflater, parent, attachToParent).apply {
                                        d { "Camera preview update" }
                                        scanningCameraController.attachPreview(
                                            cameraPreview = cameraPreview,
                                            cameraPreviewGraphicOverlay = cameraPreviewGraphicOverlay
                                        )
                                        viewModel.setStateDetecting()
                                    }
                                }
                            )
                            CameraOverlay(
                                state = state,
                                isFlashOn = flashOn,
                                setFlash = { viewModel.flashOn = !flashOn },
                                onOpenLibrary = importBarcodeLauncher::launch
                            )
                        }
                    } else {
                        PermissionRequestScreen(
                            onOpenLibrary = importBarcodeLauncher::launch,
                            launchCameraPermissionRequest = cameraPermissionState::launchPermissionRequest
                        )
                    }
                }
            }
        }
    }
}
