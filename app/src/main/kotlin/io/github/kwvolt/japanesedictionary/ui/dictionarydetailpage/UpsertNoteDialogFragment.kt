package io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DwpNoteEditDialogBinding
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.items.item.TextItem
import io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage.DictionaryDetailPageViewModel
import io.github.kwvolt.japanesedictionary.presentation.dictionarydetailpage.NoteUpsertResult
import io.github.kwvolt.japanesedictionary.presentation.mainactivity.LoadingViewModel
import io.github.kwvolt.japanesedictionary.ui.ConfirmDialogFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.tabs.worddefinition.WordDefinitionTabFragment
import io.github.kwvolt.japanesedictionary.ui.model.ScreenStateUnknownError
import kotlinx.coroutines.launch

class UpsertNoteDialogFragment : DialogFragment() {
    private var _binding: DwpNoteEditDialogBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException(getString(R.string.binding_null_error))
    private val loadingViewModel: LoadingViewModel by activityViewModels()
    private lateinit var noteItem: TextItem

    // args
    private val noteId: Long? by lazy { arguments?.takeIf { it.containsKey(ARG_NOTE_ID) }?.getLong(ARG_NOTE_ID) }
    private val noteContainerId: Int by lazy {
        requireArguments().getInt(ARG_RESULT_NOTE_CONTAINER_ID, WordDefinitionTabFragment.GENERAL_NOTE_CONTAINER_ID)
    }
    // shared view model
    private val parentFragmentTyped: DictionaryDetailPageViewFragment by lazy {
        val parent = requireParentFragment()
        parent as? DictionaryDetailPageViewFragment
            ?: throw IllegalStateException("Parent fragment must be DictionaryDetailPageViewFragment")
    }
    private val factory: ViewModelProvider.Factory by lazy {
        parentFragmentTyped.dictionaryDetailPageViewModelFactory
    }
    private val viewModel: DictionaryDetailPageViewModel by viewModels(
        ownerProducer = { parentFragmentTyped },
        factoryProducer = { factory }
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DwpNoteEditDialogBinding.inflate(layoutInflater)
        noteItem = viewModel.getOrGenerateNewNoteItem(noteId, noteContainerId)
        binding.dwpDialogEditNote.setText(noteItem.inputTextValue)

        val isUpdate:Boolean = requireArguments().getBoolean(ARG_IS_UPDATE)

        val alertDialog: AlertDialog =  AlertDialog.Builder(requireContext())
            .setTitle(getString(getTitle(isUpdate)))
            .setView(binding.root)
            .setPositiveButton(R.string.update, null)
            .setNegativeButton(R.string.cancel, null)
            .create()

        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        return alertDialog
    }

    override fun onStart() {
        super.onStart()
        setConfirmDialogListener()
        collectNoteUpsertEvents()
        addPositiveButtonListener()
        addNegativeButtonListener()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addNegativeButtonListener(){
        val negativeButton: Button? = (dialog as? AlertDialog)?.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton?.apply {
            setOnClickListener {
                val textChanged = binding.dwpDialogEditNote.text.toString() != noteItem.inputTextValue
                if (textChanged) {
                    ConfirmDialogFragment.showDiscard(childFragmentManager, CONFIRM_DIALOG_REQUEST_KEY)
                } else {
                    if(isAdded) dismiss()
                }
            }
        }
    }

    private fun addPositiveButtonListener(){
        val positiveButton: Button? = (dialog as? AlertDialog)?.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton?.apply {
            isEnabled = binding.dwpDialogEditNote.text.toString().let {
                it.isNotBlank() && it != noteItem.inputTextValue
            }

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

    private fun setConfirmDialogListener(){
        childFragmentManager.setFragmentResultListener(
            CONFIRM_DIALOG_REQUEST_KEY,
            this
        ) { _, bundle ->
            val confirmed = bundle.getBoolean(ConfirmDialogFragment.RESULT_DISCARD_CONFIRMED)
            if (confirmed) { if(isAdded) dismiss()}
        }
    }

    private fun collectNoteUpsertEvents() {
        lifecycleScope.launch {
            viewModel.noteUpsertEvents.collect { result ->
                when (result) {
                    is NoteUpsertResult.Success -> {
                        val resultBundle = Bundle().apply {
                            putBoolean(RESULT_CONFIRMED, true)
                            putInt(ARG_RESULT_NOTE_CONTAINER_ID, noteContainerId)
                        }
                        parentFragmentManager.setFragmentResult(RESULT_KEY, resultBundle)
                        if(isAdded) dismiss()
                    }
                    is NoteUpsertResult.ValidationError -> {
                        binding.dwpDialogEditNote.error = result.message
                    }
                    is NoteUpsertResult.UnknownError -> {
                        loadingViewModel.showWarning(screenStateUnknownError = ScreenStateUnknownError(result.exception))
                    }
                }
            }
        }
    }

    @StringRes
    private fun getTitle(isUpdate: Boolean): Int{
        return if(isUpdate){
            R.string.dwp_edit_note_dialog_title_update
        }
        else {
            R.string.dwp_edit_note_dialog_title_insert
        }
    }


    companion object {
        const val RESULT_CONFIRMED = "UPDATE_NOTE_RESULT_CONFIRMED"
        const val ARG_RESULT_NOTE_CONTAINER_ID = "UPDATE_NOTE_ARG_RESULT_NOTE_SECTION_ID"
        const val ARG_NOTE_ID = "UPDATE_NOTE_ARG_NOTE_ID"
        const val ARG_IS_UPDATE = "UPDATE_NOTE_ARG_IS_UPDATE"
        const val RESULT_KEY = "UPDATE_NOTE_RESULT_KEY"
        const val CONFIRM_DIALOG_REQUEST_KEY = "UPDATE_NOTE_CONFIRM_DIALOG_REQUEST_KEY"

        fun show(fragmentManager: FragmentManager, noteId: Long? = null, noteContainerId: Int = -1, isUpdate: Boolean, tag: String) {
            val fragment = UpsertNoteDialogFragment()
            val args = Bundle().apply {
                noteId?.let { putLong(ARG_NOTE_ID, it) }
                putInt(ARG_RESULT_NOTE_CONTAINER_ID, noteContainerId)
                putBoolean(ARG_IS_UPDATE, isUpdate)
            }
            fragment.arguments = args
            fragment.show(fragmentManager, tag)
        }
    }
}