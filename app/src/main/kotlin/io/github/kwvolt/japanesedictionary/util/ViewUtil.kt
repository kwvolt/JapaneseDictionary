package io.github.kwvolt.japanesedictionary.util

import android.view.View
import android.view.View.GONE
import androidx.cardview.widget.CardView
import androidx.core.view.doOnPreDraw

object ViewUtil {
    fun setCardBackgroundAlpha(cardView: CardView, alpha: Int) {
        cardView.setCardBackgroundColor(
            cardView.cardBackgroundColor.withAlpha(alpha)
        )
    }
    const val ALPHA_75_PERCENT: Int = 192

    fun setNavigationTransition(view: View){
        view.visibility = GONE
        view.doOnPreDraw {
            view.visibility = View.VISIBLE
            view.alpha = 0f
            view.animate().alpha(1f).setDuration(300).start()
        }

    }
}