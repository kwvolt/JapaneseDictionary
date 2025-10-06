package io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.github.kwvolt.japanesedictionary.databinding.UpsertRecyclerviewButtonItemBinding
import io.github.kwvolt.japanesedictionary.databinding.UpsertRecyclerviewEditTextItemBinding
import io.github.kwvolt.japanesedictionary.databinding.UpsertRecyclerviewLabelItemBinding
import io.github.kwvolt.japanesedictionary.databinding.UpsertRecyclerviewWordClassItemBinding
import io.github.kwvolt.japanesedictionary.domain.model.items.item.DisplayItem
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder.ButtonCallBack
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder.ButtonViewHolder
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder.EditTextCallBack
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder.EditTextViewHolder
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder.LabelTextCallBack
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder.LabelTextViewHolder
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder.UpsertRecyclerCallBack
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder.UpsertViewHolder
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder.WordClassCallBack
import io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder.WordClassViewHolder

class UpsertAdapter(
    private val wordClassCallBack: WordClassCallBack,
    private val labelTextCallBack: LabelTextCallBack,
    private val editTextCallBack: EditTextCallBack,
    private val buttonCallBack: ButtonCallBack
) : ListAdapter<DisplayItem, UpsertViewHolder>(AddUpdateDiffUtilCallback()) {

    private var hasUpdated: Boolean = false

    private val upsertAdapterCallBack: UpsertRecyclerCallBack = object : UpsertRecyclerCallBack {
        override fun setHasUpdated() {
            hasUpdated = true
        }
    }

    fun hasDataBeenUpdated(): Boolean = hasUpdated

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): UpsertViewHolder {
        return when (viewType){
            DisplayItem.RecyclerViewType.LABEL.id -> {
                val binding = UpsertRecyclerviewLabelItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
                LabelTextViewHolder(binding, labelTextCallBack, upsertAdapterCallBack)
            }
            DisplayItem.RecyclerViewType.TEXT.id -> {
                val binding = UpsertRecyclerviewEditTextItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
                EditTextViewHolder(binding, editTextCallBack, upsertAdapterCallBack)
            }
            DisplayItem.RecyclerViewType.BUTTON.id -> {
                val binding = UpsertRecyclerviewButtonItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
                ButtonViewHolder(binding, buttonCallBack, upsertAdapterCallBack)
            }
            DisplayItem.RecyclerViewType.WORD_CLASS.id -> {
                val binding = UpsertRecyclerviewWordClassItemBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
                WordClassViewHolder(binding, wordClassCallBack, upsertAdapterCallBack)
            }
            else -> {
                throw IllegalStateException("Unknown ViewType Int encountered: $viewType in AddUpdateAdapter->onCreateViewHolder()")
            }
        }
    }

    override fun onBindViewHolder(viewHolder: UpsertViewHolder, position: Int) {
        val item: DisplayItem = getItem(position)
        viewHolder.bind(item)
    }
}

class AddUpdateDiffUtilCallback: DiffUtil.ItemCallback<DisplayItem>() {

    override fun areItemsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
        return oldItem.item.itemProperties.getIdentifier() == newItem.item.itemProperties.getIdentifier()
    }

    override fun areContentsTheSame(oldItem: DisplayItem, newItem: DisplayItem): Boolean {
        return oldItem == newItem
    }
}

