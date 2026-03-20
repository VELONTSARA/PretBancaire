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
import com.bernado.pretbancaire.models.Pret

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

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val mainActivity = activity as? MainActivity
            val liste = mainActivity?.listeGlobalPrets ?: mutableListOf()
            val pretSelectionne = liste[position]

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Options : ${pretSelectionne.nom_client}")

            val options = arrayOf("Modifier", "Supprimer")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> { // MODIFIER
                        mainActivity?.indexAModifier = position
                        mainActivity?.changerOnglet(1) // On va sur l'onglet Simulation (index 1)
                        Toast.makeText(context, "Modification en cours...", Toast.LENGTH_SHORT).show()
                    }
                    // ... dans ton onCreateView, dans le builder.setItems(options)
                    1 -> { // OPTION SUPPRIMER
                        val numCompte = pretSelectionne.num_compte

                        // On demande confirmation avant de supprimer
                        AlertDialog.Builder(requireContext())
                            .setTitle("Confirmation")
                            .setMessage("Voulez-vous vraiment supprimer le prêt de ${pretSelectionne.nom_client} ?")
                            .setPositiveButton("Oui") { _, _ ->
                                supprimerPretSurServeur(numCompte, position)
                            }
                            .setNegativeButton("Non", null)
                            .show()
                    }
// ...
                }
            }
            builder.show()
            true
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        chargerDonneesDepuisServeur()
    }

    private fun chargerDonneesDepuisServeur() {
        val mainActivity = (activity as? MainActivity)
        val api = mainActivity?.apiService

        api?.getTousLesPrets()?.enqueue(object : retrofit2.Callback<List<Pret>> {
            override fun onResponse(call: retrofit2.Call<List<Pret>>, response: retrofit2.Response<List<Pret>>) {
                if (response.isSuccessful) {
                    val listeRecue = response.body() ?: mutableListOf()
                    mainActivity?.listeGlobalPrets?.clear()
                    mainActivity?.listeGlobalPrets?.addAll(listeRecue)
                    refreshData()
                } else {
                    Toast.makeText(context, "Erreur : ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<List<Pret>>, t: Throwable) {
                // Affiche l'erreur précise (ex: "MismatchedInputException" ou "CLEARTEXT communication not permitted")
                android.util.Log.e("API_GET_ERROR", "Détail : ${t.message}", t)
                Toast.makeText(context, "Erreur technique : ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    } // <-- Il manquait cette accolade !

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
        }
    }
    private fun supprimerPretSurServeur(numCompte: String, position: Int) {
        val mainActivity = (activity as? MainActivity)
        val api = mainActivity?.apiService

        // On utilise l'ID (num_compte) dans l'URL
        api?.supprimerPret(numCompte)?.enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> {
            override fun onResponse(call: retrofit2.Call<okhttp3.ResponseBody>, response: retrofit2.Response<okhttp3.ResponseBody>) {
                if (response.isSuccessful) {
                    // 1. Supprimer de la liste locale pour éviter de recharger tout
                    mainActivity.listeGlobalPrets.removeAt(position)

                    // 2. Rafraîchir l'interface
                    refreshData()

                    Toast.makeText(context, "✅ Supprimé de la base de données", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "❌ Erreur serveur : ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                Toast.makeText(context, "🔌 Erreur de connexion au PC", Toast.LENGTH_SHORT).show()
            }
        })
    }
}