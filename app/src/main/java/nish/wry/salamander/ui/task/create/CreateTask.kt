package nish.wry.salamander.ui.task.create

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable
import nish.wry.salamander.ui.AppViewModelProvider
import nish.wry.salamander.ui.chip.ChipOrTaskEntryBody

@Serializable
object CreateTaskDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTask(
    onCreateChip: () -> Unit,
    exitCreateTask: () -> Unit,
    viewModel: CreateTaskViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier,
) {
    val chips by viewModel.chips.collectAsState()
    val taskUiState by viewModel.taskUiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    ChipOrTaskEntryBody(
        chipOrTaskUiState = taskUiState,
        uiState = uiState,
        onNameChange = viewModel::onTaskNameChange,
        onSegmentedButtonPriorityClick = viewModel::onSegmentedButtonPriorityClick,
        onTimelessSwitchClick = viewModel::onTimelessSwitchClick,
        onOffsetTimeChange = viewModel::onOffsetTimeChange,
        onFastIoInputChange = viewModel::onFastIoInputChange,
        toggleShowTimePicker = viewModel::toggleShowTimePicker,
        resetSelectedTimeToCurrentTime = viewModel::resetSelectedTimeToCurrentTime,
        setTime = viewModel::setTime,
        setOrResetBitFlagForWeekday = viewModel::setOrResetBitFlagForWeekday,
        saveData = viewModel::onSaveTaskClicked,
        onExitRequested = exitCreateTask,
        chips = chips,
        onCreateChip = onCreateChip,
        onChipSelected = viewModel::onChipSelected,
        modifier = modifier
    )

}

