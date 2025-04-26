package com.example.starfinder.viewmodels.factories


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.starfinder.services.DataService
import com.example.starfinder.viewmodels.AuthViewModel

class AuthViewModelFactory(private val dataService: DataService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(dataService) as T
    }
}
