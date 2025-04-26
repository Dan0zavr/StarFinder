package com.example.starfinder.models

data class StarInfo(
    val name: String,
    val ra: Float,    // Прямое восхождение (в градусах)
    val dec: Float,   // Склонение (в градусах)
    val epoch: String = "J2000",
    val dataSourceId: Int
)