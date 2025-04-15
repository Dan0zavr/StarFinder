package com.example.starfinder.viewmodels

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.starfinder.services.CoordinateService

class MainViewModel(private val coordinateService: CoordinateService) : ViewModel()
{
    private val _location = MutableLiveData<Location?>()
    val location: LiveData<Location?> = _location

    fun fetchCurrentLocation() {
        coordinateService.getCurrentLocation { loc ->
            _location.postValue(loc)
        }
    }
}