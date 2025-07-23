package io.github.kwvolt.japanesedictionary.util

import android.text.SpannableString
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.widget.TextView

object ListSpanUtil {
    fun calculateIndent(textView: TextView, prefix: String): Int{
        val indent = textView.paint.measureText(prefix).toInt()
        return indent

    }

    fun applyLeadingMargin(content: String, firstIndent: Int, restIndent: Int): SpannableString {
        val spannable = SpannableString(content)
        spannable.setSpan(
            LeadingMarginSpan.Standard(firstIndent, restIndent),
            0,
            spannable.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }
}