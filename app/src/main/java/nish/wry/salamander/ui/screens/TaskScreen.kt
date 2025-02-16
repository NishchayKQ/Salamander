package nish.wry.salamander.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable
import nish.wry.salamander.R
import nish.wry.salamander.data.Constants
import nish.wry.salamander.ui.AppViewModelProvider
import nish.wry.salamander.ui.navigation.MainDestination
import nish.wry.salamander.ui.task.CurrentTimeDivider
import nish.wry.salamander.ui.task.CurrentTimeText
import nish.wry.salamander.ui.task.HourLabels
import nish.wry.salamander.ui.task.HourlyDividers
import nish.wry.salamander.ui.task.TaskTopAppBar
import nish.wry.salamander.ui.task.TaskViewModel
import nish.wry.salamander.ui.task.TasksBox
import nish.wry.salamander.ui.task.TimelineLayout

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
    val pagerState = rememberPagerState(initialPage = Constants.HALF_PAGE_LIMIT) {
        Constants.PAGER_LIMIT
    }
    // TODO top app bar?? smaller search bar nag ??
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTaskClicked,
            ) {
                Icon(Icons.Default.Add, null)
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        LaunchedEffect(pagerState) {
            // Collect from the a snapshotFlow reading the currentPage
            snapshotFlow { pagerState.targetPage }.collect { target ->
                // Do something with each page change, for example:
                // viewModel.sendPageSelectedEvent(page)
                val forward = pagerState.targetPage - pagerState.currentPage > 0
                viewModel.addPage(target + if (forward) 1 else -1)
            }
        }


        HorizontalPager(state = pagerState, modifier = Modifier.padding(innerPadding)) { page ->
            TimelineLayout(
                isToday = page == Constants.HALF_PAGE_LIMIT,
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
                        taskDrawingDataList = viewModel.goodBoiMap[page]?.collectAsState()?.value
                            ?: listOf(),
                        onTaskClicked = onTaskClicked,
                        onDeleteTaskClicked = viewModel::onDeleteTaskClicked
                    )
                },
            )
        }
    }
}