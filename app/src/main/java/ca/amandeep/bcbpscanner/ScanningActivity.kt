@file:OptIn(ExperimentalMaterial3Api::class)

package ca.amandeep.bcbpscanner

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
        onCancel = { viewModel.setStateDetecting() },
    )

    @OptIn(
        ExperimentalPermissionsApi::class,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scanningCameraController = ScanningCameraController(
            lifecycleOwner = this,
            viewModelProvider = ViewModelProvider(this),
        )

        setContent {
            val state by viewModel.stateFlow.collectAsStateWithLifecycle()
            val flashOn by viewModel.flashOnFlow.collectAsStateWithLifecycle()

            val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
            // Start detecting when the permission is granted.
            LaunchedEffect(cameraPermissionState) {
                if (cameraPermissionState.status.isGranted) {
                    viewModel.setStateDetecting()
                }
            }

            BCBPScannerTheme {
                val sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = true,
                )
                // When the sheet is hidden, go back to detecting.
                LaunchedEffect(sheetState.isVisible) {
                    if (!sheetState.isVisible) {
                        viewModel.setStateDetecting()
                    }
                }
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
                                        cameraPreviewGraphicOverlay = cameraPreviewGraphicOverlay,
                                    )
                                    viewModel.setStateDetecting()
                                }
                            },
                        )
                        CameraOverlay(
                            state = state,
                            isFlashOn = flashOn,
                            setFlash = { viewModel.flashOn = !flashOn },
                            onOpenLibrary = importBarcodeLauncher::launch,
                        )
                    }
                } else {
                    PermissionRequestScreen(
                        onOpenLibrary = importBarcodeLauncher::launch,
                        launchCameraPermissionRequest = cameraPermissionState::launchPermissionRequest,
                    )
                }

                if (state is State.Detected) {
                    // The bottom sheet is used to display the boarding pass.
                    ModalBottomSheet(
                        sheetState = sheetState,
                        dragHandle = {},
                        onDismissRequest = {
                            viewModel.setStateDetecting()
                        },
                    ) {
                        state.let {
                            if (it is State.Detected) {
                                Box {
                                    BoardingPass(it.code)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
