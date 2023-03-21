package ca.amandeep.bcbpscanner.ui.boardingpass

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import ca.amandeep.bcbpscanner.R
import ca.amandeep.bcbpscanner.ui.theme.BCBPScannerTheme
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import coil.transform.Transformation
import java.util.Locale

private val iconMap = mapOf(
    "AA" to R.drawable.aa,
    "AC" to R.drawable.ac,
    "AD" to R.drawable.ad,
    "AF" to R.drawable.af,
    "AI" to R.drawable.ai,
    "AK" to R.drawable.ak,
    "AM" to R.drawable.am,
    "AS" to R.drawable.`as`,
    "AT" to R.drawable.at,
    "AU" to R.drawable.au,
    "AY" to R.drawable.ay,
    "AV" to R.drawable.av,
    "AZ" to R.drawable.az,
    "B6" to R.drawable.b6,
    "BA" to R.drawable.ba,
    "BR" to R.drawable.br,
    "CX" to R.drawable.cx,
    "DL" to R.drawable.dl,
    "DY" to R.drawable.dy,
    "EI" to R.drawable.ei,
    "EK" to R.drawable.ek,
    "EY" to R.drawable.ey,
    "F8" to R.drawable.f8,
    "F9" to R.drawable.f9,
    "FR" to R.drawable.fr,
    "G3" to R.drawable.g3,
    "G4" to R.drawable.g4,
    "IB" to R.drawable.ib,
    "IX" to R.drawable.ix,
    "JJ" to R.drawable.jj,
    "JL" to R.drawable.jl,
    "KE" to R.drawable.ke,
    "KL" to R.drawable.kl,
    "LA" to R.drawable.la,
    "LH" to R.drawable.lh,
    "LP" to R.drawable.lp,
    "LX" to R.drawable.lx,
    "LY" to R.drawable.ly,
    "MH" to R.drawable.mh,
    "MX" to R.drawable.mx,
    "N0" to R.drawable.n0,
    "NH" to R.drawable.nh,
    "NK" to R.drawable.nk,
    "NZ" to R.drawable.nz,
    "OZ" to R.drawable.oz,
    "PD" to R.drawable.pd,
    "PZ" to R.drawable.pz,
    "QF" to R.drawable.qf,
    "QR" to R.drawable.qr,
    "SK" to R.drawable.sk,
    "SQ" to R.drawable.sq,
    "SU" to R.drawable.su,
    "SV" to R.drawable.sv,
    "SY" to R.drawable.sy,
    "TK" to R.drawable.tk,
    "TS" to R.drawable.ts,
    "U2" to R.drawable.u2,
    "UA" to R.drawable.ua,
    "VA" to R.drawable.va,
    "VS" to R.drawable.vs,
    "W6" to R.drawable.w6,
    "WG" to R.drawable.wg,
    "WN" to R.drawable.wn,
    "WO" to R.drawable.wo,
    "WS" to R.drawable.ws,
    "4C" to R.drawable.x4c,
    "4M" to R.drawable.x4m,
    "6E" to R.drawable.x6e,
    "9W" to R.drawable.x9w,
    "XL" to R.drawable.xl,
    "XP" to R.drawable.xp,
    "Y9" to R.drawable.y9
).mapKeys { it.key.uppercase(Locale.US) }

private val FALLBACK_IMAGE_SIZES = listOf(
    400,
    600,
    800,
    1000,
    1500,
).map { it to it / 4 }
private const val DEBUG_SIZE = false

@Composable
fun Airline(
    airline: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.heightIn(max = 70.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        iconMap[airline]?.let {
            Icon(
                painter = painterResource(id = it),
                contentDescription = "Airline logo",
                tint = Color.Unspecified
            )
        } ?: run {
            val painters = FALLBACK_IMAGE_SIZES.map {
                rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .apply {
                            data(data = "https://pics.avs.io/${it.first}/${it.second}/$airline.png")
                            transformations(TrimTransform)
                            scale(Scale.FIT)
                            size(Size.ORIGINAL)
                        }.build()
                )
            }
            if (painters.all { it.state !is AsyncImagePainter.State.Success }) {
                CircularProgressIndicator(
                    color = Color(0, 0, 0, 150)
                )
            } else {
                Image(
                    painter = painters.last { it.state is AsyncImagePainter.State.Success },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.CenterStart
                )
            }
            if (DEBUG_SIZE) {
                Text(
                    (painters.lastOrNull { it.state is AsyncImagePainter.State.Success }?.state as? AsyncImagePainter.State.Success)
                        ?.result?.drawable?.intrinsicWidth?.toString().orEmpty()
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        allianceForAirline(airline)?.let {
            Icon(
                modifier = Modifier.heightIn(max = 25.dp),
                painter = painterResource(id = it),
                contentDescription = "Airline alliance",
                tint = Color.Unspecified
            )
        }
    }
}

private object TrimTransform : Transformation {
    override val cacheKey: String = javaClass.name

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        var minX: Int = input.width
        var minY: Int = input.height
        var maxX = -1
        var maxY = -1
        for (y in 0 until input.height) {
            for (x in 0 until input.width) {
                val alpha = input.getPixel(x, y) shr 24 and 255
                // If the pixel is not transparent, update the bounds.
                if (alpha > 0) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }
        return Bitmap.createBitmap(
            /* source = */ input,
            /* x = */ minX,
            /* y = */ minY,
            /* width = */ maxX - minX + 1,
            /* height = */ maxY - minY + 1
        )
    }

    override fun equals(other: Any?) = other is TrimTransform

    override fun hashCode() = javaClass.hashCode()
}

@Composable
@Preview(showBackground = true)
private fun PreviewAirline(@PreviewParameter(ArlinePreviewProvider::class) airline: String) {
    BCBPScannerTheme {
        Row(
            Modifier
                .wrapContentSize()
                .padding(10.dp)
        ) {
            Airline(airline)
        }
    }
}

private class ArlinePreviewProvider : PreviewParameterProvider<String> {
    override val values: Sequence<String> = iconMap.keys.asSequence()
}
