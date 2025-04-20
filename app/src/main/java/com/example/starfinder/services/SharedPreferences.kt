package com.example.starfinder.services

import android.content.Context

class SharedPreferences {
    object SessionManager {
        private const val PREF_NAME = "user_session"
        private const val KEY_EMAIL = "email"

        fun saveUserEmail(context: Context, email: String) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_EMAIL, email).apply()
        }

        fun getUserEmail(context: Context): String? {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_EMAIL, null)
        }

        fun clearSession(context: Context) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
        }
    }
}