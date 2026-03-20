package com.bernado.pretbancaire.activities

import ApiService
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bernado.pretbancaire.R
import com.bernado.pretbancaire.fragments.HomeFragment
import com.bernado.pretbancaire.models.Pret
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
class MainActivity : AppCompatActivity() {

    val listeGlobalPrets = mutableListOf<Pret>()
    lateinit var apiService: ApiService
    var indexAModifier: Int = -1 // -1 signifie "Nouveau prêt"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. Initialise Retrofit dès le démarrage de l'appli
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.55.128.17:8080/") // Ton adresse IP
            .addConverterFactory(JacksonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }
    }
    fun changerOnglet(position: Int) {
        // 1. On cherche le fragment qui est ACTUELLEMENT dans le conteneur
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        // 2. On vérifie si ce fragment contient le ViewPager (peu importe son nom de classe)
        // On cherche l'ID du ViewPager directement dans la vue du fragment actuel
        val viewPager = currentFragment?.view?.findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.view_pager)

        if (viewPager != null) {
            viewPager.currentItem = position
            android.util.Log.d("DEBUG_NAV", "C'est bon ! On bascule vers l'onglet $position")
        } else {
            // Si on ne le trouve pas, on affiche une erreur précise dans les logs
            android.util.Log.e("DEBUG_NAV", "ViewPager non trouvé. Fragment actuel : ${currentFragment?.javaClass?.simpleName}")
        }
    }
}