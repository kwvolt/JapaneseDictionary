package io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DwpNoteEditDialogBinding
import io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage.DictionaryDetailPageViewModel
import io.github.kwvolt.japanesedictionary.ui.ConfirmDiscardDialogFragment

class UpdateNoteDialogFragment : DialogFragment() {

    private var _binding: DwpNoteEditDialogBinding? = null
    private val binding: DwpNoteEditDialogBinding get() = _binding?: throw IllegalStateException(getString(R.string.binding_null_error))

    private val noteText: String? by lazy {
        arguments?.getString(ARG_NOTE_TEXT)
    }

    private val noteId: Long? by lazy {
        arguments?.getLong(ARG_NOTE_ID)
    }

    private val viewModel: DictionaryDetailPageViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onStart() {
        super.onStart()

        val alertDialog = dialog as? AlertDialog
        alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            val noteText = binding.dwpDialogEditNote.text.toString()

            viewModel.validateNote()

            if (noteText.isBlank()) {
                binding.dwpDialogEditNote.error = getString(R.string.validation_empty_note)
                return@setOnClickListener // Don’t dismiss
            }

            // If valid, send result and dismiss
            val result = Bundle().apply {
                putString(RESULT_NOTE_TEXT, noteText)
                noteId?.let { putLong(RESULT_NOTE_ID, it) }
            }
            parentFragmentManager.setFragmentResult(RESULT_KEY, result)
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DwpNoteEditDialogBinding.inflate(layoutInflater)

        noteText?.let { binding.dwpDialogEditNote.setText(it) }

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.dwp_edit_note_dialog_title))
            .setView(binding.root)
            .setPositiveButton(R.string.update, null)
            .setNegativeButton(R.string.cancel, null)

        return builder.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            ConfirmDiscardDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val confirmed = bundle.getBoolean(ConfirmDiscardDialogFragment.RESULT_DISCARD_CONFIRMED)
            if (confirmed) dismiss()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        if (binding.dwpDialogEditNote.text.toString() != noteText.orEmpty()) {
            ConfirmDiscardDialogFragment.show(parentFragmentManager)
        } else {
            super.onCancel(dialog)
        }
    }

    companion object {
        const val ARG_NOTE_TEXT = "NoteText"
        const val ARG_NOTE_ID = "NoteId"
        const val RESULT_KEY = "UpdateNote"
        const val RESULT_NOTE_TEXT = "NoteText"
        const val RESULT_NOTE_ID = "NoteId"

        fun newInstance(noteText: String? = null, noteId: Long? = null): UpdateNoteDialogFragment {
            val fragment = UpdateNoteDialogFragment()
            val args = Bundle().apply {
                noteText?.let { putString(ARG_NOTE_TEXT, it) }
                noteId?.let { putLong(ARG_NOTE_ID, it) }
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}