package com.bernado.pretbancaire.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bernado.pretbancaire.R
import com.bernado.pretbancaire.activities.MainActivity

class StatsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)
        return view
    }

    override fun onResume() {
        super.onResume()
        updateCharts()
    }

    private fun updateCharts() {
        val maListe = (activity as? MainActivity)?.listeGlobalPrets ?: return
        if (maListe.isNotEmpty()) {
            val total = maListe.sumOf { it.montantAPayer }
            val min = maListe.minOf { it.montantAPayer }
            val max = maListe.maxOf { it.montantAPayer }

            val pieChart = view?.findViewById<com.bernado.pretbancaire.utils.PieChartView>(R.id.pie_chart)
            pieChart?.setData(total, min, max)
        }
    }
}