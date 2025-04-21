package com.example.starfinder.services

import java.util.Calendar
import kotlin.math.*

class AstronomicCalculations {

    fun equatorialToHorizontal(
        ra: Double, dec: Double,
        lat: Double, lon: Double,
        dateTime: Calendar
    ): Pair<Double, Double> {
        val jd = julianDate(dateTime)  // Уже должен быть Double
        val lst = localSiderealTime(jd, lon)

        val ha = lst - ra  // часовой угол
        val haRad = Math.toRadians(ha * 15.0)  // Явно умножаем на Double (15.0)
        val decRad = Math.toRadians(dec)
        val latRad = Math.toRadians(lat)

        val sinAlt = sin(decRad) * sin(latRad) + cos(decRad) * cos(latRad) * cos(haRad)
        val alt = Math.asin(sinAlt)

        val cosAz = (sin(decRad) - sin(alt) * sin(latRad)) / (cos(alt) * cos(latRad))
        val az = acos(cosAz)

        val azimuth = Math.toDegrees(if (sin(haRad) > 0) 2 * Math.PI - az else az)
        val altitude = Math.toDegrees(alt)

        return azimuth to altitude
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

        val jd = (365.25 * (y + 4716)).toInt() +
                (30.6001 * (m + 1)).toInt() +
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
