package com.example.starfinder.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CelestialBody(
    val celestialBodyId: Int?,
    val celestialBodyName: String,
    val deflection: Double,
    val ascension: Double,
    val dataSourceId: Int
) : Parcelable