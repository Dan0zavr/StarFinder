package com.example.starfinder.models

data class ObservationObject(
    val observationObjectId: Int,
    val celestialBodyId: Int?,
    val constellationId: Int?
)