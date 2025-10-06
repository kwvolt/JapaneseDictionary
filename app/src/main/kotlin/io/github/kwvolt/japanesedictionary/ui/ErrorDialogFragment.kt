package io.github.kwvolt.japanesedictionary.ui

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import io.github.kwvolt.japanesedictionary.R

import io.github.kwvolt.japanesedictionary.databinding.ErrorScrollableViewBinding
import kotlin.system.exitProcess


class ErrorDialogFragment : DialogFragment() {
    private var _binding: ErrorScrollableViewBinding? = null
    private val binding: ErrorScrollableViewBinding get() = _binding ?: throw IllegalStateException(getString(R.string.binding_null_error))


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = ErrorScrollableViewBinding.inflate(layoutInflater)
        val errorMessage = requireArguments().getString(ARG_MESSAGE) ?: ""
        val errorStackTrace = requireArguments().getString(ARG_STACKTRACE) ?: ""
        val message = if (errorMessage.isNotBlank()) {
            "$errorMessage\n\n$errorStackTrace"
        } else {
            errorStackTrace
        }

        binding.errorMessage.text = message

        val errorDialog: AlertDialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.error))
            .setView(binding.root)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _, _ ->
                requireActivity().finishAffinity()
                exitProcess(0)
            }
            .setNeutralButton(R.string.copy, null)
            .create()

        // set copy button to copy the stack trace without dismiss
        errorDialog.setOnShowListener {
            val neutralButton = errorDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            neutralButton.setOnClickListener {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Error", errorStackTrace)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
            }
        }

        return errorDialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MESSAGE = "ERROR_DIALOG_ARG_MESSAGE"
        private const val ARG_STACKTRACE = "ERROR_DIALOG_ARG_STACK_TRACE"
        private const val ERROR_DIALOG_TAG = "ERROR_DIALOG_TAG"

        fun show(
            fragmentManager: FragmentManager,
            errorThrowable: Throwable,
            errorMessage: String? = null
        ) {
            if (fragmentManager.findFragmentByTag(ERROR_DIALOG_TAG) == null) {
                val fragment = ErrorDialogFragment()
                val stackTraceString = Log.getStackTraceString(errorThrowable)
                val args = Bundle().apply {
                    putString(ARG_MESSAGE, errorMessage ?: "")
                    putString(ARG_STACKTRACE, stackTraceString)
                }

                fragment.arguments = args
                fragment.show(fragmentManager, ERROR_DIALOG_TAG)
            }
        }
    }
}