package com.example.starfinder.services.Api

import com.example.starfinder.models.CelestialBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Field
import retrofit2.http.Headers
import retrofit2.http.Query

interface SimbadApiService {
    @FormUrlEncoded
    @POST("simbad/sim-script")
    suspend fun searchStars(
        @Field("script") script: String
    ): Response<String>
}