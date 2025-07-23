package io.github.kwvolt.japanesedictionary

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.kwvolt.japanesedictionary.databinding.ActivityMainBinding
import io.github.kwvolt.japanesedictionary.presentation.mainactivity.LoadingViewModel
import io.github.kwvolt.japanesedictionary.ui.ErrorDialogFragment
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.DictionaryDetailPageViewFragment
import io.github.kwvolt.japanesedictionary.ui.model.ActivityMainScreenState
import io.github.kwvolt.japanesedictionary.ui.model.ScreenStateUnknownError
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding ?: throw IllegalStateException("Binding is null")

    private val loadingViewModel: LoadingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observe StateFlow
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                loadingViewModel.uiState.collect { state: ActivityMainScreenState ->
                    when {
                        state.isLoading -> showLoading()
                        state.screenStateUnknownError != null -> {
                            showLoading()
                            state.screenStateUnknownError?.let { error ->
                                ErrorDialogFragment.show(supportFragmentManager, error.throwable, error.message)
                            }
                        }
                        else -> {
                            hideLoading()
                        }
                    }
                }
            }
        }


        if (savedInstanceState == null) {
            val fragment = DictionaryDetailPageViewFragment()

            supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, fragment)  // Use the ID from the binding
                .commit()
        }
    }

    private fun showLoading(){
        binding.loadingBackground.apply {
            animate().alpha(1f).setDuration(200).start()
            visibility = View.VISIBLE
        }
        binding.loadingProgressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingBackground.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                binding.loadingBackground.visibility = View.GONE
                binding.loadingBackground.alpha = 1f // Reset alpha
            }
            .start()

        binding.loadingProgressBar.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}