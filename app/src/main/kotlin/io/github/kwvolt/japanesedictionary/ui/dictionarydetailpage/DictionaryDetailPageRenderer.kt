package io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage

import android.os.Bundle
import androidx.cardview.widget.CardView
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.tabs.conjugation.ConjugationTabFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.tabs.worddefinition.WordDefinitionTabFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.tabs.worddefinition.WordDefinitionTabPreviewFragment
import io.github.kwvolt.japanesedictionary.util.ViewUtil

class DictionaryDetailPageRenderer(private val isPreview: Boolean) {

    fun applyBackgroundAlpha(cardView: CardView){
        ViewUtil.setCardBackgroundAlpha(cardView, ViewUtil.ALPHA_75_PERCENT)
    }

    fun generateViewPagerAdapter(viewPager: ViewPager2, tabLayout: TabLayout, fragment: Fragment) {
        viewPager.adapter = object : FragmentStateAdapter(fragment) {
            override fun getItemCount() = 2
            override fun createFragment(position: Int): Fragment {
                return generateFragment(position, fragment)
            }
        }
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when(position) {
                0 -> fragment.getString(R.string.dwp_definition_tab)
                1 -> fragment.getString(R.string.dwp_conjugation_tab2)
                else -> {fragment.getString(R.string.dwp_definition_tab)}
            }
        }.attach()

    }

    private fun generateFragment(position: Int, parent: Fragment): Fragment {
        return when(position){
            0 -> {
                if(isPreview) {
                    WordDefinitionTabPreviewFragment().apply {
                        arguments = getWordEntryFormDataBundle(parent)
                    }
                }else {
                    WordDefinitionTabFragment()
                }
            }
            1 -> {
                if(isPreview) {
                    WordDefinitionTabPreviewFragment().apply {
                        arguments = getWordEntryFormDataBundle(parent)
                    }
                }else {
                    ConjugationTabFragment()
                }
            }
            else -> {
                if(isPreview) {
                    WordDefinitionTabPreviewFragment().apply {
                        arguments = getWordEntryFormDataBundle(parent)
                    }
                }else {
                    WordDefinitionTabFragment()
                }
            }
        }
    }

    private fun getWordEntryFormDataBundle(parent: Fragment): Bundle {
        return Bundle().apply {
            putParcelable(DictionaryDetailPagePreviewDialogFragment.ARG_WORD_FORM_ENTRY_DATA,
                BundleCompat.getParcelable(
                    parent.requireArguments(),
                    DictionaryDetailPagePreviewDialogFragment.ARG_WORD_FORM_ENTRY_DATA,
                    WordEntryFormData::class.java
                )
            )
        }
    }
}
