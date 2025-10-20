package io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.DisplayItem

abstract class UpsertViewHolder(view: View, upsertCallback: UpsertRecyclerCallBack): RecyclerView.ViewHolder(view){
    abstract fun bind(item: DisplayItem)
}

interface UpsertRecyclerCallBack{
    fun setHasUpdated()
}