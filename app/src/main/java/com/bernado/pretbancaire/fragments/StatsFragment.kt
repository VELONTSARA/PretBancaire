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

    private lateinit var layoutContenu: View
    private lateinit var layoutChargement: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        layoutContenu = view.findViewById(R.id.layout_contenu_stats)
        layoutChargement = view.findViewById(R.id.layout_chargement_stats)

        return view
    }

    override fun onResume() {
        super.onResume()
        updateCharts()
    }

    private fun updateCharts() {
        // Afficher le chargement
        layoutChargement.visibility = View.VISIBLE
        layoutContenu.visibility = View.GONE

        val mainActivity = (activity as? MainActivity)
        val maListe = mainActivity?.listeGlobalPrets ?: mutableListOf()

        if (maListe.isNotEmpty()) {
            val total = maListe.sumOf { it.montantAPayer }
            val min = maListe.minOf { it.montantAPayer }
            val max = maListe.maxOf { it.montantAPayer }

            val pieChart = view?.findViewById<com.bernado.pretbancaire.utils.PieChartView>(R.id.pie_chart)
            pieChart?.setData(total, min, max)

            // Formatage propre
            val symbols = java.text.DecimalFormatSymbols(java.util.Locale.FRENCH)
            symbols.groupingSeparator = ' '
            val df = java.text.DecimalFormat("#,###.00", symbols)

            view?.findViewById<TextView>(R.id.tv_stat_total)?.text = String.format("%,.0f Ar", total).replace(",", " ")
            view?.findViewById<TextView>(R.id.tv_stat_max)?.text = String.format("%,.0f Ar", max).replace(",", " ")
            view?.findViewById<TextView>(R.id.tv_stat_min)?.text = String.format("%,.0f Ar", min).replace(",", " ")

            // Cacher le chargement après mise à jour
            layoutChargement.visibility = View.GONE
            layoutContenu.visibility = View.VISIBLE
        } else {
            // Si la liste est vide, on peut aussi afficher un message "Pas de données"
            layoutChargement.visibility = View.GONE
            layoutContenu.visibility = View.VISIBLE
        }
    }
}


// Mise à jour des textes avec formatage "1 250 000 Ar"
