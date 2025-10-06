package io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.UpsertRecyclerviewLabelItemBinding
import io.github.kwvolt.japanesedictionary.ui.upsert.FormKeys
import io.github.kwvolt.japanesedictionary.domain.model.items.item.DisplayItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.SectionLabelItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.model.items.item.LabelItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.LabelHeaderType
import io.github.kwvolt.japanesedictionary.domain.model.items.item.StaticLabelItem
import io.github.kwvolt.japanesedictionary.ui.StringResourceFromFormKey


interface LabelTextCallBack{
    fun removeSection(sectionLabelItem: SectionLabelItem, position: Int)
    fun getLabelType(labelItem: LabelItem): LabelHeaderType
    fun getLabelKey(labelItem: StaticLabelItem): FormKeys
    fun getSectionCount(sectionLabelItem: SectionLabelItem): Int
    fun getErrorMessage(errorMessage: ErrorMessage): String?
    fun onUnexpectedError(exception: Throwable)
}

class LabelTextViewHolder(private val binding: UpsertRecyclerviewLabelItemBinding, private val callBack: LabelTextCallBack, private val upsertRecyclerCallBack: UpsertRecyclerCallBack) : UpsertViewHolder(binding.root, upsertRecyclerCallBack) {
    override fun bind(item: DisplayItem) {
        checkAndSetErrorIfPresent(item)
        binding.upsertLabelHint.visibility = View.GONE
        when (val baseItem = item.item) {
            is SectionLabelItem -> bindSectionLabelItem(baseItem)
            is StaticLabelItem -> bindStaticLabelItem(baseItem)
            else -> callBack.onUnexpectedError(
                IllegalStateException("Unknown item type: ${baseItem::class.java} in LabelTextViewHolder").fillInStackTrace()
            )
        }
    }

    private fun bindSectionLabelItem(item: SectionLabelItem) {
        setLabelItemAppearance(item, true)
        setupSectionLabelDeleteAction(item)
        setSectionLabelText(item, R.string.upsert_label_vh_section_label)
        setDividerVisible(true)
    }

    private fun bindStaticLabelItem(item: StaticLabelItem) {
        setLabelItemAppearance(item, false)
        setStaticLabelItemText(item, R.string.generic_label)
        setDividerVisible(false)
    }

    private fun setStaticLabelItemText(staticLabelItem: StaticLabelItem, stringResource: Int){
        val formKey: FormKeys = callBack.getLabelKey(staticLabelItem)
        val labelText: String = itemView.context.getString(StringResourceFromFormKey.getStringResource(formKey))
        binding.upsertLabel.text = itemView.context.getString(stringResource, labelText)
        binding.upsertLabelHint.apply{
            StringResourceFromFormKey.getLabelHint(formKey)
                ?.let {
                    text = itemView.context.getString(it)
                    visibility = View.VISIBLE
                }
        }
    }


    private fun setSectionLabelText(labelItem: SectionLabelItem, stringResource: Int) {
        val sectionCount: Int = callBack.getSectionCount(labelItem)
        binding.upsertLabel.text = itemView.context.getString(stringResource, sectionCount)
    }

    private fun setupSectionLabelDeleteAction(item: SectionLabelItem) {
        binding.upsertLabelDelete.setOnClickListener {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                callBack.removeSection(item, position)
            }
        }
    }

    private fun setLabelItemAppearance(item: LabelItem, isDeleteVisible: Boolean) {
        val labelType: LabelHeaderType = callBack.getLabelType(item)
        val textAppearance: Int = getTextAppearanceForLabelType(labelType)
        binding.upsertLabel.setTextAppearance(textAppearance)
        setDeleteIconVisible(isDeleteVisible)
    }

    private fun getTextAppearanceForLabelType(labelHeaderType: LabelHeaderType): Int {
        return when (labelHeaderType) {
            LabelHeaderType.HEADER -> R.style.TextAppearance_AppCompat_Headline1
            LabelHeaderType.SUB_HEADER -> R.style.TextAppearance_AppCompat_Subhead
        }
    }

    private fun setDeleteIconVisible(isVisible: Boolean) {
        binding.upsertLabelDelete.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
    
    private fun setDividerVisible(isVisible: Boolean){
        binding.upsertLabelDivider.visibility  = if (isVisible) View.VISIBLE else View.GONE
    }

    private fun checkAndSetErrorIfPresent(item: DisplayItem) {
        if (item is DisplayItem.DisplayLabelItem) {
            item.errorMessage?.let {
                val message = callBack.getErrorMessage(it)
                binding.upsertLabelError.apply {
                    text = message.orEmpty()
                    visibility = if (message.isNullOrEmpty()) View.GONE else View.VISIBLE
                }

            }
        }
    }
}