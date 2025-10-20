package io.github.kwvolt.japanesedictionary.util

import android.content.Context
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.domain.data.service.wordclass.WordClassUpsert
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.WordClassItem

object DictionaryDisplayUtil {
    fun getWordClassDisplayText(wordClassItem: WordClassItem): Pair<String?, String?>{
        val mainText: String? = wordClassItem.chosenMainClass.displayText.takeIf {
            wordClassItem.chosenMainClass.idName != WordClassUpsert.DEFAULT_ID_NAME
        }
        val subText: String? = wordClassItem.chosenSubClass.displayText.takeIf {
            wordClassItem.chosenSubClass.idName != WordClassUpsert.DEFAULT_ID_NAME
        }
        return Pair(mainText, subText)
    }

    fun displayWordClass(mainClass: String ?, subClass: String?, context: Context): String?{
        val text: String? = when {
            mainClass != null && subClass != null -> {
                context.getString(
                    R.string.dwp_word_class_display,
                    mainClass,
                    subClass
                )
            }
            else -> mainClass ?: subClass
        }
        return text
    }

    fun displayOrHideWordClass(text: String?, textView: TextView){
        if (text.isNullOrEmpty()) {
            textView.visibility = GONE
        } else {
            textView.text = text
            textView.visibility = VISIBLE
        }
    }

    fun displayKanaText(kanaTextView: TextView, kanaList: List<String>, firstIndent: Int, secondIndent: Int, context: Context) {
        if (kanaList.isNotEmpty()) {
            val kanaText: String =
                kanaList.joinToString(context.getString(R.string.dwp_kana_separator))
            kanaTextView.text = ListSpanUtil.applyLeadingMargin(kanaText, firstIndent, secondIndent)
        } else {
            kanaTextView.visibility = GONE
        }
    }
}