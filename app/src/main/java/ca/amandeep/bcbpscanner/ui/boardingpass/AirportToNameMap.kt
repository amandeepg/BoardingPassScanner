package ca.amandeep.bcbpscanner.ui.boardingpass

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale

object AirportToNameMap {
    fun getCityForCode(code: String): String =
        cityNamesByAirportCode[code.uppercase(Locale.US)].orEmpty()

    private val moshi = Moshi.Builder().build()
    private val airportListType =
        Types.newParameterizedType(List::class.java, JsonAirport::class.java)
    private val airportListAdapter: JsonAdapter<List<JsonAirport>> = moshi.adapter(airportListType)
    private val cityListType = Types.newParameterizedType(List::class.java, JsonCity::class.java)
    private val cityListAdapter: JsonAdapter<List<JsonCity>> = moshi.adapter(cityListType)

    private val cityNamesByAirportCode = mutableMapOf<String, String>()

    fun init(context: Context) {
        GlobalScope.launch(Dispatchers.IO) {
            val airportsJson =
                context.assets.open("airports.json").bufferedReader().use { it.readText() }
            val citiesJson =
                context.assets.open("cities.json").bufferedReader().use { it.readText() }

            val cityNamesByCityCode = mutableMapOf<String, String>()

            cityListAdapter.fromJson(citiesJson)?.forEach { city ->
                cityNamesByCityCode[city.code] = city.name
            }
            airportListAdapter.fromJson(airportsJson)?.forEach { airport ->
                cityNamesByAirportCode[airport.code.uppercase(Locale.US)] =
                    cityNamesByCityCode[airport.city_code].orEmpty()
            }
        }
    }

    @JsonClass(generateAdapter = true)
    data class JsonAirport(
        val code: String,
        val city_code: String
    )

    @JsonClass(generateAdapter = true)
    data class JsonCity(
        val code: String,
        val name: String
    )
}
