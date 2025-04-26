package com.example.starfinder

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.example.starfinder.models.User

object UserSession {
    private const val PREFS_NAME = "UserPrefs"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_USER_ID = "userId"
    private const val KEY_USER_NAME = "userName"

    fun saveUser(context: Context, user: User) {
        context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putInt(KEY_USER_ID, user.userId)
            putString(KEY_USER_NAME, user.userName)
            apply()
        }
    }

    fun getCurrentUserId(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getInt(KEY_USER_ID, 0)
    }

    fun isLoggedIn(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().apply {
            clear()
            apply()
        }
    }
}