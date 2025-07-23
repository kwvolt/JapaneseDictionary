package io.github.kwvolt.japanesedictionary.ui.model

data class DisplayScreenState(
    val entry: DisplayEntryUIModel? = null,
    val isLoading: Boolean = false,
    val screenStateUnknownError: ScreenStateUnknownError? = null,
    val hasUpdated: Boolean = false
)

data class DisplayEntryUIModel(
    val primaryText: String,
    val mainWordClass: String,
    val subWordClass: String,
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