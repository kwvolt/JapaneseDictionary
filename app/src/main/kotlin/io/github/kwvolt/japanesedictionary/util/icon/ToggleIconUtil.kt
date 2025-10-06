package io.github.kwvolt.japanesedictionary.util.icon

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.MenuItem
import androidx.core.content.ContextCompat.getColor
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.button.MaterialButton

object ToggleIconUtil {
    fun swapIcon(context: Context, drawable: Int, iconWrapper: ToggleIconWrapper, color: Int? = null){
        val icon:Drawable? = getDrawable(context, drawable)?.mutate()
        icon?.let{
            val wrapped: Drawable = DrawableCompat.wrap(icon)
            color?.let{DrawableCompat.setTint(wrapped, getColor(context, color))}
            iconWrapper.setIcon(wrapped)
        }
    }
}

interface ToggleIconWrapper {
    fun setIcon(drawable: Drawable?)
}

class MenuItemIcon(private val menuItem: MenuItem): ToggleIconWrapper {
    override fun setIcon(drawable: Drawable?) {
        menuItem.icon = drawable
    }
}

class MaterialButtonIcon(private val materialButton: MaterialButton): ToggleIconWrapper {
    override fun setIcon(drawable: Drawable?) {
        materialButton.icon = drawable
    }
}