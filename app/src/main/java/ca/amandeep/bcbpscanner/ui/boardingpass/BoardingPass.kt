@file:Suppress("RemoveEmptyParenthesesFromAnnotationEntry")

package ca.amandeep.bcbpscanner.ui.boardingpass

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ca.amandeep.bcbpscanner.R
import ca.amandeep.bcbpscanner.ui.theme.BCBPScannerTheme
import ca.amandeep.bcbpscanner.ui.theme.Card3
import com.ncredinburgh.iata.model.FlightSegment
import com.ncredinburgh.iata.model.IataCode
import com.ncredinburgh.iata.specs.Element
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
private fun PreCheck(status: String?) {
    when (status?.trim()) {
        "1" -> Text(
            text = "SSSS",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        )

        "3" -> Column {
            Icon(
                modifier = Modifier.size(width = 70.dp, height = 20.dp),
                painter = painterResource(id = R.drawable.pre),
                tint = Color.Unspecified,
                contentDescription = stringResource(R.string.tsa_precheck),
            )
            Spacer(modifier = Modifier.height(5.dp))
        }
    }
}

@Composable
private fun SingleDataRow(
    label: String,
    data: String?,
    spacing: Dp = 10.dp,
) {
    if (!data.isNullOrBlank()) {
        Column {
            Spacer(Modifier.height(spacing))
            LabelAndData(label, data)
        }
    }
}

@Composable
private fun MultipleDataRow(
    spacing: Dp = 10.dp,
    body: @Composable MultipleDataRowScope.() -> Unit,
) {
    Column {
        Spacer(Modifier.height(spacing))
        Row { MultipleDataRowScope().body() }
    }
}

private class MultipleDataRowScope {
    @Composable
    fun SingleDataRow(
        label: String,
        data: String?,
        modifier: Modifier = Modifier,
        first: Boolean = false,
        spacing: Dp = 20.dp,
    ) {
        if (!data.isNullOrBlank()) {
            if (!first) Spacer(Modifier.width(spacing))
            LabelAndData(label, data, modifier = modifier)
        }
    }
}

@Composable
private fun BoardingPassCard(
    topBar: @Composable () -> Unit,
    body: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card3(
        modifier = modifier,
        elevation = 5.dp,
    ) {
        Column {
            Surface(color = Color(0xB3FFFFFF)) {
                Box(
                    Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                ) {
                    topBar()
                }
            }
            Box(
                Modifier.padding(
                    top = 10.dp,
                    bottom = 20.dp,
                    start = 20.dp,
                    end = 20.dp,
                ),
            ) {
                body()
            }
        }
    }
}

@Composable
fun BoardingPass(
    iataCode: IataCode,
    modifier: Modifier = Modifier,
) {
    val passenger = iataCode.passengerName.trim()
    val flight = iataCode.firstFlightSegment
    val airline = (
        flight.marketingCarrierDesignator
            .let { if (it.isNullOrBlank()) null else it }
            ?: flight.operatingCarrierDesignator
    )?.trim()?.uppercase(Locale.US)
    val selectee = flight.selecteeIndicator
    val seat = flight.seatNumber?.trimStart('0')?.trim()
    val seq = flight.checkInSequenceNumber.trimStart('0').trim()
    val flightNum = flight.flightNumber.trimStart('0').trim()
    val date = SimpleDateFormat("E MMM dd, yyyy", Locale.US)
        .format(flight.dateOfFlight?.time ?: return)
    val fromCity = flight.fromCity?.trim()?.uppercase(Locale.US) ?: return
    val toCity = flight.toCity?.trim()?.uppercase(Locale.US) ?: return
    val frequentFlyer = buildString {
        append(
            flight.frequentFlyerDesignator.takeIf { it?.isNotBlank() == true }?.trim()
                ?.uppercase(Locale.US)?.plus(" ").orEmpty(),
        )
        append(flight.frequentFlyerNumber?.trim().orEmpty())
    }
    val locator = flight.pNR?.trim()?.uppercase(Locale.US)

    BoardingPassCard(
        modifier = modifier,
        topBar = { Airline(airline) },
        body = {
            Column {
                Route(fromCity, toCity)

                SingleDataRow("PASSENGER", passenger)
                PreCheck(selectee)

                MultipleDataRow {
                    SingleDataRow("FLIGHT", flightNum, first = true)
                    SingleDataRow("SEAT", seat)
                    SingleDataRow("SEQUENCE", seq)
                }

                SingleDataRow("DATE", date)
                SingleDataRow("FREQUENT FLYER", frequentFlyer)
                SingleDataRow("LOCATOR", locator)
            }
        },
    )
}

@Composable
private fun LabelAndData(
    label: String,
    data: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = LocalContentColor.current.copy(alpha = 0.6f),
        )
        Text(
            text = data,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun Sample(
    @PreviewParameter(IataCodePreviewProvider::class) iataCode: IataCode,
) {
    BCBPScannerTheme { Surface { BoardingPass(iataCode) } }
}

private class IataCodePreviewProvider : PreviewParameterProvider<IataCode> {
    override val values = sequenceOf(
        IataCode.Builder()
            .element(Element.PASSENGER_NAME, "WILLIAM H GATES")
            .flightSegment(
                FlightSegment.Builder()
                    .element(Element.FROM_CITY_AIRPORT_CODE, "JFK")
                    .element(Element.TO_CITY_AIRPORT_CODE, "DFW")
                    .element(Element.FLIGHT_NUMBER, "01608")
                    .element(Element.SEAT_NUMBER, "001A")
                    .element(Element.MARKETING_CARRIER_DESIGNATOR, "DL")
                    .element(Element.SELECTEE_INDICATOR, "3")
                    .element(Element.DATE_OF_FLIGHT, "311")
                    .element(Element.FREQUENT_FLYER_AIRLINE_DESIGNATOR, "AA")
                    .element(Element.FREQUENT_FLYER_NUMBER, "VBIPY782H")
                    .element(Element.CHECK_IN_SEQUENCE_NUMBER, "006")
                    .build(),
            ),
        IataCode.Builder()
            .element(Element.PASSENGER_NAME, "HILARY A PORTER")
            .flightSegment(
                FlightSegment.Builder()
                    .element(Element.FROM_CITY_AIRPORT_CODE, "YYZ")
                    .element(Element.TO_CITY_AIRPORT_CODE, "SFo")
                    .element(Element.FLIGHT_NUMBER, "08")
                    .element(Element.SEAT_NUMBER, "016F")
                    .element(Element.MARKETING_CARRIER_DESIGNATOR, "AC")
                    .element(Element.SELECTEE_INDICATOR, "1")
                    .element(Element.DATE_OF_FLIGHT, "125")
                    .element(Element.CHECK_IN_SEQUENCE_NUMBER, "073")
                    .build(),
            ),
        IataCode.Builder()
            .element(Element.PASSENGER_NAME, "HILARY A PORTER")
            .flightSegment(
                FlightSegment.Builder()
                    .element(Element.FROM_CITY_AIRPORT_CODE, "DEL")
                    .element(Element.TO_CITY_AIRPORT_CODE, "MUC")
                    .element(Element.FLIGHT_NUMBER, "086")
                    .element(Element.SEAT_NUMBER, "016F")
                    .element(Element.MARKETING_CARRIER_DESIGNATOR, "BA")
                    .element(Element.SELECTEE_INDICATOR, "")
                    .element(Element.DATE_OF_FLIGHT, "125")
                    .element(Element.CHECK_IN_SEQUENCE_NUMBER, "633")
                    .build(),
            ),
    ).map { it.build() }
}
