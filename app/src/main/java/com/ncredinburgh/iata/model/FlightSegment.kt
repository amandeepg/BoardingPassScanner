/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ncredinburgh.iata.model

import com.ncredinburgh.iata.UTCCalendarFactory
import com.ncredinburgh.iata.specs.Compartment
import com.ncredinburgh.iata.specs.Element
import com.ncredinburgh.iata.specs.InternationalDocumentVerification
import com.ncredinburgh.iata.specs.Occurrence
import java.util.Calendar
import java.util.Objects

data class FlightSegment private constructor(val elements: Map<Element, CharSequence>) {
    val pNR: String?
        get() = getValue(Element.OPERATING_CARRIER_PNR_CODE)?.trim { it <= ' ' }

    val fromCity: String?
        get() = getValue(Element.FROM_CITY_AIRPORT_CODE)

    val toCity: String?
        get() = getValue(Element.TO_CITY_AIRPORT_CODE)

    val operatingCarrierDesignator: String?
        get() = getValue(Element.OPERATING_CARRIER_DESIGNATOR)?.trim { it <= ' ' }

    val flightNumber: String
        get() = getValue(Element.FLIGHT_NUMBER)!!.trim { it <= ' ' }

    // Ignore...
    val julianDateOfFlight: Int?
        get() {
            var dateOfFlight: Int? = null
            try {
                dateOfFlight = getValue(Element.DATE_OF_FLIGHT).toString().toInt()
            } catch (nfe: NumberFormatException) { // Ignore...
            }
            return dateOfFlight
        }

    val dateOfFlight: Calendar?
        get() {
            val dateOfFlight = julianDateOfFlight
            return if (dateOfFlight != null) UTCCalendarFactory.getInstanceForDayOfYear(dateOfFlight) else null
        }

    val compartmentCode: Compartment
        get() = Compartment.parse(getValue(Element.COMPARTMENT_CODE))

    val seatNumber: String?
        get() = getValue(Element.SEAT_NUMBER)

    val checkInSequenceNumber: String
        get() = getValue(Element.CHECK_IN_SEQUENCE_NUMBER)!!.trim { it <= ' ' }

    val passengerStatus: String?
        get() = getValue(Element.PASSENGER_STATUS)

    // Ignore...
    val airlineNumericCode: Int?
        get() {
            var airlineNumericCode: Int? = null
            try {
                airlineNumericCode = getValue(Element.AIRLINE_NUMERIC_CODE).toString().toInt()
            } catch (nfe: NumberFormatException) { // Ignore...
            }
            return airlineNumericCode
        }

    val serialNumber: String?
        get() = getValue(Element.SEAT_NUMBER)

    val selecteeIndicator: String?
        get() = getValue(Element.SELECTEE_INDICATOR)

    val internationalDocumentVerification: InternationalDocumentVerification
        get() = InternationalDocumentVerification.parse(getValue(Element.INTERNATIONAL_DOCUMENT_VERIFICATION))

    val marketingCarrierDesignator: String?
        get() = getValue(Element.MARKETING_CARRIER_DESIGNATOR)?.trim { it <= ' ' }

    val frequentFlyerDesignator: String?
        get() = getValue(Element.FREQUENT_FLYER_AIRLINE_DESIGNATOR)?.trim { it <= ' ' }

    val frequentFlyerNumber: String?
        get() = getValue(Element.FREQUENT_FLYER_NUMBER)

    val idAdIndicator: String?
        get() = getValue(Element.ID_AD_INDICATOR)

    val freeBaggageAllowance: String?
        get() = getValue(Element.FREE_BAGGAGE_ALLOWANCE)

    val individualAirlineUse: String?
        get() = getValue(Element.FOR_AIRLINE_USE)

    private fun getValue(e: Element): String? = elements[e]?.toString()

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is FlightSegment -> false
        else -> elements == other.elements
    }

    override fun hashCode(): Int = Objects.hash(elements)

    class Builder {
        private val elements: MutableMap<Element, CharSequence> = mutableMapOf()

        fun element(e: Element, s: CharSequence): Builder {
            assertRepeatedOccurrence(e)
            elements[e] = s
            return this
        }

        fun build(): FlightSegment = FlightSegment(elements)

        companion object {
            private fun assertRepeatedOccurrence(e: Element) {
                check(e.occurrence == Occurrence.R) {
                    String.format(
                        "Element (%s) does not have REPEATED occurrence.",
                        e.name
                    )
                }
            }
        }
    }
}
