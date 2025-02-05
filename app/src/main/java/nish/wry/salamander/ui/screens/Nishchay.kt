package nish.wry.salamander.ui.screens

import nish.wry.salamander.R
import nish.wry.salamander.ui.navigation.NavigationDestination
import nish.wry.salamander.ui.navigation.Routes

object NishchayDestination : NavigationDestination {
    override val route: Routes = Routes.Nishchay
    override val titleRes: Int = R.string.nishchay
    override val icon: Int = R.drawable.outline_person_24
}
