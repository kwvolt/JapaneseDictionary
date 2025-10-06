package io.github.kwvolt.japanesedictionary.util.icon

import android.content.Context
import io.github.kwvolt.japanesedictionary.R

object BookmarkUtil {
    fun setBookmarkIcon(context: Context, isBookmarked: Boolean, iconWrapper: ToggleIconWrapper){
        if (isBookmarked) {
            ToggleIconUtil.swapIcon(context, R.drawable.star, iconWrapper, R.color.bookmarked)
        } else {
            ToggleIconUtil.swapIcon(context, R.drawable.star_border, iconWrapper, R.color.md_theme_onPrimary)
        }
    }
}


fun ToggleIconWrapper.toggleBookmark(context: Context, isBookmarked: Boolean){
    BookmarkUtil.setBookmarkIcon(context, isBookmarked, this)
}