package io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.BaseItem

abstract class AddUpdateViewHolder(view: View): RecyclerView.ViewHolder(view){
    abstract fun bind(baseItem: BaseItem)
}