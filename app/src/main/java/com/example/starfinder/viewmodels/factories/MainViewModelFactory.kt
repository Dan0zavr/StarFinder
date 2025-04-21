package com.example.starfinder.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.starfinder.services.CompassService
import com.example.starfinder.services.CoordinateService
import com.example.starfinder.viewmodels.MainViewModel

class MainViewModelFactory(
    private val coordinateService: CoordinateService,
    private val compassService: CompassService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(coordinateService, compassService) as T
    }
}