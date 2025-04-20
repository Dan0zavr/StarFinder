package com.example.starfinder.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.starfinder.models.User
import com.example.starfinder.services.DataService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel(private val dbHelper: DataService) : ViewModel() {

    private val _loginResult = MutableLiveData<User?>()
    val loginResult: LiveData<User?> get() = _loginResult

    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val user = dbHelper.getUserByEmailAndPassword(email, password)
            _loginResult.postValue(user)
        }
    }

    fun register(userName: String, email: String, password: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val existingUser = dbHelper.getUserByEmail(email)
            if (existingUser == null) {
                dbHelper.insertUser(User(0, userName, email, password))
                callback(true)
            } else {
                callback(false)
            }
        }
    }
}