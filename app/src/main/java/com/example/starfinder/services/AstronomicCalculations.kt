package com.example.starfinder.services

import android.util.Log
import android.util.Log.d
import androidx.camera.core.Logger.e
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

class AstronomicCalculations {

    fun equatorialToHorizontal(
        raHours: Double,
        decDeg: Double,
        latDeg: Double,
        lonDeg: Double,
        time: Calendar
    ): Pair<Double, Double> {
        // 1. Переводим всё в радианы
        val raRad = Math.toRadians(raHours * 15.0) // 1 час = 15 градусов
        val decRad = Math.toRadians(decDeg)
        val latRad = Math.toRadians(latDeg)

        // 2. Вычисляем звёздное время
        val lst = calculateLocalSiderealTime(lonDeg, time)
        val haRad = lst - raRad // Часовой угол

        // 3. Вычисляем высоту (altitude)
        val sinAlt = sin(decRad) * sin(latRad) + cos(decRad) * cos(latRad) * cos(haRad)
        val altRad = asin(sinAlt)

        // 4. Вычисляем азимут (azimuth)
        val cosAz = (sin(decRad) - sin(altRad) * sin(latRad)) / (cos(altRad) * cos(latRad))
        val azRad = acos(cosAz.coerceIn(-1.0, 1.0))

        // 5. Корректируем азимут по квадранту
        val finalAz = if (sin(haRad) > 0) 2 * PI - azRad else azRad

        return Pair(
            Math.toDegrees(finalAz),  // Азимут (0°=север, 90°=восток)
            Math.toDegrees(altRad)    // Высота над горизонтом
        )

    }

    private fun calculateLocalSiderealTime(longitudeDeg: Double, time: Calendar): Double {
        // 1. Переводим календарь в UTC
        time.timeZone = TimeZone.getTimeZone("UTC")
        val utcCalendar = time.clone() as Calendar

        // 2. Извлекаем компоненты даты (в UTC)
        val year = utcCalendar.get(Calendar.YEAR)
        val month = utcCalendar.get(Calendar.MONTH) + 1 // Январь = 1
        val day = utcCalendar.get(Calendar.DAY_OF_MONTH)
        val hour = utcCalendar.get(Calendar.HOUR_OF_DAY)
        val minute = utcCalendar.get(Calendar.MINUTE)
        val second = utcCalendar.get(Calendar.SECOND)

        // 3. Вычисляем юлианскую дату (JD)
        val jd = calculateJulianDate(year, month, day, hour, minute, second)

        // 4. Вычисляем гринвичское звёздное время (GMST)
        val gmstRad = calculateGMST(jd)

        // 5. Переводим в местное звёздное время (LST)
        val lstRad = gmstRad + Math.toRadians(longitudeDeg)

        // 6. Нормализуем в диапазон [0, 2π)
        return lstRad % (2 * Math.PI).let {
            if (it < 0) it + 2 * Math.PI else it
        }
    }

    private fun calculateJulianDate(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int
    ): Double {
        val a = floor((14 - month) / 12.0)
        val y = year + 4800 - a
        val m = month + 12 * a - 3

        // Вычисляем JD для даты
        var jd = day + floor((153 * m + 2) / 5.0) +
                365 * y + floor(y / 4.0) -
                floor(y / 100.0) + floor(y / 400.0) -
                32045

        // Добавляем время суток
        val fractionOfDay = (hour + minute / 60.0 + second / 3600.0) / 24.0
        return jd + fractionOfDay
    }

    private fun calculateGMST(julianDate: Double): Double {
        // 1. Вычисляем время в юлианских столетиях от эпохи J2000.0
        val t = (julianDate - 2451545.0) / 36525.0

        // 2. Формула из IAU 2000B model
        var gmst = 280.46061837 +
                360.98564736629 * (julianDate - 2451545.0) +
                0.000387933 * t * t -
                t * t * t / 38710000.0

        // 3. Нормализуем в диапазон [0, 360)
        gmst %= 360.0
        if (gmst < 0) gmst += 360.0

        return Math.toRadians(gmst)
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

