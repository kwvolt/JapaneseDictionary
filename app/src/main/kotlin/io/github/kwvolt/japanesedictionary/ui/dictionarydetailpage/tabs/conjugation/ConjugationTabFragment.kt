package io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.tabs.conjugation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DwpConjugationTabBinding
import io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage.DictionaryDetailPageViewModel
import io.github.kwvolt.japanesedictionary.presentation.mainactivity.LoadingViewModel
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.DictionaryDetailPageViewFragment
import io.github.kwvolt.japanesedictionary.util.ViewUtil
import io.github.kwvolt.japanesedictionary.util.viewBinding
import kotlin.getValue

class ConjugationTabFragment(): Fragment(R.layout.dwp_conjugation_tab) {
    private val binding: DwpConjugationTabBinding by viewBinding(DwpConjugationTabBinding::bind)
    private val loadingViewModel: LoadingViewModel by activityViewModels()

    // shared viewmodel
    private val parentFragmentTyped: DictionaryDetailPageViewFragment by lazy {
        val parent = requireParentFragment()
        parent as? DictionaryDetailPageViewFragment ?: throw IllegalStateException("Parent fragment must be DictionaryDetailPageViewFragment")
    }
    private val factory: ViewModelProvider.Factory by lazy {
        parentFragmentTyped.dictionaryDetailPageViewModelFactory
    }
    private val viewModel: DictionaryDetailPageViewModel by viewModels(
        ownerProducer = { parentFragmentTyped },
        factoryProducer = { factory }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtil.setNavigationTransition(view)

        viewModel.loadConjugation()


    }
}