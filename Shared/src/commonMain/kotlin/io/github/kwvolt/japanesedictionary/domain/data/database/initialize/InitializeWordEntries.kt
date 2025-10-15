package io.github.kwvolt.japanesedictionary.domain.data.database.initialize

import io.github.kwvolt.japanesedictionary.domain.data.database.DatabaseResult
import io.github.kwvolt.japanesedictionary.domain.data.service.wordclass.WordClassFetcher
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.ValidUpsertResult
import io.github.kwvolt.japanesedictionary.domain.data.service.wordentry.WordEntryFormUpsertValidation
import io.github.kwvolt.japanesedictionary.domain.model.FormItemManager
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.domain.model.WordSectionFormData
import io.github.kwvolt.japanesedictionary.domain.model.items.item.InputTextType
import io.github.kwvolt.japanesedictionary.domain.model.items.item.TextItem
import io.github.kwvolt.japanesedictionary.domain.model.items.item.WordClassItem
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap

class InitializeWordEntries(
    private val wordEntryFormUpsertValidation: WordEntryFormUpsertValidation,
    private val wordClassFetcher: WordClassFetcher
) {
    val kanjiPool = listOf(
        '日', '一', '国', '人', '年', '大', '十', '二', '本', '中',
        '長', '出', '三', '時', '行', '見', '月', '後', '前', '生',
        '五', '間', '上', '東', '四', '今', '金', '九', '入', '学',
        '高', '円', '子', '外', '八', '六', '下', '来', '気', '小',
        '七', '山', '話', '女', '北', '午', '百', '書', '先', '名',
        '川', '千', '水', '半', '男', '西', '電', '校', '語', '土',
        '木', '聞', '食', '車', '何', '南', '万', '毎', '白', '天',
        '母', '火', '右', '読', '友', '左', '休', '父', '雨', '駅',
        '青', '店', '曜', '飲', '新', '買', '古', '会', '海', '絵',
        '牛', '犬', '魚', '鳥', '肉', '花', '春', '夏', '秋', '冬'
    )

    suspend fun generateWordEntries () {
        val wordClassResult = wordClassFetcher.fetchWordClassItem("VERB", "RU_VERB")
        val wordClassItem: WordClassItem = when(wordClassResult) {
            is DatabaseResult.Success -> wordClassResult.value
            else -> error("Expected word class to be found, but got: $wordClassResult")
        }

        generateUniqueKanjiCombinations(2, 50).forEach {
            val formItemManager = FormItemManager()
            val wordEntryFormData = buildWordEntryForm(
                wordClassItem,
                it,
                listOf(),
                listOf(
                    SectionForm(
                        "meeo",
                        listOf("にほんご", "にほん", "にほ", "に"),
                        listOf()
                    )
                ),
                formItemManager
            )

            wordEntryFormUpsertValidation.wordEntryForm(wordEntryFormData, emptyList())
        }
    }

    fun generateUniqueKanjiCombinations(length: Int, limit: Int? = null): List<String> {
        val combinations = mutableSetOf<String>()
        while (combinations.size < (limit ?: Int.MAX_VALUE)) {
            val combo = "日" + kanjiPool.shuffled().take(length).joinToString("")
            combinations.add(combo)
            if (limit != null && combinations.size >= limit) break
        }
        return combinations.toList()
    }

    internal fun buildWordEntryForm(
        wordClassItem: WordClassItem,
        primaryText: String,
        noteList: List<String>,
        sectionFromList: List<SectionForm>,
        formItemManager: FormItemManager
    ): WordEntryFormData {

        var index = 0
        val sectionMap = sectionFromList.associate { sectionForm ->
            index++ to WordSectionFormData(
                formItemManager.createNewTextItem(InputTextType.MEANING, sectionForm.meaning, formItemManager.createItemProperties()),
                getListToMap(sectionForm.kanaList, InputTextType.KANA, formItemManager),
                getListToMap(sectionForm.noteList, InputTextType.SECTION_NOTE_DESCRIPTION, formItemManager)
            )
        }.toPersistentMap()

        return WordEntryFormData(
            wordClassItem,
            formItemManager.createNewTextItem(InputTextType.PRIMARY_TEXT, primaryText, formItemManager.createItemProperties()),
            getListToMap(noteList, InputTextType.DICTIONARY_NOTE_DESCRIPTION, formItemManager),
            sectionMap
        )
    }

    private fun getListToMap(list: List<String>, inputTextType: InputTextType, formItemManager: FormItemManager): PersistentMap<String, TextItem>{
        return list.associate { text ->
            val item = formItemManager.createNewTextItem(inputTextType, text, formItemManager.createItemProperties())
            item.itemProperties.getIdentifier() to item
        }.toPersistentMap()
    }

    internal data class SectionForm(val meaning: String, val kanaList: List<String>, val noteList: List<String>)
}