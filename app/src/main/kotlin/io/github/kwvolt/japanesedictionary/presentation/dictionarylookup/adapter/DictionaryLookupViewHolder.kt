package io.github.kwvolt.japanesedictionary.presentation.dictionarylookup.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DluRecyclerviewItemBinding
import io.github.kwvolt.japanesedictionary.databinding.DluRecyclerItemGlossBinding
import io.github.kwvolt.japanesedictionary.databinding.DluRecyclerLoadingItemBinding
import io.github.kwvolt.japanesedictionary.databinding.DluRecyclerNoItemBinding
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.SimplifiedWordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.SimplifiedWordSectionFormData
import io.github.kwvolt.japanesedictionary.util.DictionaryDisplayUtil
import io.github.kwvolt.japanesedictionary.util.ListSpanUtil
import io.github.kwvolt.japanesedictionary.util.icon.MaterialButtonIcon
import io.github.kwvolt.japanesedictionary.util.icon.toggleBookmark

interface DictionaryLookUpRecyclerCallBack {
    fun toggleBookmark(dictionaryId: Long)
    fun displayPopupMenu(dictionaryId: Long, view: View)
    fun moveToDetailPage(dictionaryId: Long, view: View)
    fun toggleGlossLayout(dictionaryId: Long)
}


sealed class  DictionaryLookupViewHolder(view: View): RecyclerView.ViewHolder(view){
    class DetailViewHolder(
        private val binding: DluRecyclerviewItemBinding,
        private val callBack: DictionaryLookUpRecyclerCallBack,
        private val inflater: LayoutInflater = LayoutInflater.from(binding.root.context)
    ): DictionaryLookupViewHolder(binding.root){
        fun bind(simplifiedWordEntryFormData: SimplifiedWordEntryFormData) = with(binding){
            // primary text
            dluRecyclerPrimaryText.text = simplifiedWordEntryFormData.primaryTextInput.inputTextValue
            // word class
            val (mainText: String?, subText: String?) = DictionaryDisplayUtil.getWordClassDisplayText(simplifiedWordEntryFormData.wordClassInput)
            val wordClassText: String? = DictionaryDisplayUtil.displayWordClass(mainText, subText, root.context)
            DictionaryDisplayUtil.displayOrHideWordClass(wordClassText, dluRecyclerWordClass)

            // clear gloss
            dluFirstGlossLayout.removeAllViews()
            dluExpandableGlossLayout.removeAllViews()

            // gloss
            simplifiedWordEntryFormData.wordSectionList.forEachIndexed { index, data ->
                val targetLayout = getTargetLayout(index)
                inflateItemGloss(index, targetLayout, data)
            }
            dluExpandableGlossLayout.visibility = if (simplifiedWordEntryFormData.wordSectionList.size <= 1) {
                    GONE
                }
                else {
                    setExpandCollapseButton(
                        dluExpandDetailButton,
                        dluExpandableGlossLayout,
                        simplifiedWordEntryFormData.dictionaryId,
                        simplifiedWordEntryFormData.isExpanded
                    )
                    VISIBLE
                }
            setBookmarkButton(simplifiedWordEntryFormData.dictionaryId,simplifiedWordEntryFormData.isBookmark)
            setOverFlowButton(simplifiedWordEntryFormData.dictionaryId)
            setMoveToDetailPageButton(simplifiedWordEntryFormData.dictionaryId)
        }

        private fun getTargetLayout(index: Int): LinearLayout {
            return if (index == 0) {
                binding.dluFirstGlossLayout
            }else{
                binding.dluExpandableGlossLayout
            }
        }

        private fun inflateItemGloss(index: Int, layout: LinearLayout, section: SimplifiedWordSectionFormData) {
            val glossBinding = DluRecyclerItemGlossBinding.inflate(inflater, layout, false)
            glossBinding.bindGloss(index, section, binding.root.context)
            layout.addView(glossBinding.root)
        }

        private fun setExpandCollapseButton(button: MaterialButton, layout: LinearLayout, dictionaryId: Long, isExpanded: Boolean){
            if (isExpanded) {
                layout.visibility = VISIBLE
                button.icon = ContextCompat.getDrawable(binding.root.context, R.drawable.arrow_drop_up)
            } else {
                layout.visibility = GONE
                button.icon = ContextCompat.getDrawable(binding.root.context, R.drawable.arrow_drop_down)
            }
            button.setOnClickListener {
                callBack.toggleGlossLayout(dictionaryId)
            }
        }

        private fun setBookmarkButton(dictionaryId: Long, isBookmarked: Boolean){
            MaterialButtonIcon(binding.dluBookmarkButton).toggleBookmark(binding.root.context, isBookmarked)
            binding.dluBookmarkButton.setOnClickListener {
                callBack.toggleBookmark(dictionaryId)
            }
        }

        private fun setOverFlowButton(dictionaryId: Long){
            binding.dluOverflowButton.setOnClickListener { view ->
                callBack.displayPopupMenu(dictionaryId, view)
            }
        }

        private fun setMoveToDetailPageButton(dictionaryId: Long){
            binding.dluMoveToDetailPageButton.setOnClickListener { view ->
                callBack.moveToDetailPage(dictionaryId, view)
            }
        }

        private fun DluRecyclerItemGlossBinding.bindGloss(
            index: Int,
            section: SimplifiedWordSectionFormData,
            context: Context
        ) {
            val prefix = context.getString(R.string.dwp_ordered_number_prefix, index + 1)
            val indent = ListSpanUtil.calculateIndent(dluFirstMeaning, prefix)
            dluFirstMeaning.text = ListSpanUtil.applyLeadingMargin(prefix + section.meaningInput.inputTextValue, 0, indent)
            DictionaryDisplayUtil.displayKanaText(
                dluFirstKana,
                section.kanaInputList.map { it.inputTextValue },
                indent,
                indent,
                context
            )
        }

    }
    class LoadingViewHolder(binding: DluRecyclerLoadingItemBinding): DictionaryLookupViewHolder(binding.root)
    class NoItemViewHolder(binding:DluRecyclerNoItemBinding): DictionaryLookupViewHolder(binding.root)
}