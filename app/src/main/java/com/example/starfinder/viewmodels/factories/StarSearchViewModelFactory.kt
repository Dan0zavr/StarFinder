package com.example.starfinder.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.starfinder.services.DataService
import com.example.starfinder.viewmodels.StarSearchViewModel

class StarSearchViewModelFactory(
    private val dataService: DataService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StarSearchViewModel(dataService) as T
    }
}