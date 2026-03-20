package com.bernado.pretbancaire.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.bernado.pretbancaire.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // On lie le XML avec la barre de chargement que tu as donné
        setContentView(R.layout.activity_splash)

        // On cache la barre de titre pour faire plus propre
        supportActionBar?.hide()

        // On attend 3 secondes avant de lancer MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // On ferme le splash pour ne pas y revenir avec le bouton retour
        }, 3000)
    }
}