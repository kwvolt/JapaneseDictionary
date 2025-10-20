package io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.tabs.worddefinition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DwpDefinitionTabBinding
import io.github.kwvolt.japanesedictionary.domain.model.dictionary_entry.WordEntryFormData
import io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage.DictionaryDetailPagePreviewDialogFragment

class WordDefinitionTabPreviewFragment: Fragment() {
    private var _binding: DwpDefinitionTabBinding? = null
    private val binding: DwpDefinitionTabBinding get() = _binding ?: throw IllegalStateException(getString(R.string.binding_null_error))

    val formData: WordEntryFormData? by lazy {
        BundleCompat.getParcelable(
            requireArguments(),
            DictionaryDetailPagePreviewDialogFragment.ARG_WORD_FORM_ENTRY_DATA,
            WordEntryFormData::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DwpDefinitionTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val renderer = WordDefinitionTabRenderer(true, binding, this)
        formData?.let {renderer.renderEntry(renderer.formatText(it))}

    }
}