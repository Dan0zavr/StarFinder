package com.example.starfinder.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.starfinder.services.DataService
import com.example.starfinder.viewmodels.ObservationPlanViewModel

class ObservationPlanViewModelFactory(private val service: DataService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ObservationPlanViewModel::class.java)) {
            return ObservationPlanViewModel(service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}