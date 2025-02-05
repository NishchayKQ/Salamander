package nish.wry.salamander.ui.screens

import nish.wry.salamander.R
import nish.wry.salamander.ui.navigation.NavigationDestination
import nish.wry.salamander.ui.navigation.Routes

object SuBaseDestination : NavigationDestination {
    override val route = Routes.SuBase
    override val titleRes: Int = R.string.subase
    override val icon: Int = R.drawable.outline_timer_24
}

