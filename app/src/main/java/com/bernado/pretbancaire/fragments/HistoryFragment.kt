package com.bernado.pretbancaire.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bernado.pretbancaire.R
import com.bernado.pretbancaire.activities.MainActivity
import com.bernado.pretbancaire.adapters.PretAdapter

class HistoryFragment : Fragment() {

    private lateinit var listView: ListView
    private lateinit var tvStats: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        listView = view.findViewById(R.id.lv_historique)
        tvStats = view.findViewById(R.id.tv_stats_bas)

        // CONFIGURATION DU CLIC LONG POUR MODIFIER/SUPPRIMER
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val activity = activity as? MainActivity
            val liste = activity?.listeGlobalPrets ?: mutableListOf()
            val pretSelectionne = liste[position]

            // Création de la boîte de dialogue
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Options : ${pretSelectionne.nomClient}")

            val options = arrayOf("Modifier", "Supprimer")
            builder.setItems(options) { _, which ->
                when (which) {
                    // Dans le when(which) du setOnItemLongClickListener
                    // Dans le when(which) de ton HistoryFragment
                    0 -> { // MODIFIER
                        val mainActivity = (activity as MainActivity)

                        // 1. On mémorise l'index
                        mainActivity.indexAModifier = position

                        // 2. On change d'onglet proprement via la fonction de l'activité
                        mainActivity.changerOnglet(0)

                        Toast.makeText(context, "Modification en cours...", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        // LOGIQUE DE SUPPRESSION
                        liste.removeAt(position)
                        refreshData() // On rafraîchit l'affichage immédiatement
                        Toast.makeText(context, "Prêt supprimé avec succès", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            builder.show()
            true // Important : consomme le clic long
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun refreshData() {
        val maListe = (activity as? MainActivity)?.listeGlobalPrets ?: mutableListOf()

        val adapter = PretAdapter(requireContext(), R.layout.item_pret, maListe)
        listView.adapter = adapter

        if (maListe.isNotEmpty()) {
            val total = maListe.sumOf { it.montantAPayer }
            val min = maListe.minOf { it.montantAPayer }
            val max = maListe.maxOf { it.montantAPayer }

            tvStats.text = String.format("Total: %.2f | Min: %.2f | Max: %.2f", total, min, max)
        } else {
            tvStats.text = "Aucune donnée enregistrée"
            // Si la liste est vide, on s'assure que l'adapter est vide aussi
            listView.adapter = null
        }
    }

}