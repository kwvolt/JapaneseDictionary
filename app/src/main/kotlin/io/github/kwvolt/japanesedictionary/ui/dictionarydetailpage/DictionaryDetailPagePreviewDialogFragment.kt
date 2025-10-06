package io.github.kwvolt.japanesedictionary.ui.dictionarydetailpage

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.appbar.MaterialToolbar
import io.github.kwvolt.japanesedictionary.R
import io.github.kwvolt.japanesedictionary.databinding.DwpPreviewLayoutBinding
import io.github.kwvolt.japanesedictionary.domain.model.WordEntryFormData
import io.github.kwvolt.japanesedictionary.util.viewBinding

class DictionaryDetailPagePreviewDialogFragment: DialogFragment(R.layout.dwp_preview_layout) {
    private val binding: DwpPreviewLayoutBinding by viewBinding(DwpPreviewLayoutBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        val dictionaryDetailPageRenderer = DictionaryDetailPageRenderer(true)
        dictionaryDetailPageRenderer.applyBackgroundAlpha(binding.dwpPreviewCardView)

        val toolbar: MaterialToolbar = binding.dwpPreviewFormMenu
        toolbar.doOnLayout {
            toolbar.setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.dwp_menu_close_preview -> {
                        dismiss()
                        true
                    }
                    else -> false
                }
            }
        }
        dictionaryDetailPageRenderer.generateViewPagerAdapter(binding.dwpPreviewViewPager, binding.dwpPreviewTabLayout, this)
    }

    override fun onStart() {
        super.onStart()
        dialog?.let { thisDialog: Dialog ->
            thisDialog.window?.let { thisWindow ->
                thisWindow.setLayout(
                    (resources.displayMetrics.widthPixels * .9).toInt(),
                    (resources.displayMetrics.heightPixels * .95).toInt(),
                )
                thisWindow.setGravity(Gravity.CENTER)
            }
        }
    }

    companion object {
        const val ARG_WORD_FORM_ENTRY_DATA: String = "ARG_WORD_FORM_ENTRY_DATA"
        const val DICTIONARY_DETAIL_PAGE_PREVIEW_TAG: String = "DICTIONARY_DETAIL_PAGE_PREVIEW_TAG"

        fun show(fragmentManager: FragmentManager, wordEntryFormData: WordEntryFormData) {
            val fragment = DictionaryDetailPagePreviewDialogFragment()
            val args = Bundle().apply {
                putParcelable(ARG_WORD_FORM_ENTRY_DATA, wordEntryFormData)
            }
            fragment.arguments = args
            fragment.show(fragmentManager, DICTIONARY_DETAIL_PAGE_PREVIEW_TAG)
        }
    }
}