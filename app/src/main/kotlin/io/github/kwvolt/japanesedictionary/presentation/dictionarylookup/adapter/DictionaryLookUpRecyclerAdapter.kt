package io.github.kwvolt.japanesedictionary.presentation.dictionarylookup.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.github.kwvolt.japanesedictionary.databinding.DluRecyclerviewItemBinding
import io.github.kwvolt.japanesedictionary.databinding.DluRecyclerLoadingItemBinding
import io.github.kwvolt.japanesedictionary.domain.model.SimplifiedWordEntryFormData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.github.kwvolt.japanesedictionary.databinding.DluRecyclerNoItemBinding

class DictionaryLookUpRecyclerAdapter(
    private val callBack: DictionaryLookUpRecyclerCallBack,
) : ListAdapter<DictionaryLookupItem, DictionaryLookupViewHolder>(
    DictionaryLookupDiffUtilCallback()
) {
    init {
        setHasStableIds(true)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DictionaryLookupItem.WordItem -> VIEW_TYPE_ITEM
            is DictionaryLookupItem.LoadingItem -> VIEW_TYPE_LOADING
            is DictionaryLookupItem.NoItem -> VIEW_TYPE_NO_ITEM
        }
    }

    override fun getItemId(position: Int): Long {
        return when (val item = getItem(position)) {
            is DictionaryLookupItem.WordItem -> item.data.dictionaryId
            is DictionaryLookupItem.LoadingItem -> Long.MAX_VALUE - position
            is DictionaryLookupItem.NoItem -> Long.MAX_VALUE - 1000 - position
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DictionaryLookupViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = DluRecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DictionaryLookupViewHolder.DetailViewHolder(binding, callBack)

            }
            VIEW_TYPE_LOADING -> {
                val binding = DluRecyclerLoadingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DictionaryLookupViewHolder.LoadingViewHolder(binding)
            }
            else -> {
                val binding = DluRecyclerNoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DictionaryLookupViewHolder.NoItemViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: DictionaryLookupViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is DictionaryLookupItem.WordItem -> if (holder is DictionaryLookupViewHolder.DetailViewHolder) {
                holder.bind(item.data)
            }
            is DictionaryLookupItem.LoadingItem -> { /* no bind needed for loading */ }
            is DictionaryLookupItem.NoItem -> {}
        }
    }

    fun updateItems(items: List<SimplifiedWordEntryFormData>, isLoading: Boolean) {
        val displayItems: List<DictionaryLookupItem> = when {
            items.isEmpty() && !isLoading -> listOf(DictionaryLookupItem.NoItem)
            else -> {
                val lookupItems: MutableList<DictionaryLookupItem> = items.map { DictionaryLookupItem.WordItem(it) }.toMutableList()
                lookupItems.apply {
                    if (isLoading) add(DictionaryLookupItem.LoadingItem)
                }
            }
        }
        submitList(displayItems)
    }

    fun showLoading() {
        if (currentList.any { it is DictionaryLookupItem.LoadingItem }) return
        val currentList = currentList.toMutableList()
        currentList.add(DictionaryLookupItem.LoadingItem)
        submitList(currentList)
    }

    fun hideLoading() {
        submitList(currentList.filterNot { it is DictionaryLookupItem.LoadingItem })
    }

    fun clearItems(){
        submitList(null)
    }

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
        private const val VIEW_TYPE_NO_ITEM = 2
    }
}

class DictionaryLookupDiffUtilCallback : DiffUtil.ItemCallback<DictionaryLookupItem>() {
    override fun areItemsTheSame(oldItem: DictionaryLookupItem, newItem: DictionaryLookupItem): Boolean {
        return when {
            oldItem is DictionaryLookupItem.WordItem && newItem is DictionaryLookupItem.WordItem ->
                oldItem.data.dictionaryId == newItem.data.dictionaryId
            oldItem is DictionaryLookupItem.LoadingItem && newItem is DictionaryLookupItem.LoadingItem ->
                true
            oldItem is DictionaryLookupItem.NoItem && newItem is DictionaryLookupItem.NoItem ->
                true
            else -> false

        }
    }

    override fun areContentsTheSame(oldItem: DictionaryLookupItem, newItem: DictionaryLookupItem): Boolean {
        return oldItem == newItem
    }
}

sealed class DictionaryLookupItem {
    data class WordItem(val data: SimplifiedWordEntryFormData) :DictionaryLookupItem()
    data object LoadingItem : DictionaryLookupItem()
    data object NoItem : DictionaryLookupItem()
}