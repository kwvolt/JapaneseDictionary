package io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.tabs.WordDefinitionTabFragment

class DictionaryDetailPageViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            1 -> WordDefinitionTabFragment()
            else -> WordDefinitionTabFragment()
        }
    }
}