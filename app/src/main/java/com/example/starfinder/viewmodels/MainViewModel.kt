package com.example.starfinder.viewmodels

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starfinder.models.CelestialBody
import com.example.starfinder.services.Api.ApiManager
import com.example.starfinder.services.CompassService
import com.example.starfinder.services.CoordinateService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val _selectedStar = MutableLiveData<CelestialBody?>()
    val selectedStar: LiveData<CelestialBody?> = _selectedStar

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

    fun selectStar(star: CelestialBody) {
        _selectedStar.postValue(star)
    }

    private fun getCurrentLocation() {
        coordinateService.getCurrentLocation {
            _location.postValue(it)
        }
    }
}


