package com.example.starfinder.services

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

class AstronomicCalculations {


    fun equatorialToHorizontal(
        raDeg: Double,
        decDeg: Double,
        latDeg: Double,
        lonDeg: Double,
        timeUtc: Calendar
    ): Pair<Double, Double> {
        // 1. Прецессия J2000.0 → дата
        val (raCorr, decCorr) = precessJ2000toDate(raDeg, decDeg, timeUtc)

        // 2. В радианы
        val raRad  = Math.toRadians(raCorr)
        val decRad = Math.toRadians(decCorr)
        val latRad = Math.toRadians(latDeg)

        // 3. LST = GMST + долгота
        val jd   = calculateJulianDate(timeUtc)
        val gmst = calculateGMST(jd)
        val lstRad = Math.toRadians((gmst + lonDeg) % 360.0)

        // 4. Часовой угол H = LST − RA
        var haRad = lstRad - raRad
        if (haRad < 0) haRad += 2*PI

        // 5. Истинная высота h₀
        val sinH0 = sin(latRad)*sin(decRad) +
                cos(latRad)*cos(decRad)*cos(haRad)
        val h0Rad  = asin(sinH0.coerceIn(-1.0,1.0))  // h₀ in rad

        // 6. Новая формула азимута:
        //    sin A = −cosδ·sinH / cosh
        //    cos A = (sinδ − sinφ·sinh) / (cosφ·cosh)
        val cosH0 = cos(h0Rad)
        val sinA = -cos(decRad)*sin(haRad) / cosH0
        val cosA = (sin(decRad) - sin(latRad)*sin(h0Rad)) /
                (cos(latRad)*cosH0)
        var aRad = atan2(sinA.coerceIn(-1.0,1.0), cosA.coerceIn(-1.0,1.0))
        if (aRad < 0) aRad += 2*PI

        // 7. Поправка на рефракцию (Bennett)
        val hDeg0 = Math.toDegrees(h0Rad)
        val refr  = if (hDeg0 in -1.0..90.0) refractCorrection(hDeg0) else 0.0
        val hDeg  = hDeg0 + refr/60.0  // convert minutes → degrees

        // 8. Возврат: (азимут, высота)
        return Pair(Math.toDegrees(aRad), hDeg)
    }

    private fun precessJ2000toDate(ra: Double, dec: Double, timeUtc: Calendar): Pair<Double,Double> {
        val jd = calculateJulianDate(timeUtc)
        val T  = (jd - 2451545.0) / 36525.0
        val zetaA  = (2306.2181*T + 0.30188*T*T + 0.017998*T*T*T)/3600.0
        val zA     = (2306.2181*T + 1.09468*T*T + 0.018203*T*T*T)/3600.0
        val thetaA = (2004.3109*T - 0.42665*T*T - 0.041833*T*T*T)/3600.0
        val zt = Math.toRadians(zetaA)
        val z  = Math.toRadians(zA)
        val th = Math.toRadians(thetaA)
        val alpha = Math.toRadians(ra)
        val beta = Math.toRadians(dec)
        val A = cos(beta)*sin(alpha + zt)
        val B = cos(th)*cos(beta)*cos(alpha + zt) - sin(th)*sin(beta)
        val C = sin(th)*cos(beta)*cos(alpha + zt) + cos(th)*sin(beta)
        val α1 = atan2(A, B) + z
        val δ1 = asin(C)
        return Pair(Math.toDegrees(α1), Math.toDegrees(δ1))
    }

    private fun refractCorrection(hDeg: Double): Double {
        val tanArg = Math.toRadians(hDeg + 7.31/(hDeg + 4.4))
        return 1.02 / tan(tanArg)
    }

    private fun calculateJulianDate(timeUtc: Calendar): Double {
        val utc = timeUtc.clone() as Calendar
        utc.timeZone = TimeZone.getTimeZone("UTC")
        var Y = utc.get(Calendar.YEAR)
        var M = utc.get(Calendar.MONTH) + 1
        val D  = utc.get(Calendar.DAY_OF_MONTH)
        val hr = utc.get(Calendar.HOUR_OF_DAY)
        val mn = utc.get(Calendar.MINUTE)
        val sc = utc.get(Calendar.SECOND)
        if (M <= 2) { Y -= 1; M += 12 }
        val A = floor(Y/100.0).toInt()
        val B = 2 - A + floor(A/4.0).toInt()
        val dayFr = (hr + mn/60.0 + sc/3600.0) / 24.0
        return floor(365.25*(Y+4716)) +
                floor(30.6001*(M+1)) +
                D + dayFr + B - 1524.5
    }

    private fun calculateGMST(jd: Double): Double {
        val T = (jd - 2451545.0)/36525.0
        var gmst = 280.46061837 +
                360.98564736629*(jd-2451545.0) +
                0.000387933*T*T -
                (T*T*T)/38710000.0
        return (gmst % 360.0 + 360.0) % 360.0
    }
}
