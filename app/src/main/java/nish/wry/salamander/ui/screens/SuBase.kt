package nish.wry.salamander.ui.screens

import kotlinx.serialization.Serializable
import nish.wry.salamander.R
import nish.wry.salamander.ui.navigation.MainDestination

@Serializable
object MainSuBaseDestination : MainDestination {
    override val titleRes: Int = R.string.subase
    override val iconRes: Int = R.drawable.outline_timer_24
}

