package com.example.starfinder.viewmodels

import java.util.*

class AstronomicCalculations {

    val epoch : Calendar = GregorianCalendar(2000, 0, 1)

    fun getGMST(calendar: Calendar): Double {
        // 1. Вычисляем юлианскую дату (JD) для переданного времени
        val JD = JulianDate(calendar)  // Ваша функция JulianDate()

        // 2. Вычисляем дни от J2000 (2451545.0) и столетия (T)
        val d = JD - 2451545.0
        val T = d / 36525.0  // Юлианские столетия от J2000

        // 3. Формула GMST из книги (для эпохи J2000)
        val GMST = 6.656306 + 2400.051262 * T + 0.00002581 * T * T

        // 4. Приводим к диапазону 0-24 часа
        return GMST % 24
    }

    fun JulianDate(calendar: Calendar): Double {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        var month = calendar.get(Calendar.MONTH) + 1  // Calendar.MONTH начинается с 0
        var year = calendar.get(Calendar.YEAR)
        val hours = calendar.get(Calendar.HOUR_OF_DAY) / 24.0 +
                calendar.get(Calendar.MINUTE) / 1440.0 +
                calendar.get(Calendar.SECOND) / 86400.0

        // Преобразование для января и февраля
        if (month <= 2) {
            year -= 1
            month += 12
        }

        val a = year / 100
        val b = 2 - a + a / 4

        val jd = (365.25 * (year + 4716)).toInt() +
                (30.6001 * (month + 1)).toInt() +
                day + b - 1524.5 + hours

        return jd
    }

    // Создаем календарь с текущей датой
    fun getCurrentCalendar() : Calendar{
        val calendar: Calendar = Calendar.getInstance()
        return calendar
    }

    fun getTimeInDouble (calendar : Calendar) : Double{
        var sec = calendar.get(Calendar.SECOND)
        var minute = calendar.get(Calendar.MINUTE)
        var hour = calendar.get(Calendar.HOUR)

        var secInDouble: Double = 0.0
        if(sec != 0) {
            secInDouble = sec / 60.0
        }
        var result = (minute + secInDouble)/60.0
        return result + hour
    }

    fun DaysSinceEpoch(currentCalendar : Calendar) : Long{
        val days = (currentCalendar.timeInMillis - epoch.timeInMillis)/(1000 * 60 * 60 * 24)
        return days
    }

    fun getGST(calendar: Calendar): Double {
        // 1. Получаем текущую дату в UT (всемирное время)
        val utCalendar = Calendar.getInstance().apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
            time = calendar.time
        }

        // 2. Вычисляем юлианскую дату (JD) для J2000
        val JD = JulianDate(utCalendar) // Ваша функция JulianDate()
        val daysSinceJ2000 = JD - 2451545.0

        // 3. Вычисляем GST по формуле IAU 2006 (в градусах)
        val gstDegrees = (280.46061837 + 360.98564736629 * daysSinceJ2000) % 360

        // 4. Конвертируем в часы (15° = 1 час)
        var gstHours = gstDegrees / 15.0

        // 5. Нормализуем результат (0-24 часа)
        if (gstHours < 0) gstHours += 24.0

        return gstHours
    }

    fun getLST(gst: Double, longitude: Double): Double{
        var lst = gst + (longitude/15.0)
        if(lst >= 24) lst -= 24
        return lst
    }


// Добавить получение B для каждого года из астрономического ежегодника (см. с 30)
}