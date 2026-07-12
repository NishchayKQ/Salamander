package nish.wry.salamander.ui.taskTab.chip

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.serialization.Serializable
import nish.wry.salamander.ui.taskTab.ChipOrTaskEntryBody

@Serializable
object CreateChipDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChip(
    exitChip: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateChipViewModel = hiltViewModel(),
) {
    val chipUiState by viewModel.chipUiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    ChipOrTaskEntryBody(
        genericTaskOrChipUiState = chipUiState,
        uiState = uiState,
        onNameChange = viewModel::onChipNameChange,
        onSegmentedButtonPriorityClick = viewModel::onSegmentedButtonPriorityClick,
        onTimelessSwitchClick = viewModel::onTimelessSwitchClick,
        onOffsetTimeChange = viewModel::onOffsetTimeChange,
        onFastIoInputChange = viewModel::onFastIoInputChange,
        toggleShowTimePicker = viewModel::toggleShowTimePicker,
        resetSelectedTimeToCurrentTime = viewModel::resetSelectedTimeToCurrentTime,
        setTime = viewModel::setTime,
        setOrResetBitFlagForWeekday = viewModel::setOrResetBitFlagForWeekday,
        saveData = viewModel::saveCreatedChip,
        onExitRequested = exitChip,
        onForGroupingOnlyToggled = viewModel::toggleForGroupingOnly,
        modifier = modifier
    )

}