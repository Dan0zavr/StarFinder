package com.example.starfinder

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.*

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val arrowDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_arrow)?.apply {
        setTint(Color.WHITE)
    }

    private val squarePaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 4f.dpToPx(context)
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 16f.dpToPx(context)
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private var currentX: Float = 0f
    private var currentY: Float = 0f
    private var isVisible: Boolean = false
    private var starName: String? = null

    fun updatePosition(screenX: Double, screenY: Double, visible: Boolean, name: String? = null) {
        // Плавное изменение позиции
        currentX = lowPassFilter(screenX.toFloat(), currentX)
        currentY = lowPassFilter((-screenY).toFloat(), currentY) // Инверсия Y
        isVisible = visible
        starName = name

        // Ограничение частоты обновления
        if (System.currentTimeMillis() - lastUpdateTime > 50) {
            postInvalidate()
            lastUpdateTime = System.currentTimeMillis()
        }
    }

    private var lastUpdateTime: Long = 0

    private fun lowPassFilter(new: Float, old: Float): Float {
        val alpha = 0.2f // Коэффициент сглаживания
        return old + alpha * (new - old)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        val centerX = width / 2f
        val centerY = height / 2f
        val margin = 32f.dpToPx(context)
        val indicatorSize = 50f.dpToPx(context)

        if (isVisible) {
            // Рисуем квадрат
            canvas.drawRect(
                centerX - indicatorSize/2,
                centerY - indicatorSize/2,
                centerX + indicatorSize/2,
                centerY + indicatorSize/2,
                squarePaint
            )

            starName?.let {
                canvas.drawText(it, centerX, centerY + indicatorSize + 24f.dpToPx(context), textPaint)
            }
        } else {
            // Рассчитываем позицию на границе экрана
            val (boundedX, boundedY) = calculateEdgePosition(centerX, centerY, margin)

            // Рисуем стрелку
            arrowDrawable?.let { drawable ->
                // Угол поворота (0 - вправо, 90 - вверх)
                val angle = Math.toDegrees(atan2(currentY.toDouble(), currentX.toDouble())).toFloat()

                val arrowSize = 24f.dpToPx(context)
                val arrowPadding = 16f.dpToPx(context)

                canvas.save()
                canvas.translate(boundedX, boundedY)
                canvas.rotate(angle)
                // Смещаем стрелку от центра к краю
                canvas.translate(arrowPadding, 0f)

                drawable.setBounds(
                    (-arrowSize/2).toInt(),
                    (-arrowSize/4).toInt(), // Более плоская стрелка
                    (arrowSize/2).toInt(),
                    (arrowSize/4).toInt()
                )
                drawable.draw(canvas)
                canvas.restore()
            }

            // Рисуем текст
            starName?.let {
                val textY = if (boundedY < centerY) {
                    boundedY - 8f.dpToPx(context)
                } else {
                    boundedY + 24f.dpToPx(context)
                }
                canvas.drawText(it, boundedX, textY, textPaint)
            }
        }
    }

    private fun calculateEdgePosition(centerX: Float, centerY: Float, margin: Float): Pair<Float, Float> {
        val aspectRatio = width.toFloat() / height.toFloat()

        return if (abs(currentX) > abs(currentY) * aspectRatio) {
            val edgeX = if (currentX > 0) width - margin else margin
            val edgeY = centerY + (edgeX - centerX) * currentY / currentX
            edgeX to edgeY.coerceIn(margin, height - margin)
        } else {
            val edgeY = if (currentY > 0) height - margin else margin
            val edgeX = centerX + (edgeY - centerY) * currentX / currentY
            edgeX.coerceIn(margin, width - margin) to edgeY
        }
    }
}

fun Float.dpToPx(context: Context): Float {
    return this * context.resources.displayMetrics.density
}