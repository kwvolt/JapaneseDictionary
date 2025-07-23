package io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DwpNoteEditDialogBinding
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.TextItem
import io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage.DictionaryDetailPageViewModel
import io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage.NoteUpsertResult
import io.github.kwvolt.japanesedictionary.presentation.mainactivity.LoadingViewModel
import io.github.kwvolt.japanesedictionary.ui.ConfirmDialogFragment
import io.github.kwvolt.japanesedictionary.ui.model.ScreenStateUnknownError
import kotlinx.coroutines.launch

class UpdateNoteDialogFragment : DialogFragment() {

    private var _binding: DwpNoteEditDialogBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException(getString(R.string.binding_null_error))

    private val loadingViewModel: LoadingViewModel by activityViewModels()

    private lateinit var noteItem: TextItem

    private val noteId: Long? by lazy { arguments?.takeIf { it.containsKey(ARG_NOTE_ID) }?.getLong(ARG_NOTE_ID) }
    private val sectionId: Int? by lazy { arguments?.takeIf { it.containsKey(ARG_NOTE_SECTION_ID) }?.getInt(ARG_NOTE_SECTION_ID) }

    private val viewModel: DictionaryDetailPageViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DwpNoteEditDialogBinding.inflate(layoutInflater)
        return AlertDialog.Builder(requireContext()).apply {
            setTitle(getString(R.string.dwp_edit_note_dialog_title))
            setView(binding.root)
            setPositiveButton(R.string.update, null) // set listener manually in onStart()
            setNegativeButton(R.string.cancel) { _, _ -> dismiss() }
        }.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        noteItem = viewModel.getOrGenerateNewNoteItem(noteId, sectionId)
        binding.dwpDialogEditNote.setText(noteItem.inputTextValue)

        parentFragmentManager.setFragmentResultListener(
            ConfirmDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val confirmed = bundle.getBoolean(ConfirmDialogFragment.RESULT_DISCARD_CONFIRMED)
            if (confirmed) dismiss()
        }
        collectNoteUpsertEvents()
    }

    override fun onStart() {
        super.onStart()
        val positiveButton = (dialog as? AlertDialog)?.getButton(AlertDialog.BUTTON_POSITIVE)

        positiveButton?.apply {
            isEnabled = binding.dwpDialogEditNote.text.toString()
                .takeIf { it.isNotBlank() && it != noteItem.inputTextValue }
                ?.let { true } ?: false

            setOnClickListener {
                val newNoteText = binding.dwpDialogEditNote.text.toString()
                binding.dwpDialogEditNote.error = null
                viewModel.upsertValidateNote(noteItem, newNoteText)
            }
        }

        binding.dwpDialogEditNote.doAfterTextChanged { text ->
            positiveButton?.isEnabled = !text.isNullOrBlank() && text.toString() != noteItem.inputTextValue
            binding.dwpDialogEditNote.error = null
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        val textChanged = binding.dwpDialogEditNote.text.toString() != noteItem.inputTextValue
        if (textChanged) {
            ConfirmDialogFragment.showDiscard(parentFragmentManager, CONFIRM_DIALOG_REQUEST_KEY)
        } else {
            super.onCancel(dialog)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun collectNoteUpsertEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.noteUpsertEvents.collect { result ->
                when (result) {
                    is NoteUpsertResult.Success -> {
                        parentFragmentManager.setFragmentResult(RESULT_KEY, Bundle.EMPTY)
                        dismiss()
                    }
                    is NoteUpsertResult.ValidationError -> {
                        binding.dwpDialogEditNote.error = result.message
                    }
                    is NoteUpsertResult.UnknownError -> {
                        loadingViewModel.showWarning(screenStateUnknownError = ScreenStateUnknownError(result.exception, result.message))
                    }
                }
            }
        }
    }


    companion object {
        const val ARG_NOTE_SECTION_ID = "SectionID"
        const val ARG_NOTE_ID = "NoteId"

        const val RESULT_KEY = "UpdateNoteRequestKey"
        const val CONFIRM_DIALOG_REQUEST_KEY = "UpdateNoteConfirmDialogKey"

        fun newInstance(noteId: Long? = null, sectionId: Int? = null): UpdateNoteDialogFragment {
            val fragment = UpdateNoteDialogFragment()
            val args = Bundle().apply {
                noteId?.let { putLong(ARG_NOTE_ID, it) }
                sectionId?.let { putInt(ARG_NOTE_SECTION_ID, it) }
            }
            fragment.arguments = args
            return fragment
        }
    }
}