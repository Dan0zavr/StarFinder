package com.example.starfinder.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CelestialBody(
    val celestialBodyId: Int,
    val celestialBodyName: String,
    val typeId: Int,
    val deflection: Float,
    val ascension: Float,
    val dataSourceId: Int
) : Parcelable