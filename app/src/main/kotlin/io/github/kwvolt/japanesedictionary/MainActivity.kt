package io.github.kwvolt.japanesedictionary

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import io.github.kwvolt.japanesedictionary.ui.addUpdate.AddUpdateRecyclerViewFragment
import io.github.kwvolt.japanesedictionary.databinding.ActivityMainBinding
import io.github.kwvolt.japanesedictionary.presentation.mainactivity.LoadingViewModel
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.DictionaryDetailPageViewFragment


class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding ?: throw IllegalStateException("Binding is null")

    private val loadingViewModel: LoadingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using ViewBinding
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        loadingViewModel.isLoading.observe(this) { isLoading ->
            if(isLoading) {
                showLoading()
            }
            else {
                hideLoading()
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

    private fun hideLoading(){
        binding.loadingBackground.animate().alpha(1f).setDuration(200).start()
        binding.loadingBackground.visibility = View.GONE
        binding.loadingProgressBar.visibility = View.GONE
    }
}