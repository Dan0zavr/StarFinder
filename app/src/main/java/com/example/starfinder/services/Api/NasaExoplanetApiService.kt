package com.example.starfinder.services.Api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface NasaExoplanetApiService {
    @GET("TAP/sync?query=select+top+10+pl_name+from+pscomppars&format=json")
    fun getExoplanets(): Call<ResponseBody>
}