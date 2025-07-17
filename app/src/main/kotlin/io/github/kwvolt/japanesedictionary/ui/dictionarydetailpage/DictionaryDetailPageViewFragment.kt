package io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.kwvolt.japanesedictionary.DatabaseProvider
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DwpLayoutBinding
import io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage.DictionaryDetailPageViewModel
import io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage.DictionaryDetailPageViewModelFactory
import io.github.kwvolt.japanesedictionary.ui.addUpdate.AddUpdateRecyclerViewFragment

class DictionaryDetailPageViewFragment: Fragment() {

    private var _binding: DwpLayoutBinding? = null
    private val binding: DwpLayoutBinding get() = _binding ?: throw IllegalStateException("Binding is null")

    private val dictionaryId: Long? by lazy {
        arguments?.getLong("dictionaryId")  // Use the key you put in the bundle
    }

    val dictionaryDetailPageViewModelFactory: ViewModelProvider.Factory by lazy {
        DictionaryDetailPageViewModelFactory(DatabaseProvider(requireActivity().application), dictionaryId)
    }
    val viewModel: DictionaryDetailPageViewModel by viewModels { dictionaryDetailPageViewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DwpLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val toolbar = binding.dwpToolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        // Set up the menu
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.dwp_menu, menu)

                val menuItem = menu.findItem(R.id.dwp_bookmark_switch)
                val context = requireContext()
                if (viewModel.isBookmarked()) {
                    val icon = ContextCompat.getDrawable(context, R.drawable.star)?.mutate()
                    icon?.setTint(ContextCompat.getColor(context, R.color.bookmarked))
                    menuItem.icon = icon
                } else {
                    menuItem.setIcon(R.drawable.star_border)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.dwp_bookmark_switch -> {
                        val isBookmarked: Boolean = viewModel.toggleBookmark()
                        if (isBookmarked) {
                            val context: Context = requireContext()
                            val icon = ContextCompat.getDrawable(context, R.drawable.star)?.mutate()
                            icon?.setTint(ContextCompat.getColor(context, R.color.bookmarked))  // or use #FFC107
                            menuItem.icon = icon
                        } else {
                            menuItem.setIcon(R.drawable.star_border) // empty star icon
                        }
                        true
                    }

                    R.id.dwp_menu_update -> {
                        TODO()
                        val newFragment = AddUpdateRecyclerViewFragment()
                        val transaction = requireActivity().supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragment_container, newFragment)
                        transaction.addToBackStack("dictionary word page") // Optional: add to back stack to allow "back" navigation
                        transaction.commit()
                        true
                    }
                    R.id.dwp_menu_delete -> {
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val tabLayout: TabLayout = binding.dwpTabLayout
        val viewPager: ViewPager2 = binding.dwpViewPager

        val adapter = DictionaryDetailPageViewPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
        }.attach()
    }
}