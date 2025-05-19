package io.github.kwvolt.japanesedictionary.ui.addUpdate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.MainClassContainer
import io.github.kwvolt.japanesedictionary.domain.data.repository.word_class.SubClassContainer
import io.github.kwvolt.japanesedictionary.databinding.AddUpdateLayoutBinding
import io.github.kwvolt.japanesedictionary.presentation.addupdate.AddUpdateViewModel
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.AddUpdateAdapter
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.AddUpdateViewModelCallBack
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.presentation.addupdate.FakeAddUpdateViewModelFactory
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.NamedItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem
import io.github.kwvolt.japanesedictionary.domain.form.handler.FormSectionManager
import io.github.kwvolt.japanesedictionary.domain.form.handler.WordUiFormHandler
import io.github.kwvolt.japanesedictionary.presentation.addupdate.UiState
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.ButtonCallBack
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.EditTextCallBack
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.LabelTextCallBack
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.viewholder.WordClassCallBack

class AddUpdateRecyclerViewFragment: Fragment() {
    private lateinit var addUpdateViewModel: AddUpdateViewModel
    private var _binding: AddUpdateLayoutBinding? = null
    private val binding: AddUpdateLayoutBinding get() = _binding ?: throw IllegalStateException("Binding is null")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddUpdateLayoutBinding.inflate(inflater, container, false)

       return binding.root
   }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModelFactory = FakeAddUpdateViewModelFactory()
        addUpdateViewModel = ViewModelProvider(this, viewModelFactory)[AddUpdateViewModel::class.java]


        val wordClassListener = InitWordClassCallBack(addUpdateViewModel)
        val editTextListener = InitEditTextCallBack(addUpdateViewModel)
        val labelTextListener = InitLabelTextCallBack(addUpdateViewModel)
        val buttonListener = InitButtonCallBack(addUpdateViewModel)

        val addUpdateAdapter: AddUpdateAdapter = AddUpdateAdapter(wordClassListener, labelTextListener,editTextListener, buttonListener)


        binding.addUpdateList.layoutManager = LinearLayoutManager(this.context)
        binding.addUpdateList.adapter = addUpdateAdapter

        addUpdateViewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                UiState.Loading -> {
                    binding.loadingBackground.animate().alpha(1f).setDuration(200).start()
                    binding.loadingBackground.visibility = View.VISIBLE
                    binding.loadingProgressBar.visibility = View.VISIBLE
                }
                is UiState.Success -> addUpdateAdapter.submitList(uiState.data)
                is UiState.UnknownError -> {
                    binding.loadingBackground.animate().alpha(1f).setDuration(200).start()
                    binding.loadingBackground.visibility = View.GONE
                    binding.loadingProgressBar.visibility = View.GONE
                    // TODO: show error
                }

                is UiState.ValidationError -> TODO()
            }
        }


        val wordUiFormHandler =  WordUiFormHandler()
        val formSectionManager = FormSectionManager()
        addUpdateViewModel.loadWordClassSpinner()
        addUpdateViewModel.loadItems(formSectionManager, wordUiFormHandler)

        // Set up UI components like buttons or text views
        val confirmButton: Button = binding.addUpdateLayoutConfirmButton
        confirmButton.setOnClickListener {
            addUpdateViewModel.upsertValuesIntoDB()
       }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the binding when the view is destroyed to prevent memory leaks
        _binding = null
    }

    class InitWordClassCallBack(private val addUpdateViewModel: AddUpdateViewModel): WordClassCallBack {

        override fun updateMainClassId(
            wordClassItem: WordClassItem,
            selectionPosition: Int,
            position: Int
        ): Boolean {
            return addUpdateViewModel.updateMainClassId(wordClassItem, selectionPosition, position)
        }

        override fun updateSubClassId(
            wordClassItem: WordClassItem,
            selectionPosition: Int,
            position: Int
        ) {
            addUpdateViewModel.updateSubClassId(wordClassItem, selectionPosition, position)
        }

        override fun getMainClassListIndex(wordClassItem: WordClassItem): Int {
            return addUpdateViewModel.getMainClassListIndex(wordClassItem)
        }

        override fun getSubClassListIndex(wordClassItem: WordClassItem): Int {
            return addUpdateViewModel.getSubClassListIndex(wordClassItem)
        }

        override fun getMainClasList(): List<MainClassContainer> {
            return addUpdateViewModel.getMainClassList()
        }

        override fun getSubClassList(wordClassItem: WordClassItem): List<SubClassContainer> {
            return addUpdateViewModel.getSubClassList(wordClassItem)
        }
    }

    class InitEditTextCallBack(private val addUpdateViewModel: AddUpdateViewModel):EditTextCallBack{
        override fun updateInputTextValue(
            inputTextItem: InputTextItem,
            inputText: String,
            position: Int
        ) {
            addUpdateViewModel.updateInputTextValue(inputTextItem, inputText, position)
        }

        override fun removeItemAtPosition(inputTextItem: InputTextItem, position: Int) {
            addUpdateViewModel.removeTextItemClicked(inputTextItem, position)
        }

        override fun getInputTextValue(inputTextItem: InputTextItem): String {
            return  inputTextItem.inputTextValue
        }

        override fun getInputTextType(inputTextItem: InputTextItem): InputTextType {
            return inputTextItem.inputTextType
        }

    }

    class InitLabelTextCallBack(private val addUpdateViewModel: AddUpdateViewModel): LabelTextCallBack{
        override fun entryRemoveItems(entryLabelItem: EntryLabelItem, position: Int) {
            addUpdateViewModel.removeSectionClicked(entryLabelItem, position)
        }

        override fun updateEntryIndexIfNeeded(entryLabelItem: EntryLabelItem, position: Int) {
            addUpdateViewModel.updateEntryIndexIfNeeded(entryLabelItem, position)
        }

        override fun getLabelType(labelItem: LabelItem): LabelType {
            return labelItem.labelType
        }

        override fun getWidgetName(namedItem: NamedItem): String {
            return namedItem.getDisplayText()
        }

    }

    class InitButtonCallBack(private val addUpdateViewModel: AddUpdateViewModel): ButtonCallBack{
        override fun buttonClickedHandler(button: ButtonAction, position: Int) {
            when (button){
                is ButtonAction.AddChild -> addUpdateViewModel.addChildTextItemClicked(button, position)
                is ButtonAction.AddItem -> addUpdateViewModel.addTextItemClicked(button, position)
                is ButtonAction.AddSection -> addUpdateViewModel.addSectionClicked(position)
            }
        }

        override fun getWidgetName(namedItem: NamedItem): String {
            return namedItem.getDisplayText()
        }
    }
}