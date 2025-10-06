package io.github.kwvolt.japanesedictionary.ui.searchfilter

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.SearchFilterBinding
import io.github.kwvolt.japanesedictionary.domain.model.SearchType
import io.github.kwvolt.japanesedictionary.presentation.search.SearchFilterViewModel
import io.github.kwvolt.japanesedictionary.ui.dictionarylookup.DictionaryLookupFragment
import io.github.kwvolt.japanesedictionary.util.WordClassBindUtil
import io.github.kwvolt.japanesedictionary.util.viewBinding
import kotlinx.coroutines.launch
import kotlin.getValue

class SearchFilterViewFragment: Fragment(R.layout.search_filter) {
    private val binding: SearchFilterBinding by viewBinding(SearchFilterBinding::bind)
    private val viewModelOwner: SearchFilterViewModelOwner by lazy {
        generateSequence(parentFragment) { it.parentFragment }
            .filterIsInstance<SearchFilterViewModelOwner>()
            .firstOrNull()
            ?: throw IllegalStateException("No parent implements SearchFilterViewModelOwner")
    }

    private val factory: ViewModelProvider.Factory by lazy {
        viewModelOwner.searchFilterViewModelFactory
    }

    private val viewModel: SearchFilterViewModel by viewModels(
        ownerProducer = { viewModelOwner as ViewModelStoreOwner },
        factoryProducer = { factory }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            setupListenersAndAdaptersWithoutDropdown()
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.loadWordClassSpinner()
            }
            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.onDropdownReady.collect {
                        WordClassBindUtil.bindWordClassItem(
                            requireContext(),
                            searchFilterMainClassInputDropdown,
                            searchFilterSubClassInputDropdown,
                            onItemSelectedInMainClass = { selectedPosition: Int, bindSubClass: (Boolean) -> Unit ->
                                val hasUpdated = viewModel.setMainClass(selectedPosition)
                                bindSubClass(hasUpdated)
                            },
                            onItemSelectedInSubClass = { selectedPosition ->
                                viewModel.setSubClass(selectedPosition)
                            },
                            getMainClassList = { viewModel.getMainClassList() },
                            getSubClassList = { viewModel.getSubClassList() },
                            getMainClassIndex = { viewModel.getMainClassIndex() },
                            getSubClassIndex = { viewModel.getSubClassIndex() },
                        )
                    }
                }
            }
        }
    }

    private fun setupListenersAndAdaptersWithoutDropdown() {
        with(binding) {
            searchFilterBookmarkCheckmark.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setIsBookmark(isChecked)
            }
            val searchTypes: List<SearchType> = SearchType.entries
            val adapter = ArrayAdapter(
                root.context,
                android.R.layout.simple_spinner_item,
                searchTypes.map{it.searchText}
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            searchFilterSearchByDropdown.setAdapter(adapter)
            searchFilterSearchByDropdown.setOnItemClickListener { _, _, position, _ ->
                viewModel.setSearchType(searchTypes[position])
            }
            searchFilterMoreFilterButton.visibility = GONE
            /*searchFilterMoreFilterButton.setOnClickListener {
                TODO("Add if more filters are needed")
            }*/
        }
    }
}

interface SearchFilterViewModelOwner {
    val searchFilterViewModelFactory: ViewModelProvider.Factory
}