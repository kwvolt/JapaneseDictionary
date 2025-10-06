package io.github.kwvolt.japanesedictionary.util

import android.os.Parcelable
import io.github.kwvolt.japanesedictionary.domain.model.SearchFilter
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
actual data class ParcelableSearchFilterWrapper actual constructor(val searchFilter: @RawValue SearchFilter) : Parcelable