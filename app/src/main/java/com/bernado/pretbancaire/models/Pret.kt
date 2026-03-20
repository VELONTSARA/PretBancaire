package com.bernado.pretbancaire.models

import java.io.Serializable

// Dans Pret.kt (Android)
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true) // <--- AJOUTE CECI
data class Pret(
    // On utilise exactement les mêmes noms que dans ton fichier IntelliJ
    val num_compte: String = "",
    val nom_client: String = "",
    val nom_banque: String = "",
    val montant: Double = 0.0,
    val date_pret: String = "",
    val taux_de_pret: Double = 0.0
) : Serializable {
    val montantAPayer: Double
        get() = montant * (1 + (taux_de_pret / 100))
}