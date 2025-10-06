package io.github.kwvolt.japanesedictionary.util

import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.kwvolt.japanesedictionary.presentation.dictionarylookup.DictionaryLookupRecyclerViewModel
import io.github.kwvolt.japanesedictionary.presentation.mainactivity.LoadingViewModel
import io.github.kwvolt.japanesedictionary.presentation.search.SearchFilterViewModel
import io.github.kwvolt.japanesedictionary.ui.model.SearchFilterScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object SearchBarUtil {
    fun launch(
        viewLifecycleOwner: LifecycleOwner,
        searchFilterViewModel: SearchFilterViewModel,
        loadingViewModel: LoadingViewModel
    ){
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                searchFilterViewModel.uiState.collect { currentState: SearchFilterScreenState ->
                    when {
                        currentState.screenStateUnknownError != null -> {
                            currentState.screenStateUnknownError?.let {
                                loadingViewModel.showWarning(it)
                            }
                        }
                        currentState.isLoading -> {
                            loadingViewModel.showLoading()
                        }
                        else -> {
                            loadingViewModel.hideLoading()
                        }
                    }
                }
            }
        }
    }

    fun attachListener(
        viewLifecycleOwner: LifecycleOwner,
        searchView: SearchView,
        block: (String)->Unit
    ) {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            var searchJob: Job? = null
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query.isNullOrBlank()) return false
                searchJob?.cancel()
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    block(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = true
        })
    }
}

fun Fragment.initalizeSearchBar(
    searchView: SearchView,
    searchFilterViewModel: SearchFilterViewModel,
    loadingViewModel: LoadingViewModel,
    afterSubmit: (String) -> Unit
){
    searchView.isSubmitButtonEnabled = true
    SearchBarUtil.attachListener(viewLifecycleOwner, searchView,afterSubmit)
    SearchBarUtil.launch(viewLifecycleOwner, searchFilterViewModel, loadingViewModel)
}