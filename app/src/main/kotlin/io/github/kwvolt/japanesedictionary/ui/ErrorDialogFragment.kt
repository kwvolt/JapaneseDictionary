package io.github.kwvolt.japanesedictionary.ui

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
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

        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.error))
            .setView(binding.root)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _, _ ->
                requireActivity().finishAffinity()
                exitProcess(0)
            }
            .setNeutralButton(R.string.copy) { _, _ ->
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Error", message)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
            }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MESSAGE = "Message"
        private const val ARG_STACKTRACE = "StackTrace"
        private const val ERROR_DIALOG_TAG = "ErrorDialog"

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