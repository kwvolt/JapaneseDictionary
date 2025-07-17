package io.github.kwvolt.japanesedictionary.ui.dictionaryList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.github.kwvolt.japanesedictionary.databinding.DictionarylistLayoutBinding

class DictionaryRecyclerViewFragment: Fragment() {

    private var _binding: DictionarylistLayoutBinding? = null
    private val binding: DictionarylistLayoutBinding get() = _binding ?: throw IllegalStateException("Binding is null")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DictionarylistLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}