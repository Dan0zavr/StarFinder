package com.example.starfinder.models

data class StarInfo(
    val name: String,
    val ra: Double,    // Прямое восхождение (в градусах)
    val dec: Double,   // Склонение (в градусах)
    val epoch: String = "J2000",
    val dataSourceId: Int
)