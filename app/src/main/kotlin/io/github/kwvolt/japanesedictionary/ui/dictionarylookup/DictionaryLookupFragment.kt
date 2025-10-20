package io.github.kwvolt.japanesedictionary.ui.dictionarylookup

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.kwvolt.japanesedictionary.DictionaryDatabaseProvider
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DluLayoutBinding
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.SearchFilter
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.SearchType
import io.github.kwvolt.japanesedictionary.presentation.dictionarylookup.DictionaryLookupRecyclerViewModel
import io.github.kwvolt.japanesedictionary.presentation.dictionarylookup.DictionaryLookupRecyclerViewModelFactory
import io.github.kwvolt.japanesedictionary.presentation.dictionarylookup.adapter.DictionaryLookUpRecyclerAdapter
import io.github.kwvolt.japanesedictionary.presentation.dictionarylookup.adapter.DictionaryLookUpRecyclerCallBack
import io.github.kwvolt.japanesedictionary.presentation.mainactivity.LoadingViewModel
import io.github.kwvolt.japanesedictionary.presentation.search.SearchFilterViewModel
import io.github.kwvolt.japanesedictionary.presentation.search.SearchFilterViewModelFactory
import io.github.kwvolt.japanesedictionary.ui.ConfirmDialogFragment
import io.github.kwvolt.japanesedictionary.ui.model.DictionaryLookupScreenState
import io.github.kwvolt.japanesedictionary.ui.searchfilter.SearchFilterViewModelOwner
import io.github.kwvolt.japanesedictionary.util.PopupMenuUtil
import io.github.kwvolt.japanesedictionary.util.ViewUtil
import io.github.kwvolt.japanesedictionary.util.icon.MenuItemIcon
import io.github.kwvolt.japanesedictionary.util.icon.toggleFilter
import io.github.kwvolt.japanesedictionary.util.initalizeSearchBar
import io.github.kwvolt.japanesedictionary.util.setConfirmDialogListener
import io.github.kwvolt.japanesedictionary.util.viewBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue

class DictionaryLookupFragment: Fragment(R.layout.dlu_layout), SearchFilterViewModelOwner {
    private val binding: DluLayoutBinding by viewBinding(DluLayoutBinding::bind)
    private val args: DictionaryLookupFragmentArgs by navArgs()
    private val loadingViewModel: LoadingViewModel by activityViewModels()

    override val searchFilterViewModelFactory: ViewModelProvider.Factory by lazy {
        SearchFilterViewModelFactory(
            DictionaryDatabaseProvider(requireActivity().application),
            args.searchFilterWrapper?.searchFilter
        )
    }
    private val searchFilterViewModel: SearchFilterViewModel by viewModels {searchFilterViewModelFactory}

    val dictionaryLookupViewModelFactory: ViewModelProvider.Factory by lazy {
        DictionaryLookupRecyclerViewModelFactory(DictionaryDatabaseProvider(requireActivity().application))
    }
    private val viewModel: DictionaryLookupRecyclerViewModel by viewModels {dictionaryLookupViewModelFactory}

