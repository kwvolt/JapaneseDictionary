package io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage

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
import com.google.android.material.appbar.MaterialToolbar
import io.github.kwvolt.japanesedictionary.DictionaryDatabaseProvider
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DwpLayoutBinding
import io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage.DictionaryDetailPageViewModel
import io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage.DictionaryDetailPageViewModelFactory
import io.github.kwvolt.japanesedictionary.presentation.mainactivity.LoadingViewModel
import io.github.kwvolt.japanesedictionary.presentation.search.SearchFilterViewModel
import io.github.kwvolt.japanesedictionary.presentation.search.SearchFilterViewModelFactory
import io.github.kwvolt.japanesedictionary.ui.ConfirmDialogFragment
import io.github.kwvolt.japanesedictionary.ui.searchfilter.SearchFilterViewModelOwner
import io.github.kwvolt.japanesedictionary.util.ParcelableSearchFilterWrapper
import io.github.kwvolt.japanesedictionary.util.icon.MenuItemIcon
import io.github.kwvolt.japanesedictionary.util.ViewUtil
import io.github.kwvolt.japanesedictionary.util.icon.toggleBookmark
import io.github.kwvolt.japanesedictionary.util.icon.toggleFilter
import io.github.kwvolt.japanesedictionary.util.initalizeSearchBar
import io.github.kwvolt.japanesedictionary.util.viewBinding
import kotlinx.coroutines.launch
import kotlin.getValue

class DictionaryDetailPageViewFragment: Fragment(R.layout.dwp_layout), SearchFilterViewModelOwner {
    private val binding: DwpLayoutBinding by viewBinding(DwpLayoutBinding::bind)
    private val args: DictionaryDetailPageViewFragmentArgs by navArgs()
    private val loadingViewModel: LoadingViewModel by activityViewModels()

    override val searchFilterViewModelFactory: ViewModelProvider.Factory by lazy {
        SearchFilterViewModelFactory(DictionaryDatabaseProvider(requireActivity().application))
    }
    private val searchFilterViewModel: SearchFilterViewModel by viewModels {searchFilterViewModelFactory}
    private val dictionaryId: Long by lazy { args.dictionaryId}

    val dictionaryDetailPageViewModelFactory: ViewModelProvider.Factory by lazy {
        DictionaryDetailPageViewModelFactory(DictionaryDatabaseProvider(requireActivity().application), dictionaryId)
    }

    val viewModel: DictionaryDetailPageViewModel by viewModels { dictionaryDetailPageViewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtil.setNavigationTransition(view)

        val dictionaryDetailPageRenderer = DictionaryDetailPageRenderer(false)
        dictionaryDetailPageRenderer.applyBackgroundAlpha(binding.dwpCardView)

        val toolbar: MaterialToolbar = binding.dwpFormMenu
        toolbar.setNavigationOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        toolbar.post {
            val menu: Menu = binding.dwpFormMenu.menu
            menu.setGroupDividerEnabled(true)
            setToggleSearchFilter(toolbar)
            setBookmark(toolbar)
            setSearchView(menu)
            setMenuListener(toolbar)
        }
        // delete Entry
        parentFragmentManager.setFragmentResultListener(
            DETAIL_DELETE_DIALOG_CONFIRMATION_KEY, viewLifecycleOwner) { _, bundle: Bundle ->
            val confirmed = bundle.getBoolean(ConfirmDialogFragment.RESULT_DISCARD_CONFIRMED)
            if (confirmed) {
                viewModel.deleteEntry()
                if (!findNavController().navigateUp()) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }

        }
        dictionaryDetailPageRenderer.generateViewPagerAdapter(binding.dwpViewPager, binding.dwpTabLayout, this)
    }

    private fun setToggleSearchFilter(toolbar: MaterialToolbar){
        val filterToggle: MenuItem = toolbar.menu.findItem(R.id.dwp_filter_search)
        val isExpanded = searchFilterViewModel.isExpanded
        binding.searchFilterContainer.visibility = if (isExpanded) VISIBLE else GONE
        MenuItemIcon(filterToggle).toggleFilter(requireContext(),  isExpanded)
    }

    private fun setBookmark(toolbar: MaterialToolbar){
        val bookmarkMenuItem: MenuItem? = toolbar.menu.findItem(R.id.dwp_bookmark_switch)
        bookmarkMenuItem?.let{
            MenuItemIcon(it).toggleBookmark(requireContext(), viewModel.getIsBookmarked())
        }
    }

    private fun setSearchView(menu: Menu){
        val searchView: SearchView = menu.findItem(R.id.dwp_action_search).actionView as SearchView
        initalizeSearchBar(
            searchView,
            searchFilterViewModel,
            loadingViewModel
        ) { query: String ->
            val action: NavDirections =
                DictionaryDetailPageViewFragmentDirections.actionDetailToLookup(
                    query,
                    ParcelableSearchFilterWrapper(searchFilterViewModel.searchFilter)
                )
            findNavController().navigate(action)
        }
    }

    private fun setMenuListener(toolbar: MaterialToolbar){
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.dwp_filter_search -> {
                    val isActive: Boolean = searchFilterViewModel.toggleIsExpanded()
                    MenuItemIcon(menuItem).toggleFilter(requireContext(), isActive)
                    binding.searchFilterContainer.visibility =
                        if (isActive) VISIBLE else GONE
                    true
                }

                R.id.dwp_bookmark_switch -> {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val isBookmark = viewModel.toggleBookmark()
                        MenuItemIcon(menuItem).toggleBookmark(
                            requireContext(),
                            isBookmark
                        )
                    }
                    true
                }

                R.id.dwp_menu_update -> {
                    val action: NavDirections = DictionaryDetailPageViewFragmentDirections.actionDetailToUpsert(dictionaryId = dictionaryId)
                    findNavController().navigate(action)
                    true
                }

                R.id.dwp_menu_delete -> {
                    ConfirmDialogFragment.show(
                        parentFragmentManager,
                        R.string.warning_delete_title_entry,
                        R.string.warning_delete_message_entry,
                        DETAIL_DELETE_DIALOG_CONFIRMATION_KEY
                    )
                    true
                }

                else -> false
            }
        }
    }

    companion object {
        private const val DETAIL_DELETE_DIALOG_CONFIRMATION_KEY = "DETAIL_DELETE_DIALOG_CONFIRMATION_KEY"
    }
}