package io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.LabelItemBinding
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelHeaderType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.NamedItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelItem
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.AddUpdateViewHolder
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.getResourceString


interface LabelTextCallBack{
    fun entryRemoveItems(entryLabelItem: EntryLabelItem, position: Int)
    fun getLabelType(labelItem: LabelItem): LabelHeaderType
    fun getWidgetName(namedItem: NamedItem): String
    fun getErrorMessage(staticLabelFormUIItem: StaticLabelFormUIItem): String?
}

class LabelTextViewHolder(private val binding: LabelItemBinding, private val callBack: LabelTextCallBack) : AddUpdateViewHolder(binding.root) {

    override fun bind(baseItem: BaseItem) {
        when (baseItem) {
            is EntryLabelItem -> {
                handleEntryLabelItem(baseItem)
                setLabelText(baseItem)
            }
            is StaticLabelItem -> {
                handleLabelItem(baseItem)
                setLabelText(baseItem)
            }
            is StaticLabelFormUIItem -> {
                handleLabelItem(baseItem.staticLabelItem)
                setLabelText(baseItem.staticLabelItem)
                checkAndSetError(baseItem)
            }
            else -> throw IllegalStateException("Unknown item type: ${baseItem::class.java}")
        }
    }

    private fun setLabelText(labelItem: LabelItem) {
        val labelName: String = callBack.getWidgetName(labelItem)
        binding.addUpdateLabel.text = getResourceString(binding, labelName, R.string.generic_label)
    }

    private fun handleEntryLabelItem(item: EntryLabelItem) {
        setupEntryLabelAppearance()
        setupEntryLabelDeleteAction(item)
    }

    private fun setupEntryLabelAppearance() {
        binding.apply {
            addUpdateLabel.setTextAppearance(R.style.TextAppearance_AppCompat_Headline1)
            setDeleteIconVisible(true)
        }
    }

    private fun setupEntryLabelDeleteAction(item: EntryLabelItem) {
        binding.addUpdateEditTextDelete.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                callBack.entryRemoveItems(item, position)
            }
        }
    }

    private fun handleLabelItem(item: LabelItem) {
        val labelType = callBack.getLabelType(item)
        val textAppearance = getTextAppearanceForLabelType(labelType)
        binding.addUpdateLabel.setTextAppearance(textAppearance)
        setDeleteIconVisible(false)
    }

    private fun getTextAppearanceForLabelType(labelHeaderType: LabelHeaderType): Int {
        return when (labelHeaderType) {
            LabelHeaderType.HEADER -> R.style.TextAppearance_AppCompat_Headline1
            LabelHeaderType.SUB_HEADER -> R.style.TextAppearance_AppCompat_Subhead
        }
    }

    private fun setDeleteIconVisible(isVisible: Boolean) {
        binding.addUpdateEditTextDelete.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun checkAndSetError(staticLabelFormUIItem: StaticLabelFormUIItem){
        val message = callBack.getErrorMessage(staticLabelFormUIItem)
        binding.addUpdateLabelError.apply {
            text = message.orEmpty()
            visibility = if (message.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }
}