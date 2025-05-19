package io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter

import android.util.Log
import androidx.viewbinding.ViewBinding

internal fun getResourceString(binding: ViewBinding, name: String, resource: Int): String {
    return try {
        binding.root.context.getString(resource, name)
    } catch (e: Exception) {
        Log.e("ResourceUtils", "Error fetching string resource $resource with name $name", e)
        name // Fallback to name if resource fetching fails
    }
}