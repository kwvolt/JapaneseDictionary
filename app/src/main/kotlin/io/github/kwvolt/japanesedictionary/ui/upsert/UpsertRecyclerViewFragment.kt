package io.github.kwvolt.japanesedictionary.ui.upsert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnLayout
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
import androidx.navigation.ui.navigateUp
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.kwvolt.japanesedictionary.DictionaryDatabaseProvider
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DwpLayoutBinding
import io.github.kwvolt.japanesedictionary.databinding.DwpLayoutBinding.bind
import io.github.kwvolt.japanesedictionary.databinding.UpsertLayoutBinding
import io.github.kwvolt.japanesedictionary.presentation.mainactivity.LoadingViewModel
import io.github.kwvolt.japanesedictionary.presentation.upsert.UpsertViewModel
import io.github.kwvolt.japanesedictionary.presentation.upsert.UpsertViewModelFactory
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.UpsertAdapter
import io.github.kwvolt.japanesedictionary.ui.ConfirmDialogFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.DictionaryDetailPagePreviewDialogFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.DictionaryDetailPageViewFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.DictionaryDetailPageViewFragmentDirections
import io.github.kwvolt.japanesedictionary.ui.dictionarylookup.DictionaryLookupFragmentArgs
import io.github.kwvolt.japanesedictionary.ui.model.FormScreenState
import io.github.kwvolt.japanesedictionary.util.RetrieveDictionaryIdAsNullable
import io.github.kwvolt.japanesedictionary.util.ViewUtil
import io.github.kwvolt.japanesedictionary.util.viewBinding
import kotlinx.coroutines.launch
import kotlin.getValue


class UpsertRecyclerViewFragment: Fragment(R.layout.upsert_layout) {
    private val binding: UpsertLayoutBinding by viewBinding(UpsertLayoutBinding::bind)
    private val args: UpsertRecyclerViewFragmentArgs by navArgs()

    private var undoMenuItem: MenuItem? = null
    private var redoMenuItem: MenuItem? = null

    private val loadingViewModel: LoadingViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewUtil.setCardBackgroundAlpha(binding.upsertRecyclerCardView, ViewUtil.ALPHA_75_PERCENT)

        val viewModelFactory: ViewModelProvider.Factory by lazy {
            UpsertViewModelFactory(DictionaryDatabaseProvider(requireActivity().application))
        }
        val viewModel: UpsertViewModel by viewModels { viewModelFactory }

        val wordClassListener = InitWordClassCallBack(viewModel)
        val editTextListener = InitEditTextCallBack(viewModel)
        val labelTextListener = InitLabelTextCallBack(viewModel)
        val buttonListener = InitButtonCallBack(viewModel)

        val upsertAdapter = UpsertAdapter(wordClassListener, labelTextListener, editTextListener, buttonListener)
        binding.upsertRecyclerList.layoutManager = LinearLayoutManager(this.context)
        binding.upsertRecyclerList.adapter = upsertAdapter

        val dictionaryId: Long? = RetrieveDictionaryIdAsNullable.getDictionaryId(args.dictionaryId)

        // Launch coroutine to wait for data load
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadWordClassSpinner()
            dictionaryId?.let { viewModel.loadExistingItemForm(it) } ?: viewModel.initializeNewItemForm()
        }

        val toolbar: Toolbar = binding.upsertFormMenu

        // handle back press and unsaved changes confirmation
        toolbar.setNavigationOnClickListener {
            if (upsertAdapter.hasDataBeenUpdated()) {
                ConfirmDialogFragment.showDiscard(parentFragmentManager, DISCARD_CONFIRMATION)
            } else {
                findNavController().navigateUp()
            }
        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (upsertAdapter.hasDataBeenUpdated()) {
                ConfirmDialogFragment.showDiscard(parentFragmentManager, DISCARD_CONFIRMATION)
            } else {
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
        parentFragmentManager.setFragmentResultListener(
            DISCARD_CONFIRMATION,
            viewLifecycleOwner
        ){ _, bundle: Bundle ->
            val confirmed = bundle.getBoolean(ConfirmDialogFragment.RESULT_DISCARD_CONFIRMED)
            if (confirmed) {
                findNavController().navigateUp()
            }
        }

        // Set up the menu
        toolbar.setOnMenuItemClickListener { menuItem ->
            binding.upsertRecyclerList.clearFocus()
            when (menuItem.itemId) {
                R.id.upsert_action_redo -> {
                    viewModel.redo()
                    true
                }
                R.id.upsert_action_undo -> {
                    viewModel.undo()
                    true
                }
                else -> false
            }
        }

        toolbar.doOnLayout {
            toolbar.title = getString(if(dictionaryId != null) R.string.upsert_update_title else R.string.upsert_insert_title)
            undoMenuItem = toolbar.menu.findItem(R.id.upsert_action_undo)
            redoMenuItem = toolbar.menu.findItem(R.id.upsert_action_redo)
            undoMenuItem?.isEnabled = false
            redoMenuItem?.isEnabled = false
        }

        // observe
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { currentState: FormScreenState ->

                    when {
                        currentState.screenStateUnknownError != null -> {
                            loadingViewModel.showLoading()
                            loadingViewModel.showWarning(currentState.screenStateUnknownError!!)
                        }

                        currentState.isLoading -> {
                            loadingViewModel.showLoading()
                        }

                        currentState.confirmed != null -> {
                            loadingViewModel.hideLoading()
                            currentState.confirmed?.let{
                                val action: NavDirections = UpsertRecyclerViewFragmentDirections.actionUpsertToDetail(dictionaryId = dictionaryId ?: -1)
                                findNavController().navigate(action)
                            }
                        }

                        else -> {
                            if (loadingViewModel.isCurrentlyLoading()) {
                                loadingViewModel.hideLoading()
                            }

                            // Update list and menu states
                            upsertAdapter.submitList(currentState.items)
                            undoMenuItem?.let { it.isEnabled = currentState.canUndo}
                            redoMenuItem?.let { it.isEnabled = currentState.canRedo}
                        }
                    }

                }
            }
        }

        // set up preview
        binding.upsertPreviewButton.setOnClickListener {
            binding.upsertRecyclerList.clearFocus()
            viewModel.generatePreview {
                DictionaryDetailPagePreviewDialogFragment.show(parentFragmentManager, it)
            }
        }

        // Set up UI components like buttons or text views
        binding.upsertConfirmButton.setOnClickListener {
            binding.upsertRecyclerList.clearFocus()
            viewModel.upsertValuesIntoDB()
        }
    }

    companion object {
        private const val DISCARD_CONFIRMATION: String = "UPSERT_RECYCLER_VIEW_DISCARD_CONFIRMATION"
    }
}