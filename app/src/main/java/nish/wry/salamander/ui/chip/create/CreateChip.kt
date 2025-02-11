package nish.wry.salamander.ui.chip.create

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable
import nish.wry.salamander.ui.AppViewModelProvider
import nish.wry.salamander.ui.chip.ChipEntryBody

@Serializable
object CreateChipDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChip(
    exitChip: () -> Unit,
    viewModel: CreateChipViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier,
) {
    val chipUiState by viewModel.chipUiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    ChipEntryBody(
        chipUiState = chipUiState,
        uiState = uiState,
        onChipNameChange = viewModel::onChipNameChange,
        onSegmentedButtonPriorityClick = viewModel::onSegmentedButtonPriorityClick,
        onTimelessSwitchClick = viewModel::onTimelessSwitchClick,
        onOffsetTimeChange = viewModel::onOffsetTimeChange,
        onFastIoInputChange = viewModel::onFastIoInputChange,
        toggleShowTimePicker = viewModel::toggleShowTimePicker,
        resetSelectedTimeToCurrentTime = viewModel::resetSelectedTimeToCurrentTime,
        setTime = viewModel::setTime,
        setOrResetBitFlagForWeekday = viewModel::setOrResetBitFlagForWeekday,
        saveCreatedChip = viewModel::saveCreatedChip,
        exitChip = exitChip,
        modifier = modifier
    )

}

// TODO fix preview
//@Preview
//@Composable
//fun CreateChipPreview() {
//    class FakeTaskRepository : TaskRepository {
//        override fun getAllChips(): Flow<List<Chip>> {
//            TODO("Not yet implemented")
//        }
//
//        override fun getChipWithId(id: Int): Flow<Chip> {
//            TODO("Not yet implemented")
//        }
//
//        override suspend fun createChip(chip: Chip) {
//            TODO("Not yet implemented")
//        }
//
//        override suspend fun deleteChip(chip: Chip) {
//            TODO("Not yet implemented")
//        }
//
//        override suspend fun updateChip(chip: Chip) {
//            TODO("Not yet implemented")
//        }
//
//        override fun getTasksWithChip(chipId: Int): Flow<List<Task>> {
//            TODO("Not yet implemented")
//        }
//
//        override fun getTaskWithId(id: Int): Flow<Task> {
//            TODO("Not yet implemented")
//        }
//
//        override fun getTaskForNextThreeDays(
//            bitmask: Int,
//            startDate: Calendar,
//            endDate: Calendar,
//        ): Flow<List<Task>> {
//            TODO("Not yet implemented")
//        }
//
//        override suspend fun createTask(task: Task) {
//            TODO("Not yet implemented")
//        }
//
//        override suspend fun updateTask(task: Task) {
//            TODO("Not yet implemented")
//        }
//
//        override suspend fun deleteTask(task: Task) {
//            TODO("Not yet implemented")
//        }
//
//    }
//
//    CreateChip(
//        viewModel = CreateChipViewModel(
//            savedStateHandle = ,
//            repository = FakeTaskRepository()
//        ),
//        exitChip = {}
//    )
//}

