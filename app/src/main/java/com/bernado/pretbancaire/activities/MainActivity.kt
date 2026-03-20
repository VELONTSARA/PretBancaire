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
            .baseUrl("http://10.112.66.17:8080/") // Ton adresse IP
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
        // Si tu utilises un ViewPager2 (le plus probable pour des onglets)
        val viewPager = findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.view_pager)
        if (viewPager != null) {
            viewPager.currentItem = position
        } else {
            // Si tu utilises une BottomNavigationView à la place
            // val nav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            // nav.selectedItemId = R.id.nav_simulation_id
        }
    }
}