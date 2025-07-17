package io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.tabs

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.util.TypedValueCompat.dpToPx
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DwpDefinitionTabBinding
import io.github.kwvolt.japanesedictionary.databinding.DwpNotesBinding
import io.github.kwvolt.japanesedictionary.databinding.DwpNotesTextviewBinding
import io.github.kwvolt.japanesedictionary.databinding.DwpSectionsBinding
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage.DictionaryDetailPageViewModel
import io.github.kwvolt.japanesedictionary.presentation.mainactivity.LoadingViewModel
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.DictionaryDetailPageViewFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.UpdateNoteDialogFragment
import io.github.kwvolt.japanesedictionary.ui.model.DisplayScreenState
import kotlinx.coroutines.launch


class WordDefinitionTabFragment(

): Fragment() {

    private var _binding: DwpDefinitionTabBinding? = null
    private val binding: DwpDefinitionTabBinding get() = _binding ?: throw IllegalStateException(getString(R.string.binding_null_error))

    private val loadingViewModel: LoadingViewModel by activityViewModels()


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

        val viewModel: DictionaryDetailPageViewModel by viewModels(ownerProducer = { requireParentFragment() },
            factoryProducer = {
                (requireParentFragment() as DictionaryDetailPageViewFragment).dictionaryDetailPageViewModelFactory  // expose this factory from parent
            }
        )

        // retrieve wordFormData
        viewModel.loadEntry()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { currentState: DisplayScreenState ->

                    when {
                        currentState.isLoading -> {
                            loadingViewModel.showLoading()
                        }
                        else -> {
                            loadingViewModel.hideLoading()
                            // display
                            currentState.entry?.let { displayInformation(it) }
                        }
                    }
                }
            }
        }

        parentFragmentManager.setFragmentResultListener(
            UpdateNoteDialogFragment.RESULT_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val updatedNoteText = bundle.getString(UpdateNoteDialogFragment.RESULT_NOTE_TEXT)
            val noteId = bundle.getLong(UpdateNoteDialogFragment.RESULT_NOTE_ID)

            // Handle updated note here (e.g., save it to database)
            viewModel.upsertNote()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun displayInformation(wordEntryFormData: WordEntryFormData) {
        binding.apply {
            // Clear previous views (important!)
            dwpDefinitionContainerLayout.removeAllViews()
            dwpGeneralNotesLayout.dwpExpandableNotesLayout.removeAllViews()

            // Primary text
            dwpPrimaryText.text = wordEntryFormData.primaryTextInput.inputTextValue

            // Word class
            val main = wordEntryFormData.wordClassInput.chosenMainClass.displayText
            val sub = wordEntryFormData.wordClassInput.chosenSubClass.displayText
            dwpWordClass.text = getString(R.string.dwp_word_class_display, main, sub)

            // Sections
            displaySectionInformation(wordEntryFormData.wordSectionMap.values, dwpDefinitionContainerLayout)

            // General notes
            assignNoteClickListener(dwpGeneralNotesLayout, wordEntryFormData.entryNoteInputMap.values.isNotEmpty())
            generateNotes(wordEntryFormData.entryNoteInputMap.values, dwpGeneralNotesLayout.dwpExpandableNotesLayout)
        }
    }

    private fun displaySectionInformation(sections: Collection<WordSectionFormData>, parent: LinearLayout){
        sections.forEachIndexed { index ,sectionEntry ->
            val sectionBinding = DwpSectionsBinding.inflate(layoutInflater, parent, false)

            sectionBinding. dwpSectionsLayout.layoutParams = sectionLayout(
                if (index == 0) null else SECTION_TOP_MARGIN_DP,
                SECTION_BOTTOM_MARGIN_DP
            )

            val prefix: String = getString(R.string.dwp_ordered_number_prefix, index+1)
            val indent: Int = getIndent(sectionBinding.dwpMeaning, prefix)

            sectionBinding.dwpMeaning.text = setSpan(prefix + sectionEntry.meaningInput.inputTextValue, 0, indent)

            sectionBinding.dwpKana.text =
                setSpan(sectionEntry.kanaInputMap.values.joinToString(getString(R.string.dwp_kana_separator)) { it.inputTextValue }, indent, indent)

            // section notes
            assignNoteClickListener(sectionBinding.dwpSectionNotes, sectionEntry.sectionNoteInputMap.isNotEmpty())
            generateNotes(sectionEntry.sectionNoteInputMap.values,sectionBinding.dwpSectionNotes.dwpExpandableNotesLayout)

            // Add the section to the definition container
            parent.addView(sectionBinding.root)
        }
    }

    private fun generateNotes(notes: Collection<TextItem>, parent: LinearLayout){
        notes.forEachIndexed(
        ){ index, noteItem ->
            val notesBinding = DwpNotesTextviewBinding.inflate(layoutInflater, parent, false)

            val prefix: String = getString(R.string.dwp_ordered_number_prefix, index+1)
            val indent: Int = getIndent(notesBinding.dwpNoteText, prefix)
            notesBinding.dwpNoteText.text = setSpan(prefix + noteItem.inputTextValue, 0, indent)

            assignNotePopupMenu(notesBinding, noteItem)

            parent.addView(notesBinding.root)
        }
    }

    private fun assignNotePopupMenu(dwpNotesTextviewBinding: DwpNotesTextviewBinding, noteItem: TextItem) {
        dwpNotesTextviewBinding.overflowIcon.setOnClickListener { view ->
            val popup = PopupMenu(requireContext(), view)
            popup.menuInflater.inflate(R.menu.dwp_note_popup, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.dwp_menu_update -> {
                        val dialog = UpdateNoteDialogFragment.newInstance(noteItem.inputTextValue, noteItem.itemProperties.getId())
                        dialog.show(parentFragmentManager, "UpdateNoteDialog")
                        true
                    }

                    R.id.dwp_menu_delete -> {
                        // Handle Delete action
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

    private fun getIndent(textView: TextView, prefix: String): Int{
        val indent = textView.paint.measureText(prefix).toInt()
        return indent

    }

    private fun setSpan(content: String, firstIndent: Int, restIndent: Int): SpannableString{
        val spannable = SpannableString(content)
        spannable.setSpan(
            LeadingMarginSpan.Standard(firstIndent, restIndent),
            0,
            spannable.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    private fun assignNoteClickListener(dwpNotesBinding: DwpNotesBinding, hasNotes: Boolean) {
        if (hasNotes) {
            dwpNotesBinding.dwpNotesHeader.setOnClickListener {
                val isVisible = dwpNotesBinding.dwpExpandableNotesLayout.isVisible
                val drawableRes = if (isVisible) R.drawable.arrow_drop_down else R.drawable.arrow_drop_up
                val drawable = context?.let { ContextCompat.getDrawable(it, drawableRes) }

                dwpNotesBinding.dwpNotesHeader.contentDescription = getString(
                    if (isVisible) R.string.dwp_expand_notes_content_description else R.string.dwp_collapse_notes_content_description
                )

                dwpNotesBinding.dwpNotesHeader.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null, null, drawable, null
                )

                dwpNotesBinding.dwpExpandableNotesLayout.visibility = if (isVisible) GONE else VISIBLE
            }

            // Set initial arrow
            val initialDrawable = context?.let { ContextCompat.getDrawable(it, R.drawable.arrow_drop_down) }
            dwpNotesBinding.dwpNotesHeader.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, initialDrawable, null)

        } else {
            // Remove arrow if there are no notes
            dwpNotesBinding.dwpNotesHeader.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
            dwpNotesBinding.dwpNotesHeader.setOnClickListener(null)
        }
    }

    companion object {
        private const val SECTION_TOP_MARGIN_DP = 20f
        private const val SECTION_BOTTOM_MARGIN_DP = 20f
    }
}