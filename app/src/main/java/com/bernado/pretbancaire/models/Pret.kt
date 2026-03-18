package com.bernado.pretbancaire.models
import java.util.Date

data class Pret (
    val numeroCompte: String,
    val nomClient: String,
    val nomBanque: String,
    val montant: Double,
    val datePret: Date,
    val tauxPret: Double,
    val montantAPayer: Double
)