package io.github.kwvolt.japanesedictionary.presentation.upsert.wordentryadapter.viewholder

import android.view.View
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import io.github.kwvolt.japanesedictionary.databinding.UpsertRecyclerviewWordClassItemBinding
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.SubClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.interfaces.WordChildClassContainer
import io.github.kwvolt.japanesedictionary.domain.model.items.item.DisplayItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.ErrorMessage
import io.github.kwvolt.japanesedictionary.domain.model.items.item.WordClassItem
import io.github.kwvolt.japanesedictionary.util.WordClassBindUtil


interface WordClassCallBack{
    fun updateMainClassId(displayItem: DisplayItem.DisplayWordClassItem, selectionPosition: Int, position: Int): Boolean
    fun updateSubClassId(displayItem: DisplayItem.DisplayWordClassItem, selectionPosition: Int, position: Int)
    fun getMainClassListIndex(wordClassItem: WordClassItem): Int
    fun getSubClassListIndex(wordClassItem: WordClassItem): Int
    fun getMainClasList(): List<MainClassContainer>
    fun getSubClassList(wordClassItem: WordClassItem): List<SubClassContainer>
    fun getErrorMessage(errorMessage: ErrorMessage): String?
    fun onUnexpectedError(exception: Throwable)
}

class WordClassViewHolder(
    private val binding: UpsertRecyclerviewWordClassItemBinding,
    private val callBack: WordClassCallBack,
    private val upsertRecyclerCallBack: UpsertRecyclerCallBack
) : UpsertViewHolder(binding.root, upsertRecyclerCallBack) {
    override fun bind(item: DisplayItem) = with(binding) {
        if(item is DisplayItem.DisplayWordClassItem) {
            WordClassBindUtil.bindWordClassItem(
                root.context,
                upsertMainClassDrop,
                upsertSubClassDrop,
                onItemSelectedInMainClass = { selectedPosition: Int, bindSubClass:(Boolean)->Unit ->
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        val hasUpdated = callBack.updateMainClassId(item, selectedPosition, bindingAdapterPosition)
                        upsertRecyclerCallBack.setHasUpdated()
                        bindSubClass(hasUpdated)
                    }
                },
                onItemSelectedInSubClass = {selectedPosition ->
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        upsertRecyclerCallBack.setHasUpdated()
                        callBack.updateSubClassId(item, selectedPosition, bindingAdapterPosition)
                    }
                },
                getSubClassList = {callBack.getSubClassList(item.item)},
                getSubClassIndex = {callBack.getSubClassListIndex(item.item) },
                getMainClassList = {callBack.getMainClasList()},
                getMainClassIndex = {callBack.getMainClassListIndex(item.item)}
            )
            checkAndSetError(item.errorMessage)
        }
        else {
            callBack.onUnexpectedError(
                IllegalStateException("Unknown display item type: ${item::class.java} in WordClassViewHolder").fillInStackTrace()
            )
        }
    }

    private fun checkAndSetError(errorMessage: ErrorMessage){
        val message = callBack.getErrorMessage(errorMessage)
        binding.upsertWordClassError.apply {
            text = message.orEmpty()
            visibility = if (message.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }
}