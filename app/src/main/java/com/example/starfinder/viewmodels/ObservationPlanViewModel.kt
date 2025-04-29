package com.example.starfinder.viewmodels

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.starfinder.R
import com.example.starfinder.models.CelestialBody
import com.example.starfinder.models.StarInfo
import com.example.starfinder.services.DataService
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class ObservationPlanViewModel(private val dataService: DataService) : ViewModel() {

    // LiveData свойства
    private val _selectedStar = MutableLiveData<CelestialBody?>()
    val selectedStar: LiveData<CelestialBody?> = _selectedStar

    private val _latitude = MutableLiveData<Double>().apply { value = 0.0 }
    val latitude: LiveData<Double> = _latitude

    private val _longitude = MutableLiveData<Double>().apply { value = 0.0 }
    val longitude: LiveData<Double> = _longitude

    private val _date = MutableLiveData<String>()
    val date: LiveData<String> = _date

    private val _time = MutableLiveData<String>()
    val time: LiveData<String> = _time

    private val _starSuggestions = MutableLiveData<List<StarInfo>>().apply { value = emptyList() }
    val starSuggestions: LiveData<List<StarInfo>> = _starSuggestions

    private val _loading = MutableLiveData<Boolean>().apply { value = false }
    val loading: LiveData<Boolean> = _loading

    // Публичные методы для доступа к данным
    fun getSelectedStar(): CelestialBody? = _selectedStar.value
    fun getCurrentLatitude(): Double = _latitude.value ?: 0.0
    fun getCurrentLongitude(): Double = _longitude.value ?: 0.0
    fun getCurrentDate(): String? = _date.value
    fun getCurrentTime(): String? = _time.value

    // Методы для установки значений
    fun setCelestialBody(star: CelestialBody) {
        _selectedStar.value = star
    }

    fun setLatitude(lat: Double) {
        _latitude.value = lat
    }

    fun setLongitude(lon: Double) {
        _longitude.value = lon
    }

    fun setDate(year: Int, month: Int, day: Int) {
        _date.value = "%04d-%02d-%02d".format(year, month + 1, day)
    }

    fun setTime(hour: Int, minute: Int) {
        _time.value = "%02d:%02d".format(hour, minute)

    fun searchStars(query: String) {
        if (query.length < 2) {
            _starSuggestions.value = emptyList()
            return
        }

        viewModelScope.launch {
            _loading.value = true
            try {
                val stars = dataService.getWithQuery(
                    "SELECT * FROM CelestialBody WHERE CelestialBodyName LIKE ?",
                    arrayOf("%$query%")
                ) { cursor ->
                    StarInfo(
                        name = cursor.getString(cursor.getColumnIndexOrThrow("CelestialBodyName")),
                        dec = cursor.getDouble(cursor.getColumnIndexOrThrow("Deflection")),
                        ra = cursor.getDouble(cursor.getColumnIndexOrThrow("Ascension")),
                        dataSourceId = cursor.getInt(cursor.getColumnIndexOrThrow("DataSourceId"))
                    )
                }
                _starSuggestions.value = stars.take(2)
            } catch (e: Exception) {
                _starSuggestions.value = emptyList()
                Log.e("ObservationPlanVM", "Error searching stars", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearSelection() {
        _selectedStar.value = null
    }
}
}