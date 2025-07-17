package io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.github.kwvolt.japanesedictionary.databinding.ButtonItemBinding
import io.github.kwvolt.japanesedictionary.databinding.EditTextItemBinding
import io.github.kwvolt.japanesedictionary.databinding.LabelItemBinding
import io.github.kwvolt.japanesedictionary.databinding.WordClassItemBinding
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.StaticLabelFormUIItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassFormUIItem
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.ButtonCallBack
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.ButtonViewHolder
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.EditTextCallBack
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.EditTextViewHolder
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.LabelTextCallBack
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.LabelTextViewHolder
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.WordClassCallBack
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.WordClassViewHolder

class AddUpdateAdapter(
    private val wordClassCallBack: WordClassCallBack,
    private val labelTextCallBack: LabelTextCallBack,
    private val editTextCallBack: EditTextCallBack,
    private val buttonCallBack: ButtonCallBack
) : ListAdapter<BaseItem, AddUpdateViewHolder>(AddUpdateDiffUtilCallback()) {

    enum class ViewType {
        LABEL,
        WORD_CLASS,
        EDITTEXT,
        BUTTON,
    }



    override fun getItemViewType(position: Int): Int {

        return when (getItem(position)) {
            is ButtonItem -> ViewType.BUTTON.ordinal
            is InputTextFormUIItem -> ViewType.EDITTEXT.ordinal
            is LabelItem, is StaticLabelFormUIItem -> ViewType.LABEL.ordinal
            is WordClassFormUIItem -> ViewType.WORD_CLASS.ordinal
            else -> {
                throw IllegalStateException("Illegal BaseItem type ${getItem(position)::class.java} in AddUpdateAdapter->getItemViewType()")
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): AddUpdateViewHolder {
        return when (viewType){
            ViewType.LABEL.ordinal -> {
                val binding = LabelItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
                LabelTextViewHolder(binding, labelTextCallBack)
            }
            ViewType.EDITTEXT.ordinal -> {
                val binding = EditTextItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
                EditTextViewHolder(binding, editTextCallBack)
            }
            ViewType.BUTTON.ordinal -> {
                val binding = ButtonItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
                ButtonViewHolder(binding, buttonCallBack)
            }
            ViewType.WORD_CLASS.ordinal -> {
                val binding = WordClassItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
                WordClassViewHolder(binding, wordClassCallBack)
            }
            else -> {
                throw IllegalStateException("Unknown ViewType Int encountered: $viewType in AddUpdateAdapter->onCreateViewHolder()")
            }
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: AddUpdateViewHolder, position: Int) {
        val item: BaseItem = getItem(position)
        viewHolder.bind(item)
    }
}

class AddUpdateDiffUtilCallback: DiffUtil.ItemCallback<BaseItem>() {

    override fun areItemsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean {
        return oldItem.itemProperties.getIdentifier() == newItem.itemProperties.getIdentifier()
    }

    override fun areContentsTheSame(oldItem: BaseItem, newItem: BaseItem): Boolean {
        return oldItem == newItem
    }
}

