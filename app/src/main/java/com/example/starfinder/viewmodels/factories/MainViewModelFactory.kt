package com.example.starfinder.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.starfinder.services.CoordinateService
import com.example.starfinder.viewmodels.MainViewModel

class MainViewModelFactory(private val coordinateService: CoordinateService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(coordinateService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}