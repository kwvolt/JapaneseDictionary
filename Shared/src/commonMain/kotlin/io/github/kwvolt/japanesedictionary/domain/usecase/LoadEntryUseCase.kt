package io.github.kwvolt.japanesedictionary.domain.usecase

import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormBuilder
import io.github.kwvolt.japanesedictionary.domain.model.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData

class LoadEntryUseCase(private val _wordEntryFormBuilder: WordEntryFormBuilder) {
    suspend operator fun invoke(dictionaryId: Long, formItemManager: FormItemManager, condition: Boolean){
        if(condition){
            val formResult = _wordEntryFormBuilder.buildDetailedFormData(dictionaryId, formItemManager)
        }else {
            //_wordEntryFormData = WordEntryFormData.buildDefault(formItemManager)
        }
    }
}