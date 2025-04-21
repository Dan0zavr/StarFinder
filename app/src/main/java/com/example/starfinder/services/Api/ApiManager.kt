package com.example.starfinder.services.Api

import android.content.Context
import android.util.Log
import com.example.starfinder.models.CelestialBody
import com.example.starfinder.services.DataService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.HttpURLConnection
import java.net.URL


object ApiManager {
    private lateinit var simbadService: SimbadApiService

    fun init(context: Context) {
        val db = DataService(context)
        val simbadLink = db.getSourceLinkById(1)?.let {
            if (!it.endsWith("/")) "$it/" else it
        } ?: throw Exception("SIMBAD source not found")

        val retrofit = Retrofit.Builder()
            .baseUrl(simbadLink)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        simbadService = retrofit.create(SimbadApiService::class.java)
    }

    fun getSimbadService(): SimbadApiService = simbadService
}
