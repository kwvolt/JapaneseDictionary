package io.github.kwvolt.japanesedictionary.util.icon

import android.content.Context
import io.github.kwvolt.japanesedictionary.R

object FilterUtil {
    fun setFilterIcon(context: Context, isActive: Boolean, iconWrapper: ToggleIconWrapper){
        if (isActive) {
            ToggleIconUtil.swapIcon(context, R.drawable.filter_filled, iconWrapper)
        } else {
            ToggleIconUtil.swapIcon(context, R.drawable.filter_outlined, iconWrapper)
        }
    }
}

fun ToggleIconWrapper.toggleFilter(context: Context, isActive: Boolean){
    FilterUtil.setFilterIcon(context, isActive, this)
}