package com.example.starfinder.services.Api

import com.example.starfinder.services.SimbadApi

class CelestialApiServiceManager(private val dbHelper: StarFinderDbHelper) {

    suspend fun getSimbadData(objectName: String): String {
        val link = dbHelper.getSourceLinkById(1)
        val simbadApi = SimbadApi(link)
        return simbadApi.retrofit.getObjectData(objectName)
    }

    suspend fun getNasaData(): String {
        val link = dbHelper.getSourceLinkById(2)
        val nasaApi = NasaExoplanetApi(link)
        return nasaApi.retrofit.getExoplanets()
    }
}