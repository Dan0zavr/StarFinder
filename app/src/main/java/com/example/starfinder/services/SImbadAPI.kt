package com.example.starfinder.services

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.ResponseBody
import retrofit2.Response

interface SimbadApi {
    @GET("simbad/sim-id")
    suspend fun getCelestialData(
        @Query("Ident") name: String,
        @Query("output.format") format: String = "VOTable"
    ): Response<ResponseBody>
}

object SimbadService {
    private const val BASE_URL = "http://simbad.u-strasbg.fr/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create()) // SIMBAD возвращает XML
        .build()

    val api: SimbadApi = retrofit.create(SimbadApi::class.java)
}