    private val dictionaryLookupAdapter = getAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtil.setNavigationTransition(view)
        // initialize recyclerview
        val recyclerView: RecyclerView = binding.dluRecyclerView

        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = dictionaryLookupAdapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                viewModel.paginationState.let { state ->
                    val shouldPaginate = !state.isPaginating && !state.isLastPage
                    val nearEnd = (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - PAGE_THRESHOLD
                    if (shouldPaginate && firstVisibleItemPosition >= 0 && nearEnd) {
                        paginate(dictionaryLookupAdapter)
                    }
                }
            }
        })


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { currentState: DictionaryLookupScreenState ->
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
                            dictionaryLookupAdapter.updateItems(currentState.items, viewModel.paginationState.isPaginating)
                            loadingViewModel.hideLoading()
                        }
                    }
                }
            }
        }

        val argSearchFilter: SearchFilter? = args.searchFilterWrapper?.searchFilter
        val searchFilter: SearchFilter = argSearchFilter ?: SearchFilter(SearchType.ALL, false)

        // initialize list
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.runSearch(args.searchTerm, searchFilter, PAGE_SIZE)
        }


        // search bar initialize
        binding.dluFormMenu.post{
            val menu: Menu = binding.dluFormMenu.menu
            val searchView: SearchView = menu.findItem(R.id.dlu_action_search).actionView as SearchView
            val filterToggle: MenuItem = menu.findItem(R.id.dlu_filter_search)
            val isExpanded = searchFilterViewModel.isExpanded
            binding.searchFilterContainer.visibility = if (isExpanded) VISIBLE else GONE
            MenuItemIcon(filterToggle).toggleFilter(requireContext(),  isExpanded)

            initalizeSearchBar(
                searchView,
                searchFilterViewModel,
                loadingViewModel
            ){ query: String ->
                viewLifecycleOwner.lifecycleScope.launch {
                    dictionaryLookupAdapter.clearItems()
                    viewModel.runSearch(query, searchFilterViewModel.searchFilter, PAGE_SIZE)
                }
            }
            binding.dluFormMenu.setOnMenuItemClickListener { menuItem ->
                when(menuItem.itemId){
                    R.id.dlu_filter_search -> {
                        val isActive: Boolean = searchFilterViewModel.toggleIsExpanded()
                        MenuItemIcon(menuItem).toggleFilter(requireContext(),  isActive)
                        binding.searchFilterContainer.visibility = if(isActive) VISIBLE else GONE
                        true
                    }
                    else -> false
                }
            }

        }

        // Deletes dictionary entry
        setConfirmDialogListener(DICTIONARY_LOOKUP_DELETE_REQUEST_KEY){ bundle: Bundle ->
            val dictionaryId: Long = bundle.getLong(DICTIONARY_LOOKUP_DICTIONARY_ID)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.deleteEntry(dictionaryId, PAGE_SIZE)
            }
        }
    }

    private fun getAdapter(): DictionaryLookUpRecyclerAdapter {
        return DictionaryLookUpRecyclerAdapter(object : DictionaryLookUpRecyclerCallBack{
            override fun toggleBookmark(dictionaryId: Long ) {
                viewModel.toggleBookmark(dictionaryId)
            }
            override fun displayPopupMenu(dictionaryId: Long, view: View){
                showPopupMenu(dictionaryId, view)
            }
            override fun moveToDetailPage(dictionaryId: Long, view: View){
                moveToDetailPage(dictionaryId)
            }

            override fun toggleGlossLayout(dictionaryId: Long) {
                viewModel.toggleGlossLayout(dictionaryId)
            }
        })
    }

    private fun paginate(adapter: DictionaryLookUpRecyclerAdapter) {
        val state = viewModel.paginationState
        if (state.isPaginating || state.isLastPage) return
        adapter.showLoading()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.requestNextPage(PAGE_SIZE)
        }
    }


    private fun showPopupMenu(dictionaryId: Long, view: View) {
        PopupMenuUtil.setupPopupMenu(R.menu.dlu_popup, binding.root.context, view){ menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.dlu_popup_menu_update -> {
                    val action = DictionaryLookupFragmentDirections.actionLookupToUpsert(dictionaryId = dictionaryId)
                    findNavController().navigate(action)
                    true
                }
                R.id.dlu_popup_menu_delete -> {
                    ConfirmDialogFragment.show(
                        parentFragmentManager,
                        R.string.warning_delete_title_entry,
                        R.string.warning_delete_message_entry,
                        DICTIONARY_LOOKUP_DELETE_REQUEST_KEY,
                        Bundle().apply{
                            putLong(DICTIONARY_LOOKUP_DICTIONARY_ID, dictionaryId)
                        })
                    true
                }
                else -> false
            }
        }
    }

    private fun moveToDetailPage(dictionaryId: Long){
        val action: NavDirections = DictionaryLookupFragmentDirections.actionLookupToDetail(dictionaryId = dictionaryId)
        findNavController().navigate(action)
    }

    companion object {
        private const val DICTIONARY_LOOKUP_DICTIONARY_ID : String = "DICTIONARY_LOOKUP_DICTIONARY_ID"
        private const val DICTIONARY_LOOKUP_DELETE_REQUEST_KEY : String = "DICTIONARY_LOOKUP_DELETE_REQUEST_KEY"

        private const val PAGE_SIZE: Int = 15
        private const val PAGE_THRESHOLD = 5
    }
}