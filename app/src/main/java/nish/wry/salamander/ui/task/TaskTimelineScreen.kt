package nish.wry.salamander.ui.task

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import nish.wry.salamander.ui.AppViewModelProvider

@Composable
fun TaskTimelineScreen(
    onAddTaskClick: () -> Unit,
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
            searchQuery = taskScreenUiState.query,
            expanded = taskScreenUiState.expanded,
            onExpandedChange = viewModel::setSearchExpandedState,
            onSearch = { viewModel.setSearchExpandedState(false) },
            onQueryChange = viewModel::updateSearchQuery
        )
    }, bottomBar = {
        TaskBottomAppBar(onAddTaskClick = onAddTaskClick)
    }, modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        TaskCanvas(
            uiState = timelineUiState,
            updateScaleAndOffset = viewModel::updateZoomAndScroll,
            modifier = Modifier.padding(innerPadding)
        )

    }


}