package ca.amandeep.bcbpscanner.ui.camera

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.amandeep.bcbpscanner.R
import ca.amandeep.bcbpscanner.ui.theme.BCBPScannerTheme

/**
 * Show a screen for asking the user to grant permission or import a photo from the library.
 *
 * @param onOpenLibrary A function that opens the photo library on the phone.
 * @param launchCameraPermissionRequest A function that launches the camera permission request.
 */
@Composable
fun PermissionRequestScreen(
    onOpenLibrary: () -> Unit,
    launchCameraPermissionRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column {
            Spacer(modifier = Modifier.fillMaxHeight(0.3f))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.camera_rationale),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.camera_rationale_caveat),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.size(25.dp))
                Column(Modifier.fillMaxWidth(0.5f)) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = launchCameraPermissionRequest
                    ) {
                        Text(stringResource(R.string.request_permission))
                    }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onOpenLibrary
                    ) {
                        Text(stringResource(R.string.open_photo_library))
                    }
                }
            }
        }
    }
}

@Composable
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
private fun PermissionRequestScreenPreview() {
    BCBPScannerTheme {
        PermissionRequestScreen(
            onOpenLibrary = {},
            launchCameraPermissionRequest = {}
        )
    }
}
