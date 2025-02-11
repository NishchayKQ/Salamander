package nish.wry.salamander.ui.newTask

import android.text.format.DateFormat
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
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
fun NewTask(
    onCreateChip: () -> Unit,
    exitCreateTask: () -> Unit,
    viewModel: NewTaskViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier,
) {
    val chips by viewModel.chips.collectAsState()
    val taskUiState by viewModel.taskUiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val timePickerState = rememberTimePickerState(
        initialHour = taskUiState.selectedTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = taskUiState.selectedTime.get(Calendar.MINUTE),
        is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    )

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {

            TextField(
                value = taskUiState.taskName,
                onValueChange = viewModel::onTaskNameChange,
                label = { Text(stringResource(R.string.title)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                isError = !taskUiState.isTaskNameValid,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
            )

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 24.dp, bottom = 16.dp)
            ) {
                chips.forEach {
                    InputChip(
                        selected = it.id == taskUiState.chipId,
                        onClick = { viewModel.onChipSelected(it.id) },
                        label = { Text(it.name) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                IconButton(onClick = onCreateChip) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_event_chip)
                    )
                }
            }


            PrioritySegmentButton(
                selectedPriority = taskUiState.priority,
                changePriority = viewModel::onSegmentedButtonPriorityClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )

            TimelessSwitch(
                checked = taskUiState.timeless,
                onSwitchToggle = viewModel::onTimelessSwitchClick,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (taskUiState.timeless) {
                TextField(
                    value = uiState.offsetHoursString,
                    onValueChange = viewModel::onOffsetTimeChange,
                    singleLine = true,
                    label = { Text(stringResource(R.string.offset_hours)) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = if (taskUiState.isTaskNameValid) ImeAction.Done else ImeAction.Previous
                    ),
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp)
                )
            }

            if (!taskUiState.timeless) {
                TextField(
                    value = uiState.fastTimeIoInput,
                    onValueChange = viewModel::onFastIoInputChange,
                    label = { Text(stringResource(R.string.fast_time_io)) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = if (taskUiState.isTaskNameValid) ImeAction.Done else ImeAction.Previous
                    ),
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 8.dp),
                )

                TimeStampText(
                    time = taskUiState.selectedTime.time,
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
                    selectedWeekDaysBitmask = taskUiState.selectedWeekDaysBitmask,
                    setOrResetBitFlagForWeekday = viewModel::setOrResetBitFlagForWeekday,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
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
                            viewModel.onSaveTaskClicked()
                            exitCreateTask()
                        }
                    },
                    enabled = taskUiState.isTaskNameValid && taskUiState.chipId != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(stringResource(R.string.save))
                }
                OutlinedButton(
                    onClick = exitCreateTask,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }

        }

    }
}

