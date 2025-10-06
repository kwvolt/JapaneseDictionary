package io.github.kwvolt.japanesedictionary.util

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.ui.ConfirmDialogFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarylookup.DictionaryLookupFragmentDirections

object PopupMenuUtil {
    fun setupPopupMenu(menuResourceId: Int, context: Context, view: View, listener: (MenuItem)->Boolean){
        val wrapper = ContextThemeWrapper(context, R.style.CustomPopupMenu)
        val popup = PopupMenu(wrapper, view)
        popup.menuInflater.inflate(menuResourceId, popup.menu)
        popup.menu.setGroupDividerEnabled(true)
        popup.setOnMenuItemClickListener { menuItem ->
            listener(menuItem)
        }
        popup.show()
    }
}