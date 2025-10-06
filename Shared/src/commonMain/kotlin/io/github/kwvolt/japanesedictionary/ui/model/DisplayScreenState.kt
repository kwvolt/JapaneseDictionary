package io.github.kwvolt.japanesedictionary.ui.model

data class DisplayScreenState(
    val entry: DisplayEntryUIModel? = null,
    override val isLoading: Boolean = false,
    override val screenStateUnknownError: ScreenStateUnknownError? = null,
    val hasUpdated: Boolean = false
): ScreenState(isLoading, screenStateUnknownError), ScreenStateErrorCopyable<DisplayScreenState>{
    override fun copyWithError(error: ScreenStateUnknownError?): DisplayScreenState {
        return copy(screenStateUnknownError = error)
    }
}

data class DisplayEntryUIModel(
    val primaryText: String,
    val mainWordClass: String? = null,
    val subWordClass: String? = null,
    val generalNotes: List<DisplayNote>,
    val sections: List<DisplaySection>
)

data class DisplaySection(
    val sectionId: Int,
    val meaningText: String,
    val kanaList: List<String>,
    val notes: List<DisplayNote>
)

data class DisplayNote(
    val id: Long,
    val text: String
)