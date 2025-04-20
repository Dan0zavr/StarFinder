package com.example.starfinder.models

data class Observation(
    val observationId: Int,
    val observationDateTime: String,
    val observationLongitude: Float?,
    val observationLatitude: Float?,
    val userId: Int
)
