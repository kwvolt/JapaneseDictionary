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
import io.github.kwvolt.japanesedictionary.presentation.addupdate.AddUpdateAdapter
import io.github.kwvolt.japanesedictionary.presentation.addupdate.AddUpdateViewModelCallBack
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.ButtonAction
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.EntryLabelItem
import io.github.kwvolt.japanesedictionary.presentation.addupdate.FakeAddUpdateViewModelFactory
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.InputTextType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.LabelType
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.NamedItem
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.items.WordClassItem

class AddUpdateRecyclerViewFragment: Fragment() {
    private lateinit var addUpdateViewModel: AddUpdateViewModel
    private var _binding: AddUpdateLayoutBinding? = null
    private val binding: AddUpdateLayoutBinding get() = _binding!!

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

        val listeners: AddUpdateViewModelCallBack = ViewModelCallBack(addUpdateViewModel)

        val addUpdateAdapter: AddUpdateAdapter = AddUpdateAdapter(listeners)

        binding.addUpdateList.layoutManager = LinearLayoutManager(this.context)
        binding.addUpdateList.adapter = addUpdateAdapter


        addUpdateViewModel.formItems.observe(viewLifecycleOwner, Observer { updatedList ->
            // Update RecyclerView's adapter with the new list
            println(updatedList)
            addUpdateAdapter.submitList(updatedList)

        })

        addUpdateViewModel.loadWordClassSpinner()
        addUpdateViewModel.loadItems()

        // Set up UI components like buttons or text views
        val confirmButton: Button = binding.addUpdateLayoutConfirmButton
        confirmButton.setOnClickListener {
            println("testing")
       }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the binding when the view is destroyed to prevent memory leaks
        _binding = null
    }

    class ViewModelCallBack(private val addUpdateViewModel: AddUpdateViewModel): AddUpdateViewModelCallBack {
        override fun buttonClickedHandler(button: ButtonAction, position: Int) {
            when (button){
                is ButtonAction.AddChild -> addUpdateViewModel.addChildTextItemClicked(button, position)
                is ButtonAction.AddItem -> addUpdateViewModel.addTextItemClicked(button, position)
                is ButtonAction.AddSection -> addUpdateViewModel.addItemEntryClicked(position)
            }
        }

        override fun removeItemAtPosition(position: Int) {
            addUpdateViewModel.removeItemAtPosition(position)
        }

        override fun entryRemoveItems(entryLabelItem: EntryLabelItem, position: Int) {
            addUpdateViewModel.entryRemoveItems(entryLabelItem, position)
        }

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

        override fun updateInputTextValue(inputTextItem: InputTextItem, inputText: String, position: Int) {
            addUpdateViewModel.updateInputTextValue(inputTextItem, inputText, position)
        }

        override fun updateEntryIndexIfNeeded(entryLabelItem: EntryLabelItem, position: Int) {
            addUpdateViewModel.updateEntryIndexIfNeeded(entryLabelItem, position)
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

        override fun getInputTextValue(inputTextItem: InputTextItem): String {
            return  addUpdateViewModel.getInputTextValue(inputTextItem)
        }

        override fun getInputTextType(inputTextItem: InputTextItem): InputTextType {
            return addUpdateViewModel.getInputTextType(inputTextItem)
        }

        override fun getLabelType(labelItem: LabelItem): LabelType {
            return addUpdateViewModel.getLabelType(labelItem)
        }

        override fun getWidgetName(namedItem: NamedItem): String {
            return addUpdateViewModel.getWidgetName(namedItem)
        }
    }
}