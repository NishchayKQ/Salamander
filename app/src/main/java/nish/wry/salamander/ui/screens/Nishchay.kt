package nish.wry.salamander.ui.screens

import kotlinx.serialization.Serializable
import nish.wry.salamander.R
import nish.wry.salamander.ui.navigation.NavigationDestination

@Serializable
object NishchayDestination : NavigationDestination {
    override val titleRes: Int = R.string.nishchay
    override val iconRes: Int = R.drawable.outline_person_24
}
