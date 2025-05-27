package io.github.kwvolt.japanesedictionary.ui.addUpdate

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.Lifecycle
import com.google.android.material.snackbar.Snackbar
import io.github.kwvolt.japanesedictionary.DatabaseProvider
import io.github.kwvolt.japanesedictionary.databinding.AddUpdateLayoutBinding
import io.github.kwvolt.japanesedictionary.presentation.addupdate.AddUpdateViewModel
import io.github.kwvolt.japanesedictionary.presentation.addupdate.wordentryadapter.AddUpdateAdapter
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.FormSectionManager
import io.github.kwvolt.japanesedictionary.domain.form.addUpdate.handler.WordUiFormHandler
import io.github.kwvolt.japanesedictionary.presentation.addupdate.AddUpdateViewModelFactory
import io.github.kwvolt.japanesedictionary.presentation.addupdate.UiState
import androidx.core.view.isVisible
import io.github.kwvolt.japanesedictionary.R
import androidx.navigation.fragment.findNavController

class AddUpdateRecyclerViewFragment: Fragment() {
    private lateinit var addUpdateViewModel: AddUpdateViewModel
    private var _binding: AddUpdateLayoutBinding? = null
    private val binding: AddUpdateLayoutBinding get() = _binding ?: throw IllegalStateException("Binding is null")
    private var undoMenuItem: MenuItem? = null
    private var redoMenuItem: MenuItem? = null

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

        val application = requireActivity().application
        val viewModelFactory = AddUpdateViewModelFactory(application, DatabaseProvider(application))
        addUpdateViewModel = ViewModelProvider(this, viewModelFactory)[AddUpdateViewModel::class.java]


        val wordClassListener = InitWordClassCallBack(addUpdateViewModel)
        val editTextListener = InitEditTextCallBack(addUpdateViewModel)
        val labelTextListener = InitLabelTextCallBack(addUpdateViewModel)
        val buttonListener = InitButtonCallBack(addUpdateViewModel)

        val addUpdateAdapter = AddUpdateAdapter(wordClassListener, labelTextListener,editTextListener, buttonListener)

        binding.addUpdateList.layoutManager = LinearLayoutManager(this.context)
        binding.addUpdateList.adapter = addUpdateAdapter

        val wordUiFormHandler =  WordUiFormHandler()
        val formSectionManager = FormSectionManager()
        //addUpdateViewModel.loadWordClassSpinner()
        addUpdateViewModel.loadItems(formSectionManager, wordUiFormHandler)

        addUpdateViewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                UiState.Loading -> {
                    binding.loadingBackground.apply {
                        animate().alpha(1f).setDuration(200).start()
                        visibility = View.VISIBLE
                    }
                    binding.loadingProgressBar.visibility = View.VISIBLE
                }
                is UiState.Success -> {
                    if(binding.loadingProgressBar.isVisible){
                        binding.loadingBackground.apply {
                            animate().alpha(1f).setDuration(200).start()
                            visibility = View.GONE
                        }
                        binding.loadingProgressBar.visibility = View.GONE
                    }
                    addUpdateAdapter.submitList(uiState.data)
                }

                is UiState.UnknownError -> {
                    binding.loadingBackground.apply {
                        animate().alpha(1f).setDuration(200).start()
                        visibility = View.VISIBLE
                    }
                    binding.loadingProgressBar.visibility = View.VISIBLE
                    // Show a user-friendly error message
                    Snackbar.make(binding.root, "An error occurred. Please try again.", Snackbar.LENGTH_SHORT).show()
                }
                is UiState.ValidationError -> {
                    // Handle validation errors (e.g., display a Toast or Snackbar)
                    Snackbar.make(binding.root, "Validation error occurred.", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        val toolbar = binding.formMenu
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            if (!findNavController().navigateUp()) {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        // Set up UI components like buttons or text views
        val confirmButton: Button = binding.addUpdateLayoutConfirmButton
        confirmButton.setOnClickListener {
            addUpdateViewModel.upsertValuesIntoDB()

       }

        // Set up the menu
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.upsert_form_menu, menu)
                undoMenuItem = menu.findItem(R.id.action_undo)
                redoMenuItem = menu.findItem(R.id.action_redo)

                undoMenuItem?.isEnabled = false
                redoMenuItem?.isEnabled = false
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    // added guards to prevent initial misfire
                    R.id.action_redo -> {
                        addUpdateViewModel.redo()
                        true
                    }
                    R.id.action_undo -> {
                        addUpdateViewModel.undo()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        addUpdateViewModel.canUndo.observe(viewLifecycleOwner) { canUndo ->
            undoMenuItem?.isEnabled = canUndo
        }

        addUpdateViewModel.canRedo.observe(viewLifecycleOwner) { canRedo ->
            redoMenuItem?.isEnabled = canRedo
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up the binding when the view is destroyed to prevent memory leaks
        _binding = null
    }
}