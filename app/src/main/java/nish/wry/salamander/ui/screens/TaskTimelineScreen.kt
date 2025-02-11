package nish.wry.salamander.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable
import nish.wry.salamander.R
import nish.wry.salamander.ui.AppViewModelProvider
import nish.wry.salamander.ui.navigation.MainDestination
import nish.wry.salamander.ui.task.TaskBottomAppBar
import nish.wry.salamander.ui.task.TaskCanvas
import nish.wry.salamander.ui.task.TaskTopAppBar
import nish.wry.salamander.ui.task.TaskViewModel

@Serializable
object MainTaskDestination : MainDestination {
    override val titleRes: Int = R.string.task
    override val iconRes = R.drawable.outline_checklist_24
}

@Serializable
object TaskTimelineDestination

@Composable
fun TaskTimelineScreen(
    onCreateTaskClicked: () -> Unit,
    onEditChipClicked: (Int) -> Unit,
    viewModel: TaskViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier,
) {
    val timelineUiState by viewModel.timelineUiState.collectAsState()
    val taskScreenUiState by viewModel.taskScreenUiState.collectAsState()
    val chips by viewModel.chips.collectAsState()

    Scaffold(topBar = {
        TaskTopAppBar(
            chips = chips,
            selectedChipIds = timelineUiState.selectedChipIDs,
            onChipClicked = viewModel::onChipClicked,
            onEditChipClicked = onEditChipClicked,
            onDeleteChipClicked = {},
            searchQuery = taskScreenUiState.query,
            expanded = taskScreenUiState.expanded,
            onExpandedChange = viewModel::setSearchExpandedState,
            onSearch = { viewModel.setSearchExpandedState(false) },
            onQueryChange = viewModel::updateSearchQuery
        )
    }, bottomBar = {
        TaskBottomAppBar(onAddTaskClick = onCreateTaskClicked)
    }, modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        TaskCanvas(
            uiState = timelineUiState,
            updateScaleAndOffset = viewModel::updateZoomAndScroll,
            modifier = Modifier.padding(innerPadding)
        )

    }


}