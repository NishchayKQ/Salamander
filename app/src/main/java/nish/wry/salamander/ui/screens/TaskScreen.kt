package nish.wry.salamander.ui.screens

import android.content.IntentFilter
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import nish.wry.salamander.ui.task.CurrentTimeDivider
import nish.wry.salamander.ui.task.CurrentTimeText
import nish.wry.salamander.ui.task.HourLabels
import nish.wry.salamander.ui.task.HourlyDividers
import nish.wry.salamander.ui.task.TaskDrawingData
import nish.wry.salamander.ui.task.TaskTopAppBar
import nish.wry.salamander.ui.task.TaskViewModel
import nish.wry.salamander.ui.task.TasksBox
import nish.wry.salamander.ui.task.TimelineLayout
import java.util.Calendar

@Serializable
object MainTaskDestination : MainDestination {
    override val titleRes: Int = R.string.task
    override val iconRes = R.drawable.outline_checklist_24
}

@Serializable
object TaskTimelineDestination

@Composable
fun TaskScreen(
    onCreateTaskClicked: () -> Unit,
    onEditChipClicked: (Int) -> Unit,
    onTaskClicked: (Int) -> Unit,
    viewModel: TaskViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier,
) {
    val timelineUiState by viewModel.timelineUiState.collectAsState()
    val taskScreenUiState by viewModel.taskScreenUiState.collectAsState()
    val chips by viewModel.chips.collectAsState()

    Scaffold(
        topBar = {
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
        },
//        bottomBar = {
////        TaskBottomAppBar(onAddTaskClick = onCreateTaskClicked)
//    },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTaskClicked,
            ) {
                Icon(Icons.Default.Add, null)
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        IntentFilter("")

        val taskState by viewModel.currentDayDataFlow.collectAsState()
        val tasks: List<TaskDrawingData> =
            taskState[Calendar.getInstance()[Calendar.DAY_OF_YEAR]] ?: listOf()

        TimelineLayout(
            hourLabels = { HourLabels() },
            currentTimeComposable = { CurrentTimeText() },
            currentTimeDivider = { CurrentTimeDivider() },
            saveScrollAndScale = viewModel::saveScrollState,
            scrollValue = timelineUiState.scrollValue,
            scale = timelineUiState.scale,
            dividerBars = { HourlyDividers() },
            tasksComposable = {
                TasksBox(
                    chipIdsSelected = timelineUiState.selectedChipIDs,
                    taskDrawingDataList = tasks,
                    onTaskClicked = onTaskClicked,
                    onDeleteTaskClicked = viewModel::onDeleteTaskClicked
                )
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}