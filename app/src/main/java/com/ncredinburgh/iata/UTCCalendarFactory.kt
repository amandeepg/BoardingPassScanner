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
package com.ncredinburgh.iata

import java.util.Calendar
import kotlin.math.abs

/**
 * This class provides static factory methods for creating [Calendar] instances in UTC.
 */
object UTCCalendarFactory {
    @JvmStatic
    fun getInstanceForDayOfYear(dayOfYear: Int): Calendar {
        val now = Calendar.getInstance()

        val yearToUse = listOf(
            Calendar.getInstance(),
            Calendar.getInstance().apply { roll(Calendar.YEAR, -1) },
            Calendar.getInstance().apply { roll(Calendar.YEAR, 1) },
        )
            .map { it.apply { it[Calendar.DAY_OF_YEAR] = dayOfYear } }
            .minBy { abs(it.timeInMillis - now.timeInMillis) }

        yearToUse[Calendar.DAY_OF_YEAR] = dayOfYear
        return yearToUse
    }
}
