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

import com.ncredinburgh.iata.specs.CheckinSource
import com.ncredinburgh.iata.specs.DocumentType
import com.ncredinburgh.iata.specs.Element
import com.ncredinburgh.iata.specs.FormatCode
import com.ncredinburgh.iata.specs.Occurrence
import com.ncredinburgh.iata.specs.PassIssuanceSource
import com.ncredinburgh.iata.specs.PassengerDescription
import java.util.Objects
import java.util.regex.Pattern

data class IataCode private constructor(
    val elements: Map<Element, CharSequence>,
    val flightSegments: List<FlightSegment>,
) {
    val formatCode: FormatCode
        get() = FormatCode.parse(getValue(Element.FORMAT_CODE))

    val passengerName: String
        get() = getValue(Element.PASSENGER_NAME)!!.trim { it <= ' ' }

    val passengerFirstName: String?
        get() = getPassengerNamePart(
            passengerName,
            FIRST_NAME_GROUP,
        )

    val passengerLastName: String?
        get() = getPassengerNamePart(
            passengerName,
            LAST_NAME_GROUP,
        )

    val versionNumber: String?
        get() = getValue(Element.VERSION_NUMBER)

    val passengerDescription: PassengerDescription
        get() = PassengerDescription.parse(getValue(Element.PASSENGER_DESCRIPTION))

    val sourceOfCheckIn: CheckinSource
        get() = CheckinSource.parse(getValue(Element.SOURCE_OF_CHECK_IN))

    val sourceOfPassIssuance: PassIssuanceSource
        get() = PassIssuanceSource.parse(getValue(Element.SOURCE_OF_BOARDING_PASS_ISSUANCE))

    val dateOfPassIssuance: String?
        get() = getValue(Element.DATE_OF_PASS_ISSUANCE)

    val documentType: DocumentType
        get() = DocumentType.parse(getValue(Element.DOCUMENT_TYPE))

    val airlineDesignatorOfPassIssuer: String?
        get() = getValue(Element.AIRLINE_DESIGNATOR_OF_ISSUER)

    val baggageTagLicensePlate: String?
        get() = getValue(Element.BAGGAGE_TAG_LICENSE_PLATE)

    val firstFlightSegment: FlightSegment
        get() = flightSegments[0]

    val securityData: SecurityData
        get() = SecurityData(
            getValue(Element.TYPE_OF_SECURITY_DATA),
            getValue(Element.SECURITY_DATA),
        )

    private fun getValue(e: Element): String? = elements[e]?.toString()

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is IataCode -> false
        else ->
            elements == other.elements &&
                flightSegments == other.flightSegments
    }

    override fun hashCode(): Int = Objects.hash(elements, flightSegments)

    class Builder {
        private val flightSegments: MutableList<FlightSegment> = mutableListOf()
        private val elements: MutableMap<Element, CharSequence> = mutableMapOf()

        fun element(e: Element, s: CharSequence): Builder {
            assertUniqueOccurrence(e)
            elements[e] = s
            return this
        }

        fun flightSegment(segment: FlightSegment): Builder {
            flightSegments.add(segment)
            return this
        }

        fun build(): IataCode = IataCode(elements, flightSegments)

        companion object {
            private const val MAX_NO_OF_SEGMENTS = 4

            private fun assertUniqueOccurrence(e: Element) {
                check(e.occurrence == Occurrence.U) {
                    String.format(
                        "Element (%s) does not have UNIQUE occurrence.",
                        e.name,
                    )
                }
            }
        }
    }

    companion object {
        private val NAME_PATTERN = Pattern.compile("([^/]+)/?(.*)")
        private const val FIRST_NAME_GROUP = 2
        private const val LAST_NAME_GROUP = 1

        private fun getPassengerNamePart(name: String, group: Int): String? {
            val matcher = NAME_PATTERN.matcher(name)
            return if (matcher.matches()) matcher.group(group) else null
        }
    }
}
