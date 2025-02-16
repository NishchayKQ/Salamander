package nish.wry.salamander.data

import nish.wry.salamander.ui.navigation.MainDestination
import nish.wry.salamander.ui.screens.MainNishchayDestination
import nish.wry.salamander.ui.screens.MainSuBaseDestination
import nish.wry.salamander.ui.screens.MainTaskDestination

object Constants {
    const val HOUR_HEIGHT = 40

    // user cant go more than 136.98 yrs forward/backward 💀
    const val PAGER_LIMIT = 100_000
    const val HALF_PAGE_LIMIT = PAGER_LIMIT / 2

    // 30 mins from start
    const val TASK_DURATION = 30

    const val MINS_IN_A_DAY = 1440

    val listOfDestination: List<MainDestination> = listOf(
        MainSuBaseDestination, MainTaskDestination, MainNishchayDestination
    )
}

