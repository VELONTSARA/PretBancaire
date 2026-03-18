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

class PretAdapter(context: Context, resource: Int, objects: List<Pret>) :
    ArrayAdapter<Pret>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
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
        pret?.let {
            tvNom.text = it.nomClient
            tvBanque.text = it.nomBanque
            tvMontant.text = String.format("%.2f", it.montant)

            // Formatage de la date pour que ce soit joli
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            tvDate.text = sdf.format(it.datePret)

            tvTotal.text = String.format("%.2f", it.montantAPayer)
        }

        return view
    }
}