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
    // AJOUT DES DEUX LAYOUTS
    private lateinit var layoutContenu: View
    private lateinit var layoutChargement: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        // INITIALISATION
        listView = view.findViewById(R.id.lv_historique)
        layoutContenu = view.findViewById(R.id.layout_contenu)
        layoutChargement = view.findViewById(R.id.layout_chargement)

        listView = view.findViewById(R.id.lv_historique)


        listView.setOnItemLongClickListener { _, _, position, _ ->
            val mainActivity = activity as? MainActivity
            val liste = mainActivity?.listeGlobalPrets ?: mutableListOf()
            val pretSelectionne = liste[position]

            // --- REMPLACE PAR CECI ---
            val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
            val layoutModal = layoutInflater.inflate(R.layout.layout_modal_options, null)

// Titre dynamique
            layoutModal.findViewById<TextView>(R.id.tv_modal_title).text = "Options : ${pretSelectionne.nom_client}"

// Action MODIFIER
            layoutModal.findViewById<View>(R.id.btn_modal_edit).setOnClickListener {
                mainActivity?.indexAModifier = position
                mainActivity?.changerOnglet(0)
                dialog.dismiss()
                Toast.makeText(context, "Modification en cours...", Toast.LENGTH_LONG).show()

            }

// Action SUPPRIMER
            layoutModal.findViewById<View>(R.id.btn_modal_delete).setOnClickListener {
                dialog.dismiss()
                // On garde une petite confirmation classique avant de supprimer (plus sûr)
                AlertDialog.Builder(requireContext())
                    .setTitle("Confirmation")
                    .setMessage("Voulez-vous vraiment supprimer ce prêt ?")
                    .setPositiveButton("Oui") { _, _ ->
                        supprimerPretSurServeur(pretSelectionne.num_compte, position)
                    }
                    .setNegativeButton("Non", null)
                    .show()
            }

            dialog.setContentView(layoutModal)
            dialog.show()
// -------------------------
            true
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        chargerDonneesDepuisServeur()
    }

    private fun chargerDonneesDepuisServeur() {
        // 1. ON AFFICHE LE CERCLE ET ON CACHE LA LISTE
        layoutChargement.visibility = View.VISIBLE
        layoutContenu.visibility = View.GONE

        val mainActivity = (activity as? MainActivity)
        val api = mainActivity?.apiService

        api?.getTousLesPrets()?.enqueue(object : retrofit2.Callback<List<Pret>> {
            override fun onResponse(call: retrofit2.Call<List<Pret>>, response: retrofit2.Response<List<Pret>>) {
                // 2. ON CACHE LE CERCLE ET ON AFFICHE LE CONTENU
                layoutChargement.visibility = View.GONE
                layoutContenu.visibility = View.VISIBLE
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
                // 3. MÊME EN CAS D'ERREUR, ON CACHE LE CERCLE POUR NE PAS BLOQUER L'ÉCRAN
                layoutChargement.visibility = View.GONE
                layoutContenu.visibility = View.VISIBLE
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

            // On lie les nouveaux IDs
            view?.findViewById<TextView>(R.id.tv_stat_total)?.text = String.format("%,.2f Ar", total)
            view?.findViewById<TextView>(R.id.tv_stat_min)?.text = String.format("%,.2f Ar", min)
            view?.findViewById<TextView>(R.id.tv_stat_max)?.text = String.format("%,.2f Ar", max)
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
                    afficherMessage("Prêt supprimé avec succès")
                          } else {
                    afficherMessage("Erreur serveur : ${response.code()}", estErreur = true)
                   // Toast.makeText(context, "❌ Erreur serveur : ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                Toast.makeText(context, "🔌 Erreur de connexion au PC", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun afficherMessage(message: String, estErreur: Boolean = false) {
        val snack = com.google.android.material.snackbar.Snackbar.make(requireView(), message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG)

        // On personnalise la couleur selon le type de message
        if (estErreur) {
            snack.setBackgroundTint(android.graphics.Color.parseColor("#C62828")) // Rouge pro
        } else {
            snack.setBackgroundTint(android.graphics.Color.parseColor("#2E7D32")) // Vert pro
        }

        snack.setTextColor(android.graphics.Color.WHITE)
        snack.show()
    }
}