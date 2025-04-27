package com.example.starfinder.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starfinder.models.CelestialBody
import com.example.starfinder.models.StarInfo
import com.example.starfinder.services.Api.ApiManager
import com.example.starfinder.services.DataService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StarSearchViewModel(service: DataService) : ViewModel() {

    private val _starResults = MutableLiveData<List<StarInfo>>()
    val starResults: LiveData<List<StarInfo>> = _starResults

    private val _starId = -1L
    var starId = _starId

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val dataService = service

    fun searchStar(name: String) {
        viewModelScope.launch {
            _loading.value = true

            // 1. Сначала проверяем локальную БД
            val stars = dataService.getWithQuery("SELECT * FROM CelestialBody WHERE CelestialBodyName = ?", arrayOf(name)){
                    cursor ->
                StarInfo(
                    name = cursor.getString(cursor.getColumnIndexOrThrow("CelestialBodyName")),
                    dec= cursor.getDouble(cursor.getColumnIndexOrThrow("Deflection")),
                    ra = cursor.getDouble(cursor.getColumnIndexOrThrow("Ascension")),
                    dataSourceId = cursor.getInt(cursor.getColumnIndexOrThrow("DataSourceId"))
                ).also {
                    starId = cursor.getLong(cursor.getColumnIndexOrThrow("CelestialBodyId"))
                }
            }

            // 2. Если нашли в БД - используем эти данные
            if (!stars.isEmpty()) {
                _starResults.value = stars.toList()
                _loading.value = false
                Log.d("DATABASE", "Данные из бд")
                return@launch
            }

            // 3. Если нет в БД - ищем в API
            searchStarByNameInSIMBAD(name)
        }
    }

    fun searchStarByNameInSIMBAD(name: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                _error.value = null

                val script = """
                    format object "%MAIN_ID|%COO(A)|%COO(D)"
                    query id $name
                """.trimIndent()

                val SIMBADresponse = ApiManager.getSimbadService().searchStars(script)

                if (SIMBADresponse.isSuccessful) {
                    val text = SIMBADresponse.body() ?: ""
                    val stars = parseStars(text, name)
                    _starResults.value = stars
                } else {
                    _error.value = "Ошибка API: ${SIMBADresponse.code()}"
                }

                Log.d("SIMBAD", "Ответ: ${SIMBADresponse.body()}")
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun parseStars(text: String, name: String): List<StarInfo> {
        return text.lineSequence()
            .filter { line ->
                line.isNotBlank() && "|" in line &&
                        !line.startsWith("::") &&
                        !line.contains("C.D.S.") &&
                        !line.contains("execution time")
            }
            .mapNotNull { line ->
                val parts = line.split("|").map { it.trim() }
                val raStr = parts.getOrNull(1) ?: return@mapNotNull null
                val decStr = parts.getOrNull(2) ?: return@mapNotNull null

                val raDeg = hmsToDegrees(raStr)
                val decDeg = dmsToDegrees(decStr)

                if (raDeg != null && decDeg != null) {
                    StarInfo(name = name, ra = raDeg, dec = decDeg, epoch = "J2000", dataSourceId = 0)
                } else null
            }
            .toList()
    }

    private fun hmsToDegrees(hms: String): Double? {
        return try {
            val parts = hms.split(" ")
            val hours = parts.getOrNull(0)?.toDoubleOrNull() ?: return null
            val minutes = parts.getOrNull(1)?.toFloatOrNull() ?: 0f
            val seconds = parts.getOrNull(2)?.toFloatOrNull() ?: 0f
            (hours + minutes / 60 + seconds / 3600) * 15f
        } catch (e: Exception) {
            null
        }
    }

    private fun dmsToDegrees(dms: String): Double? {
        return try {
            val parts = dms.split(" ")
            val sign = if (parts[0].startsWith("-")) -1 else 1
            val degrees = parts.getOrNull(0)?.toDoubleOrNull() ?: return null
            val minutes = parts.getOrNull(1)?.toFloatOrNull() ?: 0f
            val seconds = parts.getOrNull(2)?.toFloatOrNull() ?: 0f
            sign * (kotlin.math.abs(degrees) + minutes / 60 + seconds / 3600)
        } catch (e: Exception) {
            null
        }
    }
}


