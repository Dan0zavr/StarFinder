package com.example.starfinder.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starfinder.models.User
import com.example.starfinder.services.DataService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(private val dataService: DataService) : ViewModel() {
    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                dataService.getUserByEmailAndPassword(email, password)
            }
            _loginResult.postValue(user)
        }
    }

    fun register(username: String, email: String, password: String, callback: (Long) -> Unit) {
        viewModelScope.launch {
            val success = withContext(Dispatchers.IO) {
                if (dataService.isEmailTaken(email)) {
                    -1L
                } else {
                    val user = User(
                        userId = 0,
                        userName = username,
                        email = email,
                        password = password
                    )
                    dataService.insert("User", user)
                }
            }
            callback(success)
        }
    }
}

