package ca.amandeep.bcbpscanner.ui.boardingpass

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.amandeep.bcbpscanner.R
import ca.amandeep.bcbpscanner.ui.theme.BCBPScannerTheme

/**
 * Displays the route of the flight.
 *  - The departure and arrival cities are displayed in the top row.
 *  - The departure and arrival IATA codes are displayed in the bottom row.
 *  - The IATA codes are separated by a plane icon.
 *  - The departure and arrival cities are truncated if they are too long.
 *
 * @param fromCity The IATA code of the departure city.
 * @param toCity The IATA code of the arrival city.
 */
@Composable
fun Route(
    fromCity: String,
    toCity: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1.0f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = AirportToNameMap.getCityForCode(fromCity),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Light),
            )
            Text(
                modifier = Modifier.weight(1.0f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = AirportToNameMap.getCityForCode(toCity),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Light),
                textAlign = TextAlign.End,
            )
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1.0f),
                text = fromCity,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
            )
            Spacer(Modifier.width(32.dp))
            Icon(
                modifier = Modifier.size(32.dp),
                painter = painterResource(id = R.drawable.ic_plane),
                tint = LocalContentColor.current,
                contentDescription = "",
            )
            Spacer(Modifier.width(30.dp))
            Text(
                modifier = Modifier.weight(1.0f),
                text = toCity,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                textAlign = TextAlign.End,
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RouteSample() {
    BCBPScannerTheme {
        Route("ORD", "JFK")
    }
}
