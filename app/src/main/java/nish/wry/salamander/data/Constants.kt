package nish.wry.salamander.data

import nish.wry.salamander.ui.navigation.MainDestination
import nish.wry.salamander.ui.life.MainNishchayDestination
import nish.wry.salamander.ui.suBase.MainSuBaseDestination
import nish.wry.salamander.ui.taskTab.main.MainTaskDestination

object Constants {
    const val HOUR_HEIGHT = 40

    // user cant go more than 136.98 yrs forward/backward 💀
    const val PAGER_LIMIT = 100_000
    const val HALF_PAGE_LIMIT = PAGER_LIMIT / 2

    // 30 mins from start
    const val TASK_DURATION = 30

    const val MINS_IN_A_DAY = 1440

    // for category screen
    const val GOAL_TIME_SLIDER_DEFAULT_HRS = 0
    const val GOAL_TIME_SLIDER_DEFAULT_MINS = 0

    // for schedulers Intents
    const val EXTRA_TASK_ID = "TASK_ID"
    const val EXTRA_TASK_NAME = "TASK_NAME"

    val listOfDestination: List<MainDestination> = listOf(
        MainSuBaseDestination, MainTaskDestination, MainNishchayDestination
    )
}

