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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import nish.wry.salamander.ui.screens.NishchayDestination
import nish.wry.salamander.ui.screens.SuBaseDestination
import nish.wry.salamander.ui.screens.TaskDestination
import nish.wry.salamander.ui.screens.TaskScreen

@Composable
fun SalamanderApp(
    windowSizeClass: WindowWidthSizeClass,
    navController: NavHostController = rememberNavController(),
    viewModel: SalamanderViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val isExpandedWindowSize = windowSizeClass != WindowWidthSizeClass.Compact
    Row {
        AnimatedVisibility(isExpandedWindowSize) {
            NavigationRail {
                viewModel.listOfDestination.forEach {
                    NavigationRailItem(
                        selected = viewModel.currentDestination == it,
                        onClick = {
                            viewModel.currentDestination = it
                            navController.navigate(it)
                        },
                        icon = {
                            Icon(
                                painterResource(it.iconRes),
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(it.titleRes)) }
                    )
                }
                // fabs go here
            }

        }
        Column {
            NavHost(
                navController = navController,
                startDestination = TaskDestination,
                modifier = Modifier.weight(1f)

            ) {
                composable<SuBaseDestination> {

                }

                composable<TaskDestination> {
                    TaskScreen()
                }

                composable<NishchayDestination> { }

            }
            AnimatedVisibility(!isExpandedWindowSize) {
                NavigationBar {
                    viewModel.listOfDestination.forEach {
                        NavigationBarItem(
                            selected = viewModel.currentDestination == it,
                            onClick = {
                                viewModel.currentDestination = it
                                navController.navigate(it)
                            },
                            icon = {
                                Icon(
                                    painterResource(it.iconRes),
                                    contentDescription = stringResource(it.titleRes)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

//FIXME preview wont work cuz viewmodel has IO operations
@Preview(name = "Task Screen")
@Composable
fun SalamanderAppPreview() {
    val viewModel: SalamanderViewModel = viewModel(factory = AppViewModelProvider.Factory)
    viewModel.currentDestination = viewModel.listOfDestination[1]
    SalamanderApp(
        windowSizeClass = WindowWidthSizeClass.Compact,
        viewModel = viewModel
    )
}