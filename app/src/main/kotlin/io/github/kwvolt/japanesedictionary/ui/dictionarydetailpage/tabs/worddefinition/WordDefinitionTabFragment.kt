package io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.tabs.worddefinition

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DwpDefinitionTabBinding
import io.github.kwvolt.japanesedictionary.databinding.DwpNotesTextviewBinding
import io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage.DictionaryDetailPageViewModel
import io.github.kwvolt.japanesedictionary.presentation.mainactivity.LoadingViewModel
import io.github.kwvolt.japanesedictionary.ui.ConfirmDialogFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.DictionaryDetailPageViewFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.UpsertNoteDialogFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.UpsertNoteDialogFragment.Companion.ARG_RESULT_NOTE_CONTAINER_ID
import io.github.kwvolt.japanesedictionary.ui.model.DisplayScreenState
import io.github.kwvolt.japanesedictionary.util.PopupMenuUtil
import io.github.kwvolt.japanesedictionary.util.ViewUtil
import io.github.kwvolt.japanesedictionary.util.viewBinding
import kotlinx.coroutines.launch


class WordDefinitionTabFragment: Fragment(R.layout.dwp_definition_tab) {
    private val binding: DwpDefinitionTabBinding by viewBinding(DwpDefinitionTabBinding::bind)
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

        val renderer = WordDefinitionTabRenderer(
            false,
            binding,
            this,
            isToggleSectionExpanded = {noteContainerId ->
                viewModel.isNotesContainerExpanded(noteContainerId)
            },
            onToggleSectionExpanded = { noteContainerId: Int ->
                viewModel.toggleNoteContainerExpanded(noteContainerId)
            },
            onAddNote = { button: Button, noteContainerId: Int ->
                addNewNoteButtonListener(button, noteContainerId)
            },
            assignPopupMenu = { notesBinding: DwpNotesTextviewBinding, noteId: Long, noteContainerId: Int ->
                assignNotePopupMenu(notesBinding, noteId, noteContainerId)
            }
        )

        // retrieve wordFormData
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loadEntry { renderer.formatText(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { currentState: DisplayScreenState ->
                    currentState.screenStateUnknownError?.let {
                        loadingViewModel.showWarning(it)
                        return@collect
                    }
                    when {
                        currentState.isLoading -> {
                            loadingViewModel.showLoading()
                        }

                        else -> {
                            currentState.entry?.let { renderer.renderEntry(it) }
                            loadingViewModel.hideLoading()
                        }
                    }
                }
            }
        }
        refreshNoteUpdateFragmentListener(renderer)
        refreshNoteDeleteFragmentListener(renderer)
    }

    private fun refreshNoteUpdateFragmentListener(renderer: WordDefinitionTabRenderer){
        parentFragmentManager.setFragmentResultListener(
            UpsertNoteDialogFragment.RESULT_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            viewLifecycleOwner.lifecycleScope.launch {
                val confirm: Boolean = bundle.getBoolean(UpsertNoteDialogFragment.RESULT_CONFIRMED, false)
                val noteContainerId: Int = bundle.getInt(ARG_RESULT_NOTE_CONTAINER_ID, GENERAL_NOTE_CONTAINER_ID)
                if(confirm) {
                    viewModel.setToggleNoteContainerExpanded(noteContainerId, true)
                    viewModel.refreshNoteInputs(noteContainerId)
                    renderer.updateNotes(noteContainerId, viewModel.currentFormData)
                }
            }
        }
    }

    private fun refreshNoteDeleteFragmentListener(renderer: WordDefinitionTabRenderer){
        // called after confirming a note deletion
        parentFragmentManager.setFragmentResultListener(
            WORD_DEFINITION_DELETE_DIALOG_CONFIRMATION_KEY,
            viewLifecycleOwner
        ){
                _, bundle: Bundle ->
            val noteId: Long = bundle.getLong(ARG_NOTE_ID)
            val noteContainerId: Int = bundle.getInt(ARG_NOTE_CONTAINER_ID)
            val confirmed = bundle.getBoolean(ConfirmDialogFragment.RESULT_DISCARD_CONFIRMED)
            if (confirmed) {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.deleteNote(noteId, noteContainerId != GENERAL_NOTE_CONTAINER_ID)
                    viewModel.refreshNoteInputs(noteContainerId)
                    renderer.updateNotes(noteContainerId, viewModel.currentFormData)
                }
            }
        }
    }


    // new note button
    private fun addNewNoteButtonListener(addNoteButton: Button, noteContainerId: Int){
        addNoteButton.setOnClickListener {
            UpsertNoteDialogFragment.show(
                parentFragmentManager,null, noteContainerId, false, WORD_DEFINITION_UPDATE_NOTE_DIALOG_TAG
            )
        }
    }

    // assigned to each note textview right icon as a button
    private fun assignNotePopupMenu(dwpNotesTextviewBinding: DwpNotesTextviewBinding, noteId: Long, noteContainerId: Int) {
        dwpNotesTextviewBinding.overflowIcon.setOnClickListener { view ->
            PopupMenuUtil.setupPopupMenu(R.menu.dwp_note_popup, requireContext(), view) { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.dwp_popup_menu_update -> {
                        UpsertNoteDialogFragment.show(
                            parentFragmentManager, noteId, noteContainerId = noteContainerId, true, WORD_DEFINITION_UPDATE_NOTE_DIALOG_TAG
                        )
                        true
                    }

                    R.id.dwp_popup_menu_delete -> {
                        val bundle = Bundle().apply {
                            putLong(ARG_NOTE_ID, noteId)
                            putInt(ARG_NOTE_CONTAINER_ID, noteContainerId)
                        }
                        ConfirmDialogFragment.show(
                            parentFragmentManager,
                            R.string.warning_delete_title,
                            R.string.warning_delete_message,
                            WORD_DEFINITION_DELETE_DIALOG_CONFIRMATION_KEY,
                            bundle
                        )
                        true
                    }

                    else -> false
                }
            }
        }
    }

    companion object {
        const val WORD_DEFINITION_UPDATE_NOTE_DIALOG_TAG: String = "WORD_DEFINITION_UPDATE_NOTE_DIALOG_TAG"
        private const val ARG_NOTE_ID: String = "WORD_DEFINITION_ARG_NOTE_ID"
        private const val ARG_NOTE_CONTAINER_ID: String = "WORD_DEFINITION_ARG_NOTE_CONTAINER_ID"
        private const val WORD_DEFINITION_DELETE_DIALOG_CONFIRMATION_KEY = "WORD_DEFINITION_DELETE_DIALOG_CONFIRMATION_KEY"
        internal const val GENERAL_NOTE_CONTAINER_ID: Int = -1
    }
}