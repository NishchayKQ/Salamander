package nish.wry.salamander.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import nish.wry.salamander.data.Constants
import nish.wry.salamander.ui.navigation.EditChipDestination
import nish.wry.salamander.ui.navigation.EditTaskDestination
import nish.wry.salamander.ui.screens.MainNishchayDestination
import nish.wry.salamander.ui.screens.MainSuBaseDestination
import nish.wry.salamander.ui.screens.NishchayScreen
import nish.wry.salamander.ui.screens.NishchayScreenDestination
import nish.wry.salamander.ui.taskTab.chip.CreateChip
import nish.wry.salamander.ui.taskTab.chip.CreateChipDestination
import nish.wry.salamander.ui.taskTab.main.MainTaskDestination
import nish.wry.salamander.ui.taskTab.main.TaskScreen
import nish.wry.salamander.ui.taskTab.main.TaskTimelineDestination
import nish.wry.salamander.ui.taskTab.task.CreateTask
import nish.wry.salamander.ui.taskTab.task.CreateTaskDestination

@Composable
fun SalamanderApp(
    windowSizeClass: WindowWidthSizeClass,
    navController: NavHostController = rememberNavController(),
) {
    val isExpandedWindowSize = windowSizeClass != WindowWidthSizeClass.Compact
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val testDestination = navBackStackEntry?.destination


    Row {
        AnimatedVisibility(isExpandedWindowSize) {
            NavigationRail {
                Constants.listOfDestination.forEach { mainDestination ->
                    NavigationRailItem(selected = testDestination?.hierarchy?.any {
                        it.hasRoute(
                            mainDestination::class
                        )
                    } == true, onClick = {
                        navController.navigate(mainDestination)
                    }, icon = {
                        Icon(
                            painterResource(mainDestination.iconRes), contentDescription = null
                        )
                    }, label = { Text(stringResource(mainDestination.titleRes)) })
                }
                // fabs go here
            }

        }
        Column {
            AppNavHost(
                navController = navController,
                isOnlyExpandedWindowSize = WindowWidthSizeClass.Expanded == windowSizeClass,
                modifier = Modifier.weight(1f)
            )
            AnimatedVisibility(!isExpandedWindowSize) {
                NavigationBar {
                    Constants.listOfDestination.forEach { mainDestination ->
                        NavigationBarItem(selected = testDestination?.hierarchy?.any {
                            it.hasRoute(
                                mainDestination::class
                            )
                        } == true, onClick = {
                            navController.navigate(mainDestination) {
                                // Avoid multiple copies of the same destination when
                                // re-selecting the same item
                                launchSingleTop = true
                                // Restore state when re-selecting a previously selected item
                                restoreState = true
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                            }
                        }, icon = {
                            Icon(
                                painterResource(mainDestination.iconRes),
                                contentDescription = stringResource(mainDestination.titleRes)
                            )
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    isOnlyExpandedWindowSize: Boolean,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController, startDestination = MainTaskDestination, modifier = modifier

    ) {
        composable<MainSuBaseDestination> {
//            val suBaseNavController = rememberNavController()
        }
        composable<MainTaskDestination> {
            val taskNavController = rememberNavController()
            NavHost(navController = taskNavController, startDestination = TaskTimelineDestination) {

                val taskTimeLineScreen: @Composable (Modifier) -> Unit = { innerModifier ->
                    TaskScreen(
                        onCreateTaskClicked = { taskNavController.navigate(CreateTaskDestination) },
                        onEditChipClicked = { chipId ->
                            taskNavController.navigate(EditChipDestination(chipId))
                        },
                        onTaskClicked = { taskId ->
                            taskNavController.navigate(EditTaskDestination(taskId))
                        },
                        modifier = innerModifier,
                    )
                }

                composable<TaskTimelineDestination> {
                    taskTimeLineScreen(Modifier)
                }

                composable<CreateTaskDestination> {
                    Row {
                        if (isOnlyExpandedWindowSize) {
                            taskTimeLineScreen(Modifier.weight(0.5f))
                        }
                        CreateTask(
                            onCreateChip = { taskNavController.navigate(CreateChipDestination) },
                            exitCreateTask = {
                                taskNavController.popBackStack(
                                    TaskTimelineDestination, false
                                )
                            },
                            modifier = Modifier.weight(0.5f)
                        )
                    }
                }

                composable<CreateChipDestination> {
                    Row {
                        if (isOnlyExpandedWindowSize) {
                            taskTimeLineScreen(Modifier.weight(0.5f))
                        }
                        CreateChip(
                            exitChip = {
                                taskNavController.popBackStack(
                                    CreateTaskDestination, false
                                )
                            }, modifier = Modifier.weight(0.5f)
                        )
                    }

                }
                composable<EditChipDestination> {
                    Row {
                        if (isOnlyExpandedWindowSize) {
                            taskTimeLineScreen(Modifier.weight(0.5f))
                        }
                        CreateChip(
                            exitChip = { taskNavController.popBackStack() },
                            modifier = Modifier.weight(0.5f)
                        )
                    }
                }

                composable<EditTaskDestination> {
                    Row {
                        if (isOnlyExpandedWindowSize) {
                            taskTimeLineScreen(Modifier.weight(0.5f))
                        }
                        CreateTask(
                            onCreateChip = { taskNavController.navigate(CreateChipDestination) },
                            exitCreateTask = { taskNavController.popBackStack() },
                            modifier = Modifier.weight(0.5f)
                        )
                    }
                }

            }
        }

        composable<MainNishchayDestination> {
            val nishchayNavController = rememberNavController()
            NavHost(
                navController = nishchayNavController, startDestination = NishchayScreenDestination
            ) {
                composable<NishchayScreenDestination> {
                    NishchayScreen()
                }
            }
        }

    }

}


@Preview(name = "Task Screen")
@Composable
fun SalamanderAppPreview() {
    SalamanderApp(
        windowSizeClass = WindowWidthSizeClass.Compact
    )
}