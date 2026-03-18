package com.bernado.pretbancaire.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.bernado.pretbancaire.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. On charge le fichier XML
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 2. On récupère le bouton
        val btnEntrer = view.findViewById<Button>(R.id.btn_entrer)

        // 3. Action au clic
        // Dans HomeFragment.kt, remplace le println par :
        btnEntrer.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MainFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}