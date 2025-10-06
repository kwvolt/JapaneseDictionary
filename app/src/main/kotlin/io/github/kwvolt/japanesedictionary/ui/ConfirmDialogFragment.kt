package io.github.kwvolt.japanesedictionary.ui

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import io.github.kwvolt.japanesedictionary.R

class ConfirmDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogMessage = requireArguments().getInt(ARG_MESSAGE)
        val dialogTitle = requireArguments().getInt(ARG_TITLE)
        return AlertDialog.Builder(requireContext())
            .setTitle(getString(dialogTitle))
            .setMessage(getString(dialogMessage))
            .setPositiveButton(R.string.proceed) { _, _ ->
                val requestKey = requireArguments().getString(ARG_REQUEST_KEY)
                val extraArgs = requireArguments().getBundle(ARG_EXTRAS) ?: Bundle()
                val resultBundle = Bundle(extraArgs).apply {
                    putBoolean(RESULT_DISCARD_CONFIRMED, true)
                }
                if (requestKey != null) {
                    parentFragmentManager.setFragmentResult(requestKey, resultBundle)
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                val requestKey = requireArguments().getString(ARG_REQUEST_KEY)
                val resultBundle = Bundle().apply {
                    putBoolean(RESULT_DISCARD_CONFIRMED, false)
                }
                if (requestKey != null) {
                    parentFragmentManager.setFragmentResult(requestKey, resultBundle)
                }
            }
            .create()
    }

    companion object {
        const val ARG_REQUEST_KEY = "CONFIRM_DIALOG_ARG_REQUEST_KEY"
        const val RESULT_DISCARD_CONFIRMED = "CONFIRM_DIALOG_RESULT_DISCARD_CONFIRMED"
        const val ARG_TITLE: String = "CONFIRM_DIALOG_ARG_TITLE"
        const val ARG_MESSAGE: String = "CONFIRM_DIALOG_ARG_MESSAGE"
        const val ARG_EXTRAS: String = "CONFIRM_DIALOG_ARG_EXTRAS"
        const val CONFIRM_DIALOG_TAG: String = "CONFIRM_DIALOG_CONFIRM_DIALOG_TAG"

        fun show(
            fragmentManager: FragmentManager,
            dialogTitle: Int,
            dialogMessage: Int,
            requestKey: String,
            bundle: Bundle? = null
        ) {
            if (fragmentManager.findFragmentByTag(CONFIRM_DIALOG_TAG) == null) {
                val fragment = ConfirmDialogFragment()
                val args = Bundle().apply {
                    putInt(ARG_TITLE, dialogTitle)
                    putInt(ARG_MESSAGE, dialogMessage)
                    putString(ARG_REQUEST_KEY, requestKey)
                    bundle?.let { putBundle(ARG_EXTRAS, it) }
                }
                fragment.arguments = args
                fragment.show(fragmentManager, CONFIRM_DIALOG_TAG)
            }
        }

        fun showDiscard(
            fragmentManager: FragmentManager,
            requestKey: String,
            bundle: Bundle? = null){
            show(
                fragmentManager,
                R.string.unsaved_changes_title,
                R.string.unsaved_changes_message,
                requestKey,
                bundle
            )
        }
    }
}