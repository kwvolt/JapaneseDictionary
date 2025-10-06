package io.github.kwvolt.japanesedictionary

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import io.github.kwvolt.japanesedictionary.databinding.ActivityMainBinding
import io.github.kwvolt.japanesedictionary.domain.model.SearchFilter
import io.github.kwvolt.japanesedictionary.domain.model.SearchType
import io.github.kwvolt.japanesedictionary.presentation.mainactivity.LoadingViewModel
import io.github.kwvolt.japanesedictionary.ui.ErrorDialogFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarylookup.DictionaryLookupFragmentDirections
import io.github.kwvolt.japanesedictionary.ui.model.ActivityMainScreenState
import io.github.kwvolt.japanesedictionary.ui.upsert.UpsertRecyclerViewFragmentDirections
import io.github.kwvolt.japanesedictionary.util.ParcelableSearchFilterWrapper
import kotlinx.coroutines.launch
import androidx.core.view.get
import androidx.core.view.size
import io.github.kwvolt.japanesedictionary.ui.dictionarylookup.DictionaryLookupFragmentArgs


class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding ?: throw IllegalStateException(getString(R.string.binding_null_error))

    private val loadingViewModel: LoadingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loadingViewModel.uiState.collect { state: ActivityMainScreenState ->
                    state.screenStateUnknownError?.let { error ->
                        showLoading()
                        ErrorDialogFragment.show(supportFragmentManager, error.throwable)
                        return@collect
                    }
                    when {
                        state.isLoading -> showLoading()
                        else -> {
                            hideLoading()
                        }
                    }
                }
            }
        }

        binding.navHostFragment.doOnLayout {
            val navController: NavController = binding.navHostFragment.findNavController()

            val navOptions = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .build()

            navController.addOnDestinationChangedListener { _, destination, arguments ->
                val visibleDestinations = setOf(
                    R.id.dictionaryLookupFragment,
                    R.id.dictionaryDetailPageViewFragment
                )
                binding.bottomNavigation.visibility = if (destination.id in visibleDestinations) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

                val menu = binding.bottomNavigation.menu

                val menuItemId = when (destination.id) {
                    R.id.dictionaryLookupFragment -> {
                        // Check arguments to differentiate home vs bookmarks
                        arguments?.let {
                            val searchFilterWrapper: ParcelableSearchFilterWrapper? = DictionaryLookupFragmentArgs.fromBundle(arguments).searchFilterWrapper
                            if (searchFilterWrapper?.searchFilter == SearchFilter(searchType = SearchType.ALL, isBookmark = true)) {
                                R.id.nav_bookmarks
                            }
                            else {
                                R.id.nav_home
                            }
                        } ?: R.id.nav_home
                    }
                    R.id.upsertRecyclerViewFragment -> R.id.nav_insert
                    else -> R.id.nav_home
                }
                menu.findItem(menuItemId).isChecked = true

            }

            binding.bottomNavigation.setOnItemSelectedListener { item ->
                if (navController.currentDestination?.id != item.itemId) {
                    when (item.itemId) {
                        R.id.nav_home -> {
                            val action: NavDirections = DictionaryLookupFragmentDirections.actionGlobalLookup(null,
                                ParcelableSearchFilterWrapper(SearchFilter(SearchType.ALL, false)))
                            navController.navigate(action, navOptions)
                            true
                        }

                        R.id.nav_bookmarks -> {
                            val action: NavDirections = DictionaryLookupFragmentDirections.actionGlobalLookup(
                                null,
                                ParcelableSearchFilterWrapper(SearchFilter(searchType = SearchType.ALL, isBookmark = true)))
                            navController.navigate(action, navOptions)
                            true
                        }

                        R.id.nav_insert -> {
                            val action: NavDirections = UpsertRecyclerViewFragmentDirections.actionGlobalUpsert()
                            navController.navigate(action, navOptions)
                            true
                        }
                        else -> false
                    }
                }else false
            }
        }
    }

    private fun showLoading(){
        binding.loadingBackground.apply {
            animate().alpha(1f).setDuration(200).start()
            visibility = View.VISIBLE
        }
        binding.loadingProgressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingBackground.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                binding.loadingBackground.visibility = View.GONE
                binding.loadingBackground.alpha = 1f // Reset alpha
            }
            .start()

        binding.loadingProgressBar.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
