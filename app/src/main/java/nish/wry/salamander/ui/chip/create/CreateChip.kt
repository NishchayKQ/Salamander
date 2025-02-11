package nish.wry.salamander.ui.chip.create

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import nish.wry.salamander.R
import nish.wry.salamander.ui.AppViewModelProvider
import nish.wry.salamander.ui.common.DaysOfTheWeekIconButtons
import nish.wry.salamander.ui.common.PrioritySegmentButton
import nish.wry.salamander.ui.common.SetAndResetTimeButtons
import nish.wry.salamander.ui.common.TimeInputDialogBox
import nish.wry.salamander.ui.common.TimeStampText
import nish.wry.salamander.ui.common.TimelessSwitch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChip(
    exitChip: () -> Unit,
    viewModel: CreateChipViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier,
) {
    BackHandler {
        exitChip()
    }

    val chipUiState by viewModel.chipUiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val timePickerState = rememberTimePickerState(
        initialHour = chipUiState.selectedTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = chipUiState.selectedTime.get(Calendar.MINUTE),
        is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    )

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TextField(
                value = chipUiState.name,
                onValueChange = viewModel::onChipNameChange,
                label = { Text(stringResource(R.string.chip_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                isError = chipUiState.name.isBlank(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
            )

            PrioritySegmentButton(
                selectedPriority = chipUiState.priority,
                changePriority = viewModel::onSegmentedButtonPriorityClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )

            TimelessSwitch(
                checked = chipUiState.timeless,
                onSwitchToggle = viewModel::onTimelessSwitchClick,
                modifier = Modifier.padding(bottom = 32.dp)
            )


            if (chipUiState.timeless) {
                TextField(
                    value = uiState.floatingOffsetString,
                    onValueChange = viewModel::onOffsetTimeChange,
                    singleLine = true,
                    label = { Text(stringResource(R.string.offset_hours)) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = if (chipUiState.isInputValid) ImeAction.Done else ImeAction.Previous
                    ),
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp)
                )
            }

            if (!chipUiState.timeless) {
                TextField(
                    value = uiState.fastTimeIoInput,
                    onValueChange = viewModel::onFastIoInputChange,
                    label = { Text(stringResource(R.string.fast_time_io)) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = if (chipUiState.isInputValid) ImeAction.Done else ImeAction.Previous
                    ),
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 8.dp),
                )

                TimeStampText(
                    time = chipUiState.selectedTime.time,
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 32.dp)
                )

                SetAndResetTimeButtons(
                    toggleShowTimePicker = viewModel::toggleShowTimePicker,
                    onResetTimeButtonClicked = viewModel::resetSelectedTimeToCurrentTime,
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp)
                )

                TimeInputDialogBox(
                    showTimePicker = uiState.showTimePicker,
                    timePickerState = timePickerState,
                    toggleShowTimePicker = viewModel::toggleShowTimePicker,
                    setTime = viewModel::setTime
                )

                DaysOfTheWeekIconButtons(
                    selectedWeekDaysBitmask = chipUiState.selectedWeekDaysBitmask,
                    setOrResetBitFlagForWeekday = viewModel::setOrResetBitFlagForWeekday,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.saveCreatedChip()
                            exitChip()
                        }
                    },
                    enabled = chipUiState.isInputValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(stringResource(R.string.save))
                }
                OutlinedButton(onClick = exitChip, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
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

