package com.example.starfinder.models

data class Observation(
    val observationId: Int?,
    val observationDateTime: String,
    val observationLongitude: Double?,
    val observationLatitude: Double,
    val userId: Int
)
