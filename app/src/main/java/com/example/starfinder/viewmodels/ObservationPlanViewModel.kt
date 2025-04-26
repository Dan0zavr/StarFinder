package com.example.starfinder.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import java.time.LocalTime

class ObservationPlanViewModel : ViewModel() {
    val celestialBody = MutableLiveData<String>()
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()
    val date = MutableLiveData<String>() // В формате "yyyy-MM-dd"
    val time = MutableLiveData<String>() // В формате "HH:mm"

    init {
        celestialBody.value = ""
        latitude.value = 0.0
        longitude.value = 0.0
        date.value = ""
        time.value = ""
    }

    fun setCoordinates(lat: Double, lon: Double) {
        latitude.value = lat
        longitude.value = lon
    }

    fun setDate(year: Int, month: Int, day: Int) {
        val monthFormatted = (month + 1).toString().padStart(2, '0')
        val dayFormatted = day.toString().padStart(2, '0')
        date.value = "$year-$monthFormatted-$dayFormatted"
    }

    fun setTime(hour: Int, minute: Int) {
        val hourFormatted = hour.toString().padStart(2, '0')
        val minuteFormatted = minute.toString().padStart(2, '0')
        time.value = "$hourFormatted:$minuteFormatted"
    }

    fun getFormattedCoordinates(): String {
        val lat = latitude.value
        val lon = longitude.value
        return if (lat != null && lon != null) {
            "Координаты: %.4f, %.4f".format(lat, lon)
        } else {
            "Координаты: не выбраны"
        }
    }

    fun getFormattedDate(): String {
        return if (!date.value.isNullOrBlank()) {
            "Дата: ${date.value}"
        } else {
            "Дата: не выбрана"
        }
    }

    fun getFormattedTime(): String {
        return if (!time.value.isNullOrBlank()) {
            "Время: ${time.value}"
        } else {
            "Время: не выбрано"
        }
    }
}