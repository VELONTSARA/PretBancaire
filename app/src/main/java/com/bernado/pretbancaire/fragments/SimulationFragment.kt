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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    private lateinit var btnAnnuler: Button

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
        // Formatage automatique du champ montant à la sortie du focus
        etMontant.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { // L'utilisateur a fini de taper et a cliqué ailleurs
                val input = etMontant.text.toString().replace(" ", "").replace(",", ".")
                val value = input.toDoubleOrNull()

                if (value != null) {
                    val symbols = java.text.DecimalFormatSymbols(Locale.FRENCH)
                    symbols.groupingSeparator = ' '
                    symbols.decimalSeparator = ','
                    val df = java.text.DecimalFormat("#,###.00", symbols)

                    etMontant.setText(df.format(value))
                }
            } else {
                // Optionnel : Quand il reclique dedans, on enlève les espaces pour faciliter la modification
                val rawValue = etMontant.text.toString().replace(" ", "").replace(",", ".")
                if (rawValue.toDoubleOrNull() != null) {
                    // On peut choisir de laisser tel quel ou de simplifier
                }
            }
        }

        etTaux = view.findViewById<EditText>(R.id.et_taux)
        btnEnregistrer = view.findViewById<Button>(R.id.btn_calculer)
        tvResultat = view.findViewById<TextView>(R.id.tv_resultat)

        // Ecouteur pour calcul auto (ton code actuel est bon ici)
        setupAutoCalcul()

        btnEnregistrer.setOnClickListener {
            enregistrerOuModifier()
        }
        // Dans onCreateView, ajoute l'initialisation :
        btnAnnuler = view.findViewById(R.id.btn_annuler)

        btnAnnuler.setOnClickListener {
            resetForm()
            com.google.android.material.snackbar.Snackbar.make(requireView(), "Modification annulée", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                .setBackgroundTint(android.graphics.Color.parseColor("#455A64")) // Gris bleu pro
                .show()
        }

        return view
    }
    override fun onPause() {
        super.onPause()
        // Optionnel : si on quitte l'écran sans enregistrer, on annule la modif
        // (Mais attention, certains utilisateurs préfèrent que ça reste écrit)
        // val mainActivity = (activity as MainActivity)
        // mainActivity.indexAModifier = -1
    }
    // --- C'EST ICI QUE CA SE JOUE ---
    override fun onResume() {
        super.onResume()
        val mainActivity = (activity as MainActivity)
        val index = mainActivity.indexAModifier

        if (index != -1) {
            // --- MODE MODIFICATION ---
            val p = mainActivity.listeGlobalPrets[index]
            tvTitre.text = "Modifier un prêt"
            btnEnregistrer.text = "METTRE À JOUR"
            btnAnnuler.visibility = View.VISIBLE

            etNumCompte.setText(p.num_compte)
            etNumCompte.isEnabled = false
            etNom.setText(p.nom_client)
            etBanque.setText(p.nom_banque)

            // --- C'EST ICI QU'ON FORMATE LE MONTANT ---
            val symbols = java.text.DecimalFormatSymbols(Locale.FRENCH)
            symbols.groupingSeparator = ' '
            symbols.decimalSeparator = ','
            val df = java.text.DecimalFormat("#,###.00", symbols)

            // On transforme le 10000.0 en "10 000,00"
            val montantFormate = df.format(p.montant)
            etMontant.setText(montantFormate)

            etTaux.setText(p.taux_de_pret.toString())
            // Dans onCreateView après l'initialisation de etDate
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            etDate.setText(sdf.format(Date()))
            // On appelle le calcul auto pour que le bandeau de résultat soit aussi à jour
            calculerEtAfficher()

        } else {
            // MODE NOUVEAU (on nettoie au cas où)
            tvTitre.text = "Nouveau prêt"
            btnEnregistrer.text = "ENREGISTRER"
            btnAnnuler.visibility = View.GONE // ON CACHE LE BOUTON
            etNumCompte.isEnabled = true
        }
    }

    private fun enregistrerOuModifier() {
        val mainActivity = (activity as MainActivity)
        val montantStr = etMontant.text.toString()
        val tauxStr = etTaux.text.toString()
        val nom = etNom.text.toString()

        if (montantStr.isNotEmpty() && nom.isNotEmpty()) {
          //  val montant = montantStr.toDouble()
            val montantBrut = extraireMontantPur(etMontant)
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
                montant = montantBrut,
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
                            afficherMessage("Prêt enregistré avec succès")
                              resetForm()

                            // Si tu veux quand même voir ce que le serveur a répondu :
                            val reponseBrute = response.body()?.string()
                            android.util.Log.d("SERVEUR_OK", "Le serveur a dit : $reponseBrute")

                        } else {
                            afficherMessage("Erreur Serveur : ${response.code()}", true)
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<okhttp3.ResponseBody>, t: Throwable) {
                        // Si tu tombes encore ici, c'est VRAIMENT un problème de réseau (IP ou Pare-feu)
                        android.util.Log.e("API_FAIL", "Erreur : ${t.message}")
                        afficherMessage("Problème de connexion au serveur", true)
                       // Toast.makeText(context, "🔌 Problème de connexion au PC", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                val numCompte = etNumCompte.text.toString()
                api.modifierPret(numCompte, pretAEnvoyer).enqueue(object : retrofit2.Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            afficherMessage("Prêt modifiée avec succès")

                            // IMPORTANT : On remet l'index à -1 pour le prochain prêt
                            mainActivity.indexAModifier = -1
                            resetForm()

                            // On retourne sur l'historique automatiquement
                            mainActivity.changerOnglet(1)
                        } else {
                            afficherMessage("Erreur serveur : ${response.code()}", true)
                        }
                    } override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        afficherMessage("Problème de connexion au serveur", true)
                    }
                })
                }
        } else {
            Toast.makeText(context, "Veuillez remplir les champs obligatoires", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetForm() {
        val mainActivity = (activity as MainActivity)
        mainActivity.indexAModifier = -1

        etNom.text.clear()
        etBanque.text.clear()
        etMontant.text.clear()
        etTaux.text.clear()
        etNumCompte.text.clear()
        etNumCompte.isEnabled = true

        tvTitre.text = "Nouveau prêt"
        btnEnregistrer.text = "ENREGISTRER"
        btnAnnuler.visibility = View.GONE // Cacher le bouton après reset

        // Remettre la date du jour par défaut
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        etDate.setText(sdf.format(Date()))

        tvResultat.text = "Résultat : --"
    }
    private fun setupAutoCalcul() {
        // 1. GESTION DU TAUX (0 à 100)
        etTaux.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.isNotEmpty()) {
                    val value = input.toDoubleOrNull() ?: 0.0
                    if (value > 100.0) {
                        etTaux.setText("100")
                        etTaux.setSelection(etTaux.text.length) // Curseur à la fin
                    } else if (value < 0) {
                        etTaux.setText("0")
                    }
                }
                calculerEtAfficher() // Relancer le calcul
            }
        })

        // 2. GESTION DU MONTANT (Calcul auto)
        etMontant.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Note: On ne formate pas l'EditText ici pour éviter les bugs de saisie,
                // mais on met à jour le résultat en temps réel avec un beau format.
                calculerEtAfficher()
            }
        })
    }

    // Fonction isolée pour mettre à jour le tvResultat avec le formatage 2 500 000,00
    private fun calculerEtAfficher() {
        // On utilise notre nouvelle fonction de nettoyage ici
        val m = extraireMontantPur(etMontant)
        val t = etTaux.text.toString().toDoubleOrNull() ?: 0.0

        if (m > 0) {
            val res = m * (1 + (t / 100))

            val symbols = java.text.DecimalFormatSymbols(Locale.FRENCH)
            symbols.groupingSeparator = ' '
            symbols.decimalSeparator = ','
            val df = java.text.DecimalFormat("#,###.00", symbols)

            tvResultat.text = "Montant à payer : ${df.format(res)} Ar"
        } else {
            tvResultat.text = "Résultat : --"
        }
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
    private fun extraireMontantPur(editText: EditText): Double {
        val texte = editText.text.toString()
            .replace(" ", "")      // Enlève l'espace simple
            .replace("\u00A0", "") // Enlève l'espace insécable (généré par le formateur)
            .replace(",", ".")     // Remplace la virgule par un point pour le Double
        return texte.toDoubleOrNull() ?: 0.0
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