package nish.wry.salamander.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

interface NavigationDestination {
    val route: Routes

    @get:StringRes
    val titleRes: Int

    @get:DrawableRes
    val icon: Int
}