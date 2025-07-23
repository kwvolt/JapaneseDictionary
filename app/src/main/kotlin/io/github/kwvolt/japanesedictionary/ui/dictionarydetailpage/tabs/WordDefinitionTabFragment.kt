package io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.util.TypedValueCompat.dpToPx
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DwpDefinitionTabBinding
import io.github.kwvolt.japanesedictionary.databinding.DwpNotesBinding
import io.github.kwvolt.japanesedictionary.databinding.DwpNotesTextviewBinding
import io.github.kwvolt.japanesedictionary.databinding.DwpSectionsBinding
import io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage.DictionaryDetailPageViewModel
import io.github.kwvolt.japanesedictionary.presentation.mainactivity.LoadingViewModel
import io.github.kwvolt.japanesedictionary.ui.ConfirmDialogFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.DictionaryDetailPageViewFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.UpdateNoteDialogFragment
import io.github.kwvolt.japanesedictionary.ui.model.DisplayEntryUIModel
import io.github.kwvolt.japanesedictionary.ui.model.DisplayNote
import io.github.kwvolt.japanesedictionary.ui.model.DisplayScreenState
import io.github.kwvolt.japanesedictionary.ui.model.DisplaySection
import io.github.kwvolt.japanesedictionary.util.ListSpanUtil
import kotlinx.coroutines.launch


class WordDefinitionTabFragment: Fragment() {
    private var _binding: DwpDefinitionTabBinding? = null
    private val binding: DwpDefinitionTabBinding get() = _binding ?: throw IllegalStateException(getString(R.string.binding_null_error))

    private val loadingViewModel: LoadingViewModel by activityViewModels()

    // shared viewmodel
    private val parent: Fragment by lazy { requireParentFragment() }
    private val factory: ViewModelProvider.Factory = (parent as? DictionaryDetailPageViewFragment)?.dictionaryDetailPageViewModelFactory
        ?: throw IllegalStateException("Parent must be DictionaryDetailPageViewFragment")
    private val viewModel: DictionaryDetailPageViewModel by viewModels(
        ownerProducer = { parent },
        factoryProducer = { factory }
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DwpDefinitionTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // retrieve wordFormData
        viewModel.loadEntry()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { currentState: DisplayScreenState ->
                    when {
                        currentState.isLoading -> {
                            loadingViewModel.showLoading()
                        }

                        currentState.screenStateUnknownError != null -> {
                            loadingViewModel.showWarning(currentState.screenStateUnknownError!!)
                        }
                        else -> {
                            currentState.entry?.let { renderEntry(it) }
                            loadingViewModel.hideLoading()
                        }
                    }
                }
            }
        }

        // called after update a note value
        parentFragmentManager.setFragmentResultListener(
            UpdateNoteDialogFragment.RESULT_KEY,
            viewLifecycleOwner
        ) { _, _ ->
            viewModel.loadEntry()
        }

        // called after confirming a note deletion
        parentFragmentManager.setFragmentResultListener(
            DELETE_DIALOG_CONFIRMATION_KEY,
            viewLifecycleOwner
        ){
            _, bundle: Bundle ->
            val noteId: Long = bundle.getLong(ARG_NOTE_ID)
            val isSection: Boolean = bundle.getBoolean(ARG_IS_SECTION)
            val confirmed = bundle.getBoolean(ConfirmDialogFragment.RESULT_DISCARD_CONFIRMED)
            if (confirmed) {
                viewModel.deleteNote(noteId, isSection)
                viewModel.loadEntry()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun renderEntry(displayEntryUIModel: DisplayEntryUIModel) {
        binding.apply {
            // Clear previous views
            dwpDefinitionContainerLayout.removeAllViews()
            dwpGeneralNotesLayout.dwpExpandableNotesLayout.removeAllViews()

            // Primary text
            dwpPrimaryText.text = displayEntryUIModel.primaryText

            // Word class
            dwpWordClass.text = getString(R.string.dwp_word_class_display, displayEntryUIModel.mainWordClass, displayEntryUIModel.subWordClass)

            // Sections
            renderSection(displayEntryUIModel.sections, dwpDefinitionContainerLayout)

            // General notes
            assignNoteClickListener(dwpGeneralNotesLayout, displayEntryUIModel.generalNotes.isNotEmpty())
            generateNotes(displayEntryUIModel.generalNotes, dwpGeneralNotesLayout.dwpExpandableNotesLayout)
            addNewNoteButtonListener(dwpGeneralNotesLayout.dwpAddNoteButton)
        }
    }

    private fun renderSection(sections: List<DisplaySection>, parent: LinearLayout){
        sections.forEachIndexed { index: Int , displaySection: DisplaySection ->
            val sectionBinding = DwpSectionsBinding.inflate(layoutInflater, parent, false)

            sectionBinding.dwpSectionsLayout.layoutParams = sectionLayout(
                if (index == 0) null else SECTION_TOP_MARGIN_DP,
                SECTION_BOTTOM_MARGIN_DP
            )

            val prefix: String = getString(R.string.dwp_ordered_number_prefix, index+1)
            val indent: Int = ListSpanUtil.calculateIndent(sectionBinding.dwpMeaning, prefix)
            sectionBinding.dwpMeaning.text = ListSpanUtil.applyLeadingMargin(prefix + displaySection.meaningText, 0, indent)
            sectionBinding.dwpKana.text = ListSpanUtil.applyLeadingMargin(displaySection.kanaList.joinToString(getString(R.string.dwp_kana_separator)), indent, indent)

            // section notes
            assignNoteClickListener(sectionBinding.dwpSectionNotes, displaySection.notes.isNotEmpty())
            generateNotes(displaySection.notes, sectionBinding.dwpSectionNotes.dwpExpandableNotesLayout, displaySection.sectionId)
            addNewNoteButtonListener(sectionBinding.dwpSectionNotes.dwpAddNoteButton, displaySection.sectionId)

            // Add the section to the definition container
            parent.addView(sectionBinding.root)
        }
    }

    private fun generateNotes(notes: List<DisplayNote>, parent: LinearLayout, sectionId: Int? = null){
        notes.forEachIndexed{ index, displayNote ->
            val notesBinding = DwpNotesTextviewBinding.inflate(layoutInflater, parent, false)

            val prefix: String = getString(R.string.dwp_ordered_number_prefix, index+1)
            val indent: Int = ListSpanUtil.calculateIndent(notesBinding.dwpNoteText, prefix)
            notesBinding.dwpNoteText.text = ListSpanUtil.applyLeadingMargin(prefix + displayNote.text, 0, indent)

            assignNotePopupMenu(notesBinding, displayNote.id, sectionId)

            parent.addView(notesBinding.root)
        }
    }

    // new note button
    private fun addNewNoteButtonListener(addNoteButton: Button, sectionId: Int? = null){
        addNoteButton.setOnClickListener {
            val dialog = UpdateNoteDialogFragment.newInstance(null, sectionId)
            dialog.show(parentFragmentManager, UPDATE_NOTE_DIALOG_TAG)
        }
    }

    // assigned to each note textview right icon as a button
    private fun assignNotePopupMenu(dwpNotesTextviewBinding: DwpNotesTextviewBinding, noteId: Long, sectionId: Int? = null) {
        dwpNotesTextviewBinding.overflowIcon.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.dwp_note_popup, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.dwp_popup_menu_update -> {
                        val dialog = UpdateNoteDialogFragment.newInstance(noteId, sectionId = sectionId)
                        dialog.show(parentFragmentManager, UPDATE_NOTE_DIALOG_TAG)
                        true
                    }

                    R.id.dwp_popup_menu_delete -> {
                        val bundle = Bundle()
                        bundle.putLong(ARG_NOTE_ID, noteId)
                        bundle.putBoolean(ARG_IS_SECTION, sectionId != null)
                        ConfirmDialogFragment.show(
                            parentFragmentManager,
                            R.string.warning_delete_title,
                            R.string.warning_delete_message,
                            DELETE_DIALOG_CONFIRMATION_KEY,
                            bundle
                        )
                        true
                    }

                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun sectionLayout(newTopMargin: Float?, newBottomMargin: Float?): LinearLayout.LayoutParams {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.apply {
            newTopMargin?.let { topMargin = dpToPx(it, resources.displayMetrics).toInt() }
            newBottomMargin?.let { bottomMargin = dpToPx(it, resources.displayMetrics).toInt() }
        }
        return params
    }

    // handle expanding and collapsing the note layouts along with other visual cue along with it
    private fun assignNoteClickListener(
        dwpNotesBinding: DwpNotesBinding,
        hasNotes: Boolean,
        sectionId: Int? = null
    ) {
        if (hasNotes) {
            val sectionKey = sectionId ?: -1 // Use -1 for general notes

            // Set initial state based on ViewModel
            val isInitiallyVisible = viewModel.isSectionExpanded(sectionKey)
            dwpNotesBinding.dwpExpandableNotesLayout.visibility = if (isInitiallyVisible) VISIBLE else GONE

            val initialDrawable = ContextCompat.getDrawable(
                requireContext(),
                if (isInitiallyVisible) R.drawable.arrow_drop_up else R.drawable.arrow_drop_down
            )
            dwpNotesBinding.dwpNotesHeader.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, initialDrawable, null)

            dwpNotesBinding.dwpNotesHeader.setOnClickListener {
                val isVisible = dwpNotesBinding.dwpExpandableNotesLayout.isVisible
                val newVisibility = if (isVisible) GONE else VISIBLE
                val newDrawable = ContextCompat.getDrawable(
                    requireContext(),
                    if (isVisible) R.drawable.arrow_drop_down else R.drawable.arrow_drop_up
                )

                dwpNotesBinding.dwpExpandableNotesLayout.visibility = newVisibility
                dwpNotesBinding.dwpNotesHeader.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, newDrawable, null)

                // Update state in ViewModel
                viewModel.toggleSectionExpanded(sectionKey)
            }
        } else {
            dwpNotesBinding.dwpNotesHeader.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
            dwpNotesBinding.dwpNotesHeader.setOnClickListener(null)
        }
    }

    companion object {
        private const val SECTION_TOP_MARGIN_DP: Float = 20f
        private const val SECTION_BOTTOM_MARGIN_DP: Float = 20f

        const val UPDATE_NOTE_DIALOG_TAG: String = "UpdateNoteDialog"

        private const val ARG_NOTE_ID: String = "noteId"
        private const val ARG_IS_SECTION: String = "isSection"

        private const val DELETE_DIALOG_CONFIRMATION_KEY = "WordDefinitionTabFragmentUpdateNoteConfirmationKey"
    }
}