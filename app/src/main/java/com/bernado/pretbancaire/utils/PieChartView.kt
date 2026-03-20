package com.bernado.pretbancaire.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class PieChartView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var angles = floatArrayOf(0f, 0f, 0f)
    private var values = doubleArrayOf(0.0, 0.0, 0.0)
    // Couleurs plus modernes (Pastels / Material)
    private val colors = intArrayOf(
        Color.parseColor("#3F51B5"), // Bleu Indigo (Total)
        Color.parseColor("#4CAF50"), // Vert (Min)
        Color.parseColor("#FF5252")  // Rouge (Max)
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE // On passe en STROKE pour faire un anneau
        strokeCap = Paint.Cap.ROUND // Bouts arrondis pour le style
    }

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

        // On définit l'épaisseur de l'anneau (Donut)
        val strokeWidth = width * 0.15f
        paint.strokeWidth = strokeWidth

        val radius = (Math.min(centerX, centerY) * 0.65f)
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        var startAngle = -90f // On commence en haut (12h) pour plus de propreté

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#444444")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        for (i in angles.indices) {
            if (angles[i] <= 0f) continue

            // 1. Dessiner l'arc (Donut slice)
            paint.color = colors[i]
            paint.style = Paint.Style.STROKE
            canvas.drawArc(rectF, startAngle, angles[i], false, paint)

            // 2. Calculer la position pour le texte (au centre de l'arc mais un peu décalé)
            val middleAngle = startAngle + angles[i] / 2f
            val angleRad = Math.toRadians(middleAngle.toDouble())

            // On place le texte un peu plus loin que le rayon pour ne pas chevaucher
            val textX = centerX + (radius + strokeWidth + 20f) * Math.cos(angleRad).toFloat()
            val textY = centerY + (radius + strokeWidth + 20f) * Math.sin(angleRad).toFloat()

            // 3. Afficher la valeur formatée (ex: 1.2M)
            val label = formatValue(values[i])
            canvas.drawText(label, textX, textY, textPaint)

            startAngle += angles[i]
        }

        // 4. Optionnel : Afficher le mot "TOTAL" au centre du trou
        textPaint.textSize = 30f
        textPaint.color = Color.GRAY
        canvas.drawText("ANALYSES", centerX, centerY, textPaint)
    }

    // Petite fonction pour éviter les textes trop longs sur le graphique
    private fun formatValue(value: Double): String {
        return if (value >= 1000000) String.format("%.1fM", value / 1000000)
        else if (value >= 1000) String.format("%.0fk", value / 1000)
        else value.toInt().toString()
    }
}