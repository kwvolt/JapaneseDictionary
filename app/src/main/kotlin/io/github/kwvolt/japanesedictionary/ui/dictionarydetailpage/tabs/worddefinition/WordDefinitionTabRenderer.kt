package io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.tabs.worddefinition

import android.graphics.drawable.Drawable
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.util.TypedValueCompat.dpToPx
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DwpDefinitionTabBinding
import io.github.kwvolt.japanesedictionary.databinding.DwpNotesBinding
import io.github.kwvolt.japanesedictionary.databinding.DwpNotesTextviewBinding
import io.github.kwvolt.japanesedictionary.databinding.DwpSectionsBinding
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.ui.model.DisplayEntryUIModel
import io.github.kwvolt.japanesedictionary.ui.model.DisplayNote
import io.github.kwvolt.japanesedictionary.ui.model.DisplaySection
import io.github.kwvolt.japanesedictionary.util.ListSpanUtil
import io.github.kwvolt.japanesedictionary.util.DictionaryDisplayUtil

class WordDefinitionTabRenderer(
    private val isPreview: Boolean,
    private val binding: DwpDefinitionTabBinding,
    private val fragment: Fragment,
    private val isToggleSectionExpanded: ((noteContainerId: Int) -> Boolean)? = null,
    private val onToggleSectionExpanded: ((noteContainerKey: Int) -> Unit)? = null,
    private val onAddNote: ((addNoteButton: Button, noteContainerId: Int) -> Unit)? = null,
    private val assignPopupMenu: ((binding: DwpNotesTextviewBinding, noteId: Long, noteContainerId: Int) -> Unit)? = null
) {

    fun renderEntry(displayEntryUIModel: DisplayEntryUIModel) = with(binding) {
        clearViews()
        renderPrimaryText(displayEntryUIModel.primaryText)
        renderWordClass(displayEntryUIModel)
        renderSections(displayEntryUIModel.sections)
        renderGeneralNotes(displayEntryUIModel.generalNotes)
    }

    private fun DwpDefinitionTabBinding.clearViews() {
        dwpDefinitionContainerLayout.removeAllViews()
        dwpGeneralNotesLayout.dwpExpandableNotesLayout.removeAllViews()
    }

    private fun DwpDefinitionTabBinding.renderPrimaryText(primaryText: String) {
        dwpPrimaryText.setVisibilityAndRun(!isPreview || primaryText.isNotEmpty()) {
            dwpPrimaryText.text = primaryText
        }
    }

    private fun DwpDefinitionTabBinding.renderWordClass(model: DisplayEntryUIModel) {
        val text: String? = DictionaryDisplayUtil.displayWordClass(
            model.mainWordClass,
            model.subWordClass,
            fragment.requireContext()
        )
        DictionaryDisplayUtil.displayOrHideWordClass(text, dwpWordClass)
    }

    private fun DwpDefinitionTabBinding.renderSections(sections: List<DisplaySection>) {
        val hasSections: Boolean = sections.isNotEmpty()
        val visibility: Int =  if (hasSections) VISIBLE else GONE
        dwpSectionLabel.visibility = visibility
        dwpDefinitionContainerLayout.visibility = visibility
        if (hasSections) renderSection(sections, dwpDefinitionContainerLayout)
    }

    private fun DwpDefinitionTabBinding.renderGeneralNotes(notes: List<DisplayNote>) {
        val hasNotes: Boolean = notes.isNotEmpty()
        val visibility: Int = if(!isPreview || hasNotes) VISIBLE else GONE
        dwpGeneralNoteLabel.visibility = visibility
        dwpGeneralNoteContainer.visibility = visibility

        assignNoteToggleListener(dwpGeneralNotesLayout, hasNotes)
        generateNotes(notes, dwpGeneralNotesLayout.dwpExpandableNotesLayout)

        dwpGeneralNotesLayout.dwpAddNoteButton.setVisibilityAndRun(!isPreview) {
            onAddNote?.invoke(dwpGeneralNotesLayout.dwpAddNoteButton, WordDefinitionTabFragment.GENERAL_NOTE_CONTAINER_ID)
        }
    }

    fun renderSection(sections: List<DisplaySection>, parent: LinearLayout) {
        sections.forEachIndexed { index, section ->
            val sectionBinding: DwpSectionsBinding = DwpSectionsBinding.inflate(fragment.layoutInflater, parent, false).apply {
                dwpSectionsLayout.layoutParams = sectionLayout(
                    if (index == 0) null else SECTION_TOP_MARGIN_DP,
                    SECTION_BOTTOM_MARGIN_DP
                )

                val prefix: String = fragment.getString(R.string.dwp_ordered_number_prefix, index + 1)
                val indent: Int = ListSpanUtil.calculateIndent(dwpMeaning, prefix)
                dwpMeaning.text = ListSpanUtil.applyLeadingMargin(prefix + section.meaningText, 0, indent)

                if (section.kanaList.isNotEmpty()) {
                    val kanaText: String = section.kanaList.joinToString(fragment.getString(R.string.dwp_kana_separator))
                    dwpKana.text = ListSpanUtil.applyLeadingMargin(kanaText, indent, indent)
                } else {
                    dwpKana.visibility = GONE
                }
                root.setTag(R.id.dwp_tag_section_id, section.sectionId)
                val hasNotes: Boolean = section.notes.isNotEmpty()
                dwpSectionNotes.dwpNotesHeader.visibility =  if(!isPreview || !hasNotes) VISIBLE else GONE
                assignNoteToggleListener(dwpSectionNotes, hasNotes, section.sectionId)
                generateNotes(section.notes, dwpSectionNotes.dwpExpandableNotesLayout, section.sectionId)

                dwpSectionNotes.dwpAddNoteButton.setVisibilityAndRun(!isPreview) {
                    onAddNote?.invoke(dwpSectionNotes.dwpAddNoteButton, section.sectionId)
                }
            }
            parent.addView(sectionBinding.root)
        }
    }

    fun updateNotes(sectionId: Int? = null, wordFormEntryDataToFormat: WordEntryFormData){
        if(sectionId != null){
            val notes = wordFormEntryDataToFormat.wordSectionMap[sectionId]?.getComponentNoteInputMapAsList()?.map { note ->
                DisplayNote(note.itemProperties.getId(), note.inputTextValue)
            } ?: emptyList()
            updateSectionNotes(sectionId, notes)
        }else {
            val notes = wordFormEntryDataToFormat.getEntryNoteMapAsList().map { note ->
                DisplayNote(note.itemProperties.getId(), note.inputTextValue)
            }
            updateGeneralNotes(notes)
        }
    }


    private fun updateGeneralNotes(notes: List<DisplayNote>) = with(binding) {
        dwpGeneralNotesLayout.dwpExpandableNotesLayout.removeAllViews()
        val hasNotes = notes.isNotEmpty()
        dwpGeneralNotesLayout.dwpNotesHeader.visibility =
            if (!isPreview || !hasNotes) VISIBLE else GONE
        generateNotes(notes, dwpGeneralNotesLayout.dwpExpandableNotesLayout)
    }


    fun updateSectionNotes(sectionId: Int, newNotes: List<DisplayNote>) {
        val parentLayout = binding.dwpDefinitionContainerLayout
        for (i in 0 until parentLayout.childCount) {
            val sectionView = parentLayout.getChildAt(i)
            val taggedSectionId = sectionView.getTag(R.id.dwp_tag_section_id) as? Int
            if (taggedSectionId == sectionId) {
                val sectionBinding = DwpSectionsBinding.bind(sectionView)
                val hasNotes = newNotes.isNotEmpty()
                sectionBinding.dwpSectionNotes.dwpNotesHeader.visibility =
                    if (!isPreview || !hasNotes) VISIBLE else GONE
                // Clear and re-add notes
                val notesLayout = sectionBinding.dwpSectionNotes.dwpExpandableNotesLayout
                notesLayout.removeAllViews()
                generateNotes(newNotes, notesLayout, sectionId)
                break
            }
        }
    }

    fun generateNotes(notes: List<DisplayNote>, parent: LinearLayout, noteContainerId: Int = -1) {
        notes.forEachIndexed { index, note ->
            val binding: DwpNotesTextviewBinding = DwpNotesTextviewBinding.inflate(fragment.layoutInflater, parent, false)
            val prefix: String = fragment.getString(R.string.dwp_ordered_number_prefix, index + 1)
            val indent: Int = ListSpanUtil.calculateIndent(binding.dwpNoteText, prefix)
            binding.dwpNoteText.text = ListSpanUtil.applyLeadingMargin(prefix + note.text, 0, indent)

            binding.overflowIcon.setVisibilityAndRun(!isPreview) {
                assignPopupMenu?.invoke(binding, note.id, noteContainerId)
            }
            parent.addView(binding.root)
        }
    }

    private fun assignNoteToggleListener(
        dwpNotesBinding: DwpNotesBinding,
        hasNotes: Boolean,
        sectionId: Int? = null
    ) {
        if (!hasNotes) {
            dwpNotesBinding.dwpNotesHeader.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
            dwpNotesBinding.dwpNotesHeader.setOnClickListener(null)
            return
        }

        val sectionKey: Int = sectionId ?: -1
        val isExpanded: Boolean = if (!isPreview) {
            dwpNotesBinding.dwpAddNoteButton.visibility = VISIBLE
            isToggleSectionExpanded?.invoke(sectionKey) ?: true
        } else {
            dwpNotesBinding.dwpAddNoteButton.visibility = GONE
            true
        }

        toggleNoteVisibility(dwpNotesBinding, isExpanded)

        dwpNotesBinding.dwpNotesHeader.setOnClickListener {
            val isVisible: Boolean = dwpNotesBinding.dwpExpandableNotesLayout.isVisible
            toggleNoteVisibility(dwpNotesBinding, !isVisible)
            if (!isPreview) onToggleSectionExpanded?.invoke(sectionKey)
        }
    }

    private fun toggleNoteVisibility(binding: DwpNotesBinding, show: Boolean) {
        val drawable: Drawable? = ContextCompat.getDrawable(
            fragment.requireContext(),
            if (show) R.drawable.arrow_drop_up else R.drawable.arrow_drop_down
        )
        binding.dwpExpandableNotesLayout.visibility = if (show) VISIBLE else GONE
        binding.dwpNotesHeader.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null)
    }

    private fun View.setVisibilityAndRun(shouldShow: Boolean, action: () -> Unit) {
        visibility = if (shouldShow) VISIBLE else GONE
        if (shouldShow) action()
    }

    fun formatText(wordFormEntryDataToFormat: WordEntryFormData): DisplayEntryUIModel = with(wordFormEntryDataToFormat){
        val (mainText: String?, subText: String?) = DictionaryDisplayUtil.getWordClassDisplayText(wordClassInput)

        val entryNoteTextList: List<DisplayNote> = getEntryNoteMapAsList().map {
            DisplayNote(it.itemProperties.getId(), it.inputTextValue)
        }

        val sectionList: List<DisplaySection> = wordSectionMap.map { (sectionId, sectionData) ->
            DisplaySection(
                sectionId = sectionId,
                meaningText = sectionData.meaningInput.inputTextValue,
                kanaList = sectionData.getKanaInputMapAsList().map { it.inputTextValue },
                notes = sectionData.getComponentNoteInputMapAsList().map { note ->
                    DisplayNote(note.itemProperties.getId(), note.inputTextValue)
                }
            )
        }
        return DisplayEntryUIModel(
        primaryTextInput.inputTextValue,
        mainText,
        subText,
        entryNoteTextList,
        sectionList
        )
    }

    private fun sectionLayout(top: Float?, bottom: Float?) = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        top?.let { topMargin = dpToPx(it, fragment.resources.displayMetrics).toInt() }
        bottom?.let { bottomMargin = dpToPx(it, fragment.resources.displayMetrics).toInt() }
    }

    companion object {
        private const val SECTION_TOP_MARGIN_DP = 20f
        private const val SECTION_BOTTOM_MARGIN_DP = 20f
    }
}