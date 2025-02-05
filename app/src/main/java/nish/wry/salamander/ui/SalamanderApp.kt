package nish.wry.salamander.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import nish.wry.salamander.ui.screens.NishchayDestination
import nish.wry.salamander.ui.screens.SuBaseDestination
import nish.wry.salamander.ui.task.TaskDestination
import nish.wry.salamander.ui.task.TaskScreen

@Composable
fun SalamanderApp(
    viewModel: SalamanderViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            viewModel.listOfDestination.forEach {
                item(
                    icon = {
                        Icon(
                            painter = painterResource(it.icon),
                            contentDescription = null
                        )
                    },
                    selected = viewModel.currentDestination == it,
                    onClick = { viewModel.currentDestination = it },
                    label = { Text(stringResource(it.titleRes)) },
                )
            }
        }
    ) {

        when (viewModel.currentDestination) {
            SuBaseDestination -> {}
            TaskDestination -> { TaskScreen(modifier = Modifier.fillMaxSize()) }
            NishchayDestination -> {}
        }

    }
}

@Preview(name = "Task Screen")
@Composable
fun SalamanderAppPreview() {
    val viewModel: SalamanderViewModel = viewModel(factory = AppViewModelProvider.Factory)
    viewModel.currentDestination = viewModel.listOfDestination[1]
    SalamanderApp(viewModel = viewModel)
}