package nish.wry.salamander.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

interface MainDestination {
    @get:StringRes
    val titleRes: Int

    @get:DrawableRes
    val iconRes: Int
}