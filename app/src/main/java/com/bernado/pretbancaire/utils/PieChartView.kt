package com.bernado.pretbancaire.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class PieChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var angles = floatArrayOf(0f, 0f, 0f)
    private var values = doubleArrayOf(0.0, 0.0, 0.0) // On stocke les montants ici
    private val colors = intArrayOf(Color.BLUE, Color.GREEN, Color.RED)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    fun setData(total: Double, min: Double, max: Double) {
        values = doubleArrayOf(total, min, max)
        val sum = (total + min + max).toFloat()

        if (sum > 0) {
            angles = floatArrayOf(
                (total.toFloat() / sum) * 360f,
                (min.toFloat() / sum) * 360f,
                (max.toFloat() / sum) * 360f
            )
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Math.min(centerX, centerY) * 0.70f // On réduit pour laisser de la place aux chiffres

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        var startAngle = 0f

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 28f
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER // Pour centrer le texte au bout du trait
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            strokeWidth = 3f
        }

        for (i in angles.indices) {
            if (angles[i] == 0f) continue

            // 1. Dessiner la part
            paint.color = colors[i]
            canvas.drawArc(rectF, startAngle, angles[i], true, paint)

            // 2. Calculer l'angle du milieu pour le trait
            val middleAngle = startAngle + angles[i] / 2f
            val angleRad = Math.toRadians(middleAngle.toDouble())

            // 3. Points du trait (du bord du cercle vers l'extérieur)
            val startX = centerX + radius * Math.cos(angleRad).toFloat()
            val startY = centerY + radius * Math.sin(angleRad).toFloat()
            val endX = centerX + (radius + 50f) * Math.cos(angleRad).toFloat()
            val endY = centerY + (radius + 50f) * Math.sin(angleRad).toFloat()

            canvas.drawLine(startX, startY, endX, endY, linePaint)

            // 4. Afficher le chiffre au bout du trait
            val textX = centerX + (radius + 80f) * Math.cos(angleRad).toFloat()
            val textY = centerY + (radius + 80f) * Math.sin(angleRad).toFloat()

            // Formatage du chiffre (ex: 1500.0)
            val label = when(i) {
                0 -> "${values[i].toInt()} Ar"
                1 -> "${values[i].toInt()} Ar"
                else -> "${values[i].toInt()} Ar"
            }

            canvas.drawText(label, textX, textY, textPaint)

            startAngle += angles[i]
        }
    }
}