package com.bernado.pretbancaire.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bernado.pretbancaire.R
import com.bernado.pretbancaire.activities.MainActivity
import com.bernado.pretbancaire.models.Pret
import java.text.SimpleDateFormat
import java.util.*

class SimulationFragment : Fragment() {

    // On déclare les composants en haut pour y accéder dans onResume
    private lateinit var etNumCompte: EditText
    private lateinit var etDate: EditText
    private lateinit var etNom: EditText
    private lateinit var etBanque: EditText
    private lateinit var etMontant: EditText
    private lateinit var etTaux: EditText
    private lateinit var btnEnregistrer: Button
    private lateinit var tvResultat: TextView
    private lateinit var tvTitre: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_simulation, container, false)
        etDate = view.findViewById(R.id.et_date_pret)
        etDate.isFocusable = false // Empêche le clavier de s'ouvrir
        etDate.isClickable = true

        setupDatePicker()
        // Initialisation des vues
        tvTitre = view.findViewById(R.id.tv_titre_form)
        etNumCompte = view.findViewById(R.id.et_num_compte)
        etDate = view.findViewById(R.id.et_date_pret)
        etNom = view.findViewById<EditText>(R.id.et_nom_client)
        etBanque = view.findViewById<EditText>(R.id.et_nom_banque)
        etMontant = view.findViewById<EditText>(R.id.et_montant)
        etTaux = view.findViewById<EditText>(R.id.et_taux)
        btnEnregistrer = view.findViewById<Button>(R.id.btn_calculer)
        tvResultat = view.findViewById<TextView>(R.id.tv_resultat)

        // Ecouteur pour calcul auto (ton code actuel est bon ici)
        setupAutoCalcul()

        btnEnregistrer.setOnClickListener {
            enregistrerOuModifier()
        }

        return view
    }

    // --- C'EST ICI QUE CA SE JOUE ---
    override fun onResume() {
        super.onResume()
        val mainActivity = (activity as MainActivity)
        val index = mainActivity.indexAModifier

        if (index != -1) {
            // MODE MODIFICATION
            val p = mainActivity.listeGlobalPrets[index]
            tvTitre.text = "Modifier un prêt"
            btnEnregistrer.text = "METTRE À JOUR"

            etNumCompte.setText(p.numeroCompte)
            etNom.setText(p.nomClient)
            etBanque.setText(p.nomBanque)
            etMontant.setText(p.montant.toString())
            etTaux.setText(p.tauxPret.toString())

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            etDate.setText(sdf.format(p.datePret))
        } else {
            // MODE NOUVEAU (on nettoie au cas où)
            tvTitre.text = "Nouveau prêt"
            btnEnregistrer.text = "ENREGISTRER"
        }
    }

    private fun enregistrerOuModifier() {
        val mainActivity = (activity as MainActivity)
        val montant = etMontant.text.toString().toDoubleOrNull() ?: 0.0
        val taux = etTaux.text.toString().toDoubleOrNull() ?: 0.0
        val nom = etNom.text.toString()

        if (montant > 0 && nom.isNotEmpty()) {
            val montantAPayer = montant * (1 + (taux / 100))
            val pretResultat = Pret(
                numeroCompte = etNumCompte.text.toString(),
                nomClient = nom,
                nomBanque = etBanque.text.toString(),
                montant = montant,
                datePret = Date(),
                tauxPret = taux,
                montantAPayer = montantAPayer
            )

            if (mainActivity.indexAModifier == -1) {
                mainActivity.listeGlobalPrets.add(pretResultat)
                Toast.makeText(context, "Enregistré !", Toast.LENGTH_SHORT).show()
            } else {
                mainActivity.listeGlobalPrets[mainActivity.indexAModifier] = pretResultat
                mainActivity.indexAModifier = -1 // RESET IMPORTANT
                Toast.makeText(context, "Modifié avec succès !", Toast.LENGTH_SHORT).show()
            }
            resetForm()
        }
    }

    private fun resetForm() {
        etNom.text.clear()
        etBanque.text.clear()
        etMontant.text.clear()
        etTaux.text.clear()
        etNumCompte.text.clear()
        tvResultat.text = "Résultat : --"
        tvTitre.text = "Nouveau prêt"
        btnEnregistrer.text = "ENREGISTRER"
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        etDate.setText(sdf.format(Date()))
    }

    private fun setupAutoCalcul() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val m = etMontant.text.toString().toDoubleOrNull() ?: 0.0
                val t = etTaux.text.toString().toDoubleOrNull() ?: 0.0
                if (m > 0) {
                    val res = m * (1 + (t / 100))
                    tvResultat.text = String.format("Montant à payer : %.2f Ar", res)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        etMontant.addTextChangedListener(watcher)
        etTaux.addTextChangedListener(watcher)
    }

    private fun setupDatePicker() {
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = android.app.DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    // On formate la date sélectionnée
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(selectedYear, selectedMonth, selectedDay)
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    etDate.setText(sdf.format(selectedDate.time))
                },
                year, month, day
            )
            datePickerDialog.show()
        }
    }
}