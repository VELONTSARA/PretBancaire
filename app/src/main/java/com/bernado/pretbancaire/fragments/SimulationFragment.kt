package com.bernado.pretbancaire.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bernado.pretbancaire.R
import com.bernado.pretbancaire.models.Pret
import java.util.*

class SimulationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_simulation, container, false)

        // Récupération des composants
        val etNom = view.findViewById<EditText>(R.id.et_nom_client)
        val etBanque = view.findViewById<EditText>(R.id.et_nom_banque)
        val etMontant = view.findViewById<EditText>(R.id.et_montant)
        val etTaux = view.findViewById<EditText>(R.id.et_taux)
        val btnCalculer = view.findViewById<Button>(R.id.btn_calculer)
        val tvResultat = view.findViewById<TextView>(R.id.tv_resultat)

        btnCalculer.setOnClickListener {
            val montant = etMontant.text.toString().toDoubleOrNull() ?: 0.0
            val taux = etTaux.text.toString().toDoubleOrNull() ?: 0.0

            if (montant > 0 && taux > 0) {
                // Un calcul simple pour l'exemple (Intérêts simples)
                val resultat = montant + (montant * taux / 100)

                tvResultat.text = "Total à rembourser : $resultat Ar"

                // On crée l'objet Pret (on pourra l'envoyer à la liste après)
                val nouveauPret = Pret(
                    numeroCompte = "ABC-${Random().nextInt(1000)}",
                    nomClient = etNom.text.toString(),
                    nomBanque = etBanque.text.toString(),
                    montant = montant,
                    datePret = Date(),
                    tauxPret = taux
                )

                Toast.makeText(context, "Simulation enregistrée !", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}