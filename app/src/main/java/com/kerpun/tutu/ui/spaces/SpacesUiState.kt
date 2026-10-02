package com.kerpun.tutu.ui.spaces

import com.kerpun.tutu.data.model.Space
import kotlin.math.abs

val SpaceColorPalette = listOf("#4E8CFF", "#45C0B0", "#A984E8", "#F0A048", "#E86D8A")

/** Stable color per member, derived from their user id so it doesn't change between refreshes. */
fun colorForMemberId(userId: String): String = SpaceColorPalette[abs(userId.hashCode()) % SpaceColorPalette.size]

data class SpacesUiState(
    val spaces: List<Space> = emptyList(),
    val isLoading: Boolean = true,
    val activeSpaceId: String? = null,
)

data class CreateSpaceFormState(
    val name: String = "",
    val color: String = SpaceColorPalette.first(),
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = !isSubmitting && name.isNotBlank()
}
