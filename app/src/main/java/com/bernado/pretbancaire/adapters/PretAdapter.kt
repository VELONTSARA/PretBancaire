package com.bernado.pretbancaire.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.bernado.pretbancaire.R
import com.bernado.pretbancaire.models.Pret
import java.text.SimpleDateFormat
import java.util.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

class PretAdapter(context: Context, resource: Int, objects: List<Pret>) :
    ArrayAdapter<Pret>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Dans ta fonction getView :
        val symbols = DecimalFormatSymbols(Locale.FRENCH)
        symbols.groupingSeparator = ' ' // Espace pour les milliers
        symbols.decimalSeparator = ','   // Virgule pour les centimes
        val df = DecimalFormat("#,###.00", symbols)

        // On récupère le layout de la ligne
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_pret, parent, false)

        // On récupère les données du prêt à cette position
        val pret = getItem(position)

        // On fait le lien avec les TextView du XML item_pret
        val tvNom = view.findViewById<TextView>(R.id.tv_item_nom)
        val tvBanque = view.findViewById<TextView>(R.id.tv_item_banque)
        val tvMontant = view.findViewById<TextView>(R.id.tv_item_montant)
        val tvDate = view.findViewById<TextView>(R.id.tv_item_date)
        val tvTotal = view.findViewById<TextView>(R.id.tv_item_total)

        // On remplit les cases
        // Dans getView du PretAdapter.kt
        pret?.let {
            tvNom.text = it.nom_client
            tvBanque.text = it.nom_banque
            tvMontant.text = "${df.format(it.montant)} Ar"

            // CORRECTION DATE : On affiche directement la String du serveur
            // Si tu veux changer le format (ex: yyyy-MM-dd -> dd/MM/yyyy) :
            val dateServeur = it.date_pret // ex: "2026-03-20"
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateObj = parser.parse(dateServeur)
                tvDate.text = formatter.format(dateObj!!)
            } catch (e: Exception) {
                tvDate.text = dateServeur // Si erreur, on affiche la date brute
            }

            tvTotal.text = "${df.format(it.montantAPayer)} Ar"
                //String.format("%.2f Ar", it.montantAPayer)


        }

        return view
    }
}