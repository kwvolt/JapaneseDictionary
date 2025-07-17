package io.github.kwvolt.japanesedictionary.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import io.github.kwvolt.japanesedictionary.R

class ConfirmDiscardDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.unsaved_changes_title)
            .setMessage(R.string.unsaved_changes_message)
            .setPositiveButton(R.string.proceed) { _, _ ->
                parentFragmentManager.setFragmentResult(
                    REQUEST_KEY,
                    bundleOf(RESULT_DISCARD_CONFIRMED to true)
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
    }

    companion object {
        const val REQUEST_KEY = "confirm_discard_request"
        const val RESULT_DISCARD_CONFIRMED = "discard_confirmed"

        fun show(fragmentManager: FragmentManager) {
            if (fragmentManager.findFragmentByTag("ConfirmDiscardDialog") == null) {
                ConfirmDiscardDialogFragment().show(fragmentManager, "ConfirmDiscardDialog")
            }
        }
    }
}