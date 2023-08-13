package ca.amandeep.bcbpscanner.ui.camera

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.amandeep.bcbpscanner.R
import ca.amandeep.bcbpscanner.ScanningViewModel

/**
 * The overlay that is displayed on top of the camera preview.
 * It contains the flash button, the library button, and the instructions.
 *
 * @param isFlashOn Whether the flash is on or not.
 * @param setFlash A function that sets the flash on or off.
 * @param onOpenLibrary A function that opens the photo library on the phone.
 */
@Composable
fun CameraOverlay(
    state: ScanningViewModel.State,
    isFlashOn: Boolean,
    setFlash: (Boolean) -> Unit,
    onOpenLibrary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Background will be assumed to be dark, so content color should be bright/white
    CompositionLocalProvider(
        LocalContentColor provides Color.White,
    ) {
        Column(modifier) {
            Spacer(modifier = Modifier.fillMaxHeight(0.1f))
            Crossfade(state !is ScanningViewModel.State.Detected) { isScanning ->
                if (isScanning) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.find_instruction),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(2f, 2f),
                                blurRadius = 8f,
                            ),
                        ),
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                Modifier
                    .fillMaxHeight(0.3f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledIconButton(
                    onClick = { setFlash(!isFlashOn) },
                    modifier = Modifier.size(70.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = LocalContentColor.current.copy(alpha = 0.2f),
                    ),
                ) {
                    Icon(
                        modifier = Modifier.size(35.dp),
                        painter = painterResource(
                            if (isFlashOn) {
                                R.drawable.ic_flash_on
                            } else {
                                R.drawable.ic_flash_off
                            },
                        ),
                        contentDescription = stringResource(R.string.cd_flash_button),
                    )
                }
                Spacer(modifier = Modifier.fillMaxWidth(0.1f))
                FilledIconButton(
                    onClick = onOpenLibrary,
                    modifier = Modifier.size(70.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = LocalContentColor.current.copy(alpha = 0.2f),
                    ),
                ) {
                    Icon(
                        modifier = Modifier.size(35.dp),
                        painter = painterResource(R.drawable.ic_photo_library),
                        contentDescription = stringResource(R.string.open_photo_library),
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun CameraOverlayPreview() {
    Box(Modifier.background(Color.Black)) {
        CameraOverlay(
            state = ScanningViewModel.State.Detecting,
            isFlashOn = false,
            setFlash = {},
            onOpenLibrary = {},
        )
    }
}
