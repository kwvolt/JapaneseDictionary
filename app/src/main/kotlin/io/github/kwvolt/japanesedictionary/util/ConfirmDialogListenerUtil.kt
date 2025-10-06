package io.github.kwvolt.japanesedictionary.util

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import io.github.kwvolt.japanesedictionary.ui.ConfirmDialogFragment

object ConfirmDialogListenerUtil {
    fun getResult(parentFragmentManager: FragmentManager, viewLifecycleOwner: LifecycleOwner, requestKey: String, block: (Bundle)->Unit){
        parentFragmentManager.setFragmentResultListener(
            requestKey, viewLifecycleOwner) { _,  bundle: Bundle ->
            val confirmed = bundle.getBoolean(ConfirmDialogFragment.RESULT_DISCARD_CONFIRMED, false)
            if (confirmed) {
                block(bundle)
            }
        }
    }
}

fun Fragment.setConfirmDialogListener(requestKey: String,block: (Bundle)->Unit){
    ConfirmDialogListenerUtil.getResult(
        parentFragmentManager, viewLifecycleOwner, requestKey,
        block = block
    )
}