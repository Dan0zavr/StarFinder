package com.example.starfinder

import android.R.attr.angle
import android.R.attr.height
import android.R.attr.width
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class OverlayView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val arrowDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow)!!
    private val paint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
        style = Paint.Style.FILL
    }

    private var relativeAzimuth: Float = 0f // Разница между направлением на звезду и текущим азимутом (-180..180)

    fun updateDirection(relAzimuth: Float) {
        // Нормализуем угол
        relativeAzimuth = when {
            relAzimuth > 180 -> relAzimuth - 360
            relAzimuth < -180 -> relAzimuth + 360
            else -> relAzimuth
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        // Рисуем стрелку
        canvas.save()
        canvas.rotate(relativeAzimuth, centerX, centerY)
        arrowDrawable.setBounds(
            (centerX - 50).toInt(),
            (centerY - 100).toInt(),  // Смещаем стрелку выше центра
            (centerX + 50).toInt(),
            (centerY).toInt()
        )
        arrowDrawable.draw(canvas)
        canvas.restore()

        // Отладочная информация
        canvas.drawText("ΔAz: ${relativeAzimuth.toInt()}°", 50f, 100f, paint)
    }
}