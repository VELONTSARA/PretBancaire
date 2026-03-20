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
import okhttp3.ResponseBody
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

            etNumCompte.setText(p.num_compte)
            etNom.setText(p.nom_client)
            etBanque.setText(p.nom_banque)
            etMontant.setText(p.montant.toString())
            etTaux.setText(p.taux_de_pret.toString())

            // Dans onCreateView après l'initialisation de etDate
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            etDate.setText(sdf.format(Date()))
        } else {
            // MODE NOUVEAU (on nettoie au cas où)
            tvTitre.text = "Nouveau prêt"
            btnEnregistrer.text = "ENREGISTRER"
        }
    }

    private fun enregistrerOuModifier() {
        val mainActivity = (activity as MainActivity)
        val montantStr = etMontant.text.toString()
        val tauxStr = etTaux.text.toString()
        val nom = etNom.text.toString()

        if (montantStr.isNotEmpty() && nom.isNotEmpty()) {
            val montant = montantStr.toDouble()
            val taux = tauxStr.toDoubleOrNull() ?: 0.0

            // On prépare l'objet com.bernado.pretbancaire.models.Pret pour l'envoi
            // Note : On envoie la date au format String "yyyy-MM-dd" pour PostgreSQL
            val dateAffichee = etDate.text.toString() // Format JJ/MM/AAAA
            val sdfAffiche = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val sdfServeur = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val dateEnvoi = try {
                val dateObj = sdfAffiche.parse(dateAffichee)
                sdfServeur.format(dateObj!!)
            } catch (e: Exception) {
                sdfServeur.format(Date()) // Repli sur aujourd'hui si erreur
            }

            val pretAEnvoyer = Pret(
                num_compte = etNumCompte.text.toString(),
                nom_client = nom,
                nom_banque = etBanque.text.toString(),
                montant = montant,
                date_pret = dateEnvoi,
                taux_de_pret = taux
            )

            // On récupère l'apiService de la MainActivity
            val api = mainActivity.apiService

            if (mainActivity.indexAModifier == -1) {
                // --- MODE ENREGISTREMENT (POST) ---
                api.ajouterPret(pretAEnvoyer).enqueue(object : retrofit2.Callback<okhttp3.ResponseBody> { // <--- Changé ici
                    override fun onResponse(
                        call: retrofit2.Call<okhttp3.ResponseBody>,
                        response: retrofit2.Response<okhttp3.ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            // ENFIN ! Tu entreras ici car Android ne cherchera plus à
                            // transformer le JSON complexe du serveur en objet Pret local.
                            Toast.makeText(context, "✅ Enregistré avec succès !", Toast.LENGTH_SHORT).show()
                            resetForm()

                            // Si tu veux quand même voir ce que le serveur a répondu :
                            val reponseBrute = response.body()?.string()
                            android.util.Log.d("SERVEUR_OK", "Le serveur a dit : $reponseBrute")

                        } else {
                            Toast.makeText(context, "❌ Erreur Serveur : ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                        // Si tu tombes encore ici, c'est VRAIMENT un problème de réseau (IP ou Pare-feu)
                        android.util.Log.e("API_FAIL", "Erreur : ${t.message}")
                        Toast.makeText(context, "🔌 Problème de connexion au PC", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                // --- MODE MODIFICATION (A faire plus tard avec un PUT si tu veux) ---
                Toast.makeText(context, "Mode modification à implémenter", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Veuillez remplir les champs obligatoires", Toast.LENGTH_SHORT).show()
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