package nish.wry.salamander.data

import nish.wry.salamander.ui.navigation.MainDestination
import nish.wry.salamander.ui.screens.MainNishchayDestination
import nish.wry.salamander.ui.screens.MainSuBaseDestination
import nish.wry.salamander.ui.screens.MainTaskDestination

object Constants {
    const val HOUR_HEIGHT = 40

    // 30 mins from start
    const val TASK_DURATION = 30
    val listOfDestination: List<MainDestination> = listOf(
        MainSuBaseDestination, MainTaskDestination, MainNishchayDestination
    )
}

