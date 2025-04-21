package com.example.starfinder.models

data class StarDetails(
    val name: String,
    val ra: Float,
    val dec: Float,
    val spectralType: String = "",
    val parallax: Float? = null,
    val feH: Float? = null
)