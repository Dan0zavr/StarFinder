package com.example.starfinder.services.Api

import retrofit2.Response
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Field

interface SimbadApiService {
    @FormUrlEncoded
    @POST("simbad/sim-script")
    suspend fun searchStars(
        @Field("script") script: String
    ): Response<String>
}