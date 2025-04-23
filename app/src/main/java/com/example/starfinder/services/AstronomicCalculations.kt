package com.example.starfinder.services

import java.util.Calendar
import kotlin.math.*

class AstronomicCalculations {

    fun equatorialToHorizontal(
        ra: Double,      // Прямое восхождение в градусах
        dec: Double,     // Склонение в градусах
        lat: Double,     // Широта наблюдателя в градусах
        lon: Double,     // Долгота наблюдателя в градусах
        dateTime: Calendar
    ): Pair<Double, Double> {
        // 1. Преобразование даты и времени
        val jd = julianDate(dateTime)

        // 2. Расчет местного звездного времени (LST)
        val lstDegrees = localSiderealTime(jd, lon)

        // 3. Вычисление часового угла (HA)
        val haDegrees = normalizeAngle(lstDegrees - ra)
        val haRadians = Math.toRadians(haDegrees)

        // 4. Преобразование углов в радианы
        val decRad = Math.toRadians(dec)
        val latRad = Math.toRadians(lat)

        // 5. Расчет высоты (altitude)
        val sinAlt = sin(decRad) * sin(latRad) + cos(decRad) * cos(latRad) * cos(haRadians)
        val altitude = Math.toDegrees(Math.asin(sinAlt.coerceIn(-1.0, 1.0)))

        // 6. Расчет азимута (azimuth)
        val cosAz = (sin(decRad) - sinAlt * sin(latRad)) / (cos(Math.asin(sinAlt)) * cos(latRad))
        val azRad = when {
            cosAz < -1 -> Math.PI        // Обработка граничных значений
            cosAz > 1 -> 0.0
            else -> Math.acos(cosAz.coerceIn(-1.0, 1.0))
        }
        val azimuth = Math.toDegrees(if (sin(haRadians) > 0) 2 * Math.PI - azRad else azRad)

        return normalizeAzimuth(azimuth) to altitude
    }

    private fun normalizeAngle(degrees: Double): Double {
        return (degrees % 360.0).let { if (it < 0) it + 360.0 else it }
    }

    private fun normalizeAzimuth(azimuth: Double): Double {
        return (azimuth % 360.0).let { if (it < 0) it + 360.0 else it }
    }


    fun julianDate(date: Calendar): Double {
        val year = date.get(Calendar.YEAR)
        var month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DAY_OF_MONTH)
        val hour = date.get(Calendar.HOUR_OF_DAY)
        val minute = date.get(Calendar.MINUTE)
        val second = date.get(Calendar.SECOND)

        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }

        val a = y / 100
        val b = 2 - a + (a / 4)
        val dayFraction = (hour + minute / 60.0 + second / 3600.0) / 24.0

        val jd = (365.25 * (y + 4716)) +
                (30.6001 * (m + 1)) +
                day + dayFraction + b - 1524.5

        return jd
    }

    fun localSiderealTime(jd: Double, longitude: Double): Double {
        val d = jd - 2451545.0

        var gst = 280.46061837 + 360.98564736629 * d
        gst %= 360.0
        if (gst < 0) gst += 360.0

        val lst = (gst + longitude) % 360.0
        return if (lst < 0) lst + 360.0 else lst
    }

    fun calculateAngleDifference(currentAzimuth: Double, currentAltitude: Double, starAzimuth: Double, starAltitude: Double): Pair<Double, Double> {
        // Разница в азимуте
        var deltaAzimuth = starAzimuth - currentAzimuth
        if (deltaAzimuth < 0) deltaAzimuth += 360.0  // Обрабатываем отрицательные значения
        if (deltaAzimuth > 180.0) deltaAzimuth -= 360.0  // Разница не должна превышать 180 градусов

        // Разница в высоте
        val deltaAltitude = starAltitude - currentAltitude

        return deltaAzimuth to deltaAltitude
    }
}
