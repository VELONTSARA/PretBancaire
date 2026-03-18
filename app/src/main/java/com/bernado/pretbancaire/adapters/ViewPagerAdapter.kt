package com.bernado.pretbancaire.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bernado.pretbancaire.fragments.HistoryFragment
import com.bernado.pretbancaire.fragments.SimulationFragment
import com.bernado.pretbancaire.fragments.StatsFragment

class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // Nombre d'onglets
    override fun getItemCount(): Int = 3

    // Quel fragment afficher selon la position
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SimulationFragment()
            1 -> HistoryFragment()
            else -> StatsFragment()
        }
    }
}