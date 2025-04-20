package com.example.starfinder

import android.R.attr.angle
import android.R.attr.height
import android.R.attr.width
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class OverlayView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var angle: Float = 0f

    // Инициализация paint (лучше один раз, чем каждый раз в onDraw)
    private val paint: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Получаем центр экрана
        val cx = width / 2f
        val cy = height / 2f
        val length = 200f

        // Вычисляем координаты конца стрелки
        val endX = cx + length * cos(Math.toRadians(angle.toDouble())).toFloat()
        val endY = cy - length * sin(Math.toRadians(angle.toDouble())).toFloat()

        // Рисуем линию (стрелку)
        canvas.drawLine(cx, cy, endX, endY, paint)
    }
}

