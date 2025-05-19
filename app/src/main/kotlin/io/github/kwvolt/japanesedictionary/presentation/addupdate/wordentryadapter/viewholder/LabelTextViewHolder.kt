package io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder

import android.view.View
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.LabelItemBinding
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.NamedItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelItem
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.AddUpdateViewHolder
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.getResourceString


interface LabelTextCallBack{
    fun entryRemoveItems(entryLabelItem: EntryLabelItem, position: Int)
    fun updateEntryIndexIfNeeded(entryLabelItem: EntryLabelItem, position: Int)
    fun getLabelType(labelItem: LabelItem): LabelType
    fun getWidgetName(namedItem: NamedItem): String
}

class LabelTextViewHolder(private val binding: LabelItemBinding, private val callBack: LabelTextCallBack) : AddUpdateViewHolder(binding.root) {

    override fun bind(baseItem: BaseItem) {
        if (baseItem is LabelItem) {
            // Process based on specific item type
            when (baseItem) {
                is EntryLabelItem -> handleEntryLabelItem(baseItem)
                is StaticLabelItem -> handleLabelItem(baseItem)
            }

            // Set the label text
            val labelName: String = callBack.getWidgetName(baseItem)
            binding.addUpdateLabel.text = getResourceString(binding, labelName, R.string.generic_label)
        } else {
            throw IllegalStateException("Unknown item type encountered: ${baseItem::class.java} in LabelTextViewHolder->bind()")
        }
    }

    private fun handleEntryLabelItem(item: EntryLabelItem) {
        callBack.updateEntryIndexIfNeeded(item, adapterPosition)
        setupEntryLabelAppearance()
        setupEntryLabelDeleteAction(item)
    }

    private fun setupEntryLabelAppearance() {
        binding.apply {
            addUpdateLabel.setTextAppearance(R.style.TextAppearance_AppCompat_Headline1)
            addUpdateEditTextDelete.visibility = View.VISIBLE
        }
    }

    private fun setupEntryLabelDeleteAction(item: EntryLabelItem) {
        binding.addUpdateEditTextDelete.setOnClickListener {
            callBack.entryRemoveItems(item, adapterPosition)
        }
    }

    private fun handleLabelItem(item: LabelItem) {
        val labelType = callBack.getLabelType(item)
        val textAppearance = getTextAppearanceForLabelType(labelType)
        binding.addUpdateLabel.setTextAppearance(textAppearance)
        binding.addUpdateEditTextDelete.visibility = View.GONE
    }

    private fun getTextAppearanceForLabelType(labelType: LabelType): Int {
        return when (labelType) {
            LabelType.HEADER -> R.style.TextAppearance_AppCompat_Headline1
            LabelType.SUB_HEADER -> R.style.TextAppearance_AppCompat_Subhead
        }
    }
}