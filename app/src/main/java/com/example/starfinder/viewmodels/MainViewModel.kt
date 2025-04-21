package com.example.starfinder.viewmodels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.starfinder.services.CompassService
import com.example.starfinder.services.CoordinateService

class MainViewModel(
    private val coordinateService: CoordinateService,
    private val compassService: CompassService
) : ViewModel() {

    private val _location = MutableLiveData<Location?>()
    val location: LiveData<Location?> = _location

    private val _azimuth = MutableLiveData<Float>()
    val azimuth: LiveData<Float> = _azimuth

    private val _pitch = MutableLiveData<Float>()
    val pitch: LiveData<Float> = _pitch

    fun start() {
        getCurrentLocation()
        compassService.start()
        compassService.onAzimuthPitchChanged = { az, p ->
            _azimuth.postValue(az)
            _pitch.postValue(p)
        }
    }

    fun stop() {
        compassService.stop()
    }

    private fun getCurrentLocation() {
        coordinateService.getCurrentLocation {
            _location.postValue(it)
        }
    }
}
