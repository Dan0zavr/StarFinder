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

    var targetAzimuth: Float = 0f
    var currentAzimuth: Float = 0f
    var targetAltitude: Float = 0f
    var currentAltitude: Float = 0f
    private var direction = 0f

    fun updateDirection(newDirection: Float) {
        direction = newDirection
        invalidate()
    }

    private val arrowDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow)!!
    private val paint = Paint().apply {
        color = Color.WHITE
        textSize = 48f
    }

    private var targetDirection: Float = 0f // Угол в градусах (0-360)

    fun updateDirections(
        targetAzimuth: Float,
        currentAzimuth: Float,
        targetAltitude: Float,
        currentAltitude: Float
    ) {
        this.targetAzimuth = targetAzimuth
        this.currentAzimuth = currentAzimuth
        this.targetAltitude = targetAltitude
        this.currentAltitude = currentAltitude
        invalidate()  // чтобы перерисовать стрелку
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f

        // Вычисляем относительный азимут (куда указывать стрелке)
        val deltaAzimuth = ((targetAzimuth - currentAzimuth + 360) % 360)
        val angleRad = Math.toRadians(deltaAzimuth.toDouble())

        // Расстояние от центра — стрелка будет по кругу
        val radius = (min(width, height) / 2f) - 100f
        val arrowX = centerX + radius * cos(angleRad).toFloat()
        val arrowY = centerY + radius * sin(angleRad).toFloat()

        // Поворачиваем стрелку по направлению
        canvas.save()
        canvas.translate(arrowX, arrowY)
        canvas.rotate(deltaAzimuth)
        arrowDrawable.setBounds(-50, -50, 50, 50)
        arrowDrawable.draw(canvas)
        canvas.restore()
    }
}