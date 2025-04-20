package com.example.starfinder.models

data class CelestialBody(
    val celestialBodyId: Int,
    val celestialBodyName: String,
    val typeId: Int,
    val deflection: Float,
    val ascension: Float,
    val dataSourceId: Int
)