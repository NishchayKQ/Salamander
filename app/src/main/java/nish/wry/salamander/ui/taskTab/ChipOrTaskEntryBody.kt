package nish.wry.salamander.ui.taskTab

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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nish.wry.salamander.R
import nish.wry.salamander.data.Priority
import nish.wry.salamander.data.Week
import nish.wry.salamander.data.room.Chip
import nish.wry.salamander.ui.common.DaysOfTheWeekIconButtons
import nish.wry.salamander.ui.common.PrioritySegmentButton
import nish.wry.salamander.ui.common.SetAndResetTimeButtons
import nish.wry.salamander.ui.common.TimeInputDialogBox
import nish.wry.salamander.ui.common.TimeStampText
import nish.wry.salamander.ui.common.TimelessSwitch
import nish.wry.salamander.ui.taskTab.chip.ChipOrTaskUiState
import nish.wry.salamander.ui.taskTab.chip.UiState
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipOrTaskEntryBody(
    chipOrTaskUiState: ChipOrTaskUiState,
    uiState: UiState,
    onNameChange: (String) -> Unit,
    onSegmentedButtonPriorityClick: (Priority) -> Unit,
    onTimelessSwitchClick: () -> Unit,
    onOffsetTimeChange: (String) -> Unit,
    onFastIoInputChange: (String) -> Unit,
    toggleShowTimePicker: () -> Unit,
    resetSelectedTimeToCurrentTime: () -> Unit,
    setTime: (TimePickerState) -> Unit,
    setOrResetBitFlagForWeekday: (Boolean, Week) -> Unit,
    saveData: suspend () -> Unit,
    onExitRequested: () -> Unit,
    chips: List<Chip>? = null,
    onCreateChip: (() -> Unit)? = null,
    onChipSelected: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val isTaskScreen = onCreateChip != null

    val coroutineScope = rememberCoroutineScope()

    // remember messes up if edit task is clicked (it doesn't update by the initial values)
    val timePickerState = TimePickerState(
        initialHour = chipOrTaskUiState.selectedTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = chipOrTaskUiState.selectedTime.get(Calendar.MINUTE),
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
                value = chipOrTaskUiState.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(if (isTaskScreen) R.string.task_name else R.string.chip_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 32.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (isTaskScreen) 8.dp else 16.dp
                    ),
                isError = chipOrTaskUiState.name.isBlank(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
            )
            if (isTaskScreen && chips != null && onChipSelected != null && onCreateChip != null) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 24.dp, bottom = 16.dp)
                ) {
                    chips.forEach {
                        InputChip(
                            selected = it.id == chipOrTaskUiState.chipId,
                            onClick = { onChipSelected(it.id) },
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
            }

            PrioritySegmentButton(
                selectedPriority = chipOrTaskUiState.priority,
                changePriority = onSegmentedButtonPriorityClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )

            TimelessSwitch(
                checked = chipOrTaskUiState.timeless,
                onSwitchToggle = onTimelessSwitchClick,
                modifier = Modifier.padding(bottom = 32.dp)
            )


            if (chipOrTaskUiState.timeless) {
                TextField(
                    value = uiState.offsetHoursString,
                    onValueChange = onOffsetTimeChange,
                    singleLine = true,
                    label = { Text(stringResource(R.string.offset_hours)) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = if (chipOrTaskUiState.isEntryValid) ImeAction.Done else ImeAction.Previous
                    ),
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp)
                )
            }

            if (!chipOrTaskUiState.timeless) {
                TextField(
                    value = uiState.fastTimeIoInput,
                    onValueChange = onFastIoInputChange,
                    label = { Text(stringResource(R.string.fast_time_io)) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = if (chipOrTaskUiState.isEntryValid) ImeAction.Done else ImeAction.Previous
                    ),
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 8.dp),
                )

                TimeStampText(
                    time = chipOrTaskUiState.selectedTime.time,
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 32.dp)
                )

                SetAndResetTimeButtons(
                    toggleShowTimePicker = toggleShowTimePicker,
                    onResetTimeButtonClicked = resetSelectedTimeToCurrentTime,
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp)
                )

                TimeInputDialogBox(
                    showTimePicker = uiState.showTimePicker,
                    timePickerState = timePickerState,
                    toggleShowTimePicker = toggleShowTimePicker,
                    setTime = setTime
                )

                DaysOfTheWeekIconButtons(
                    selectedWeekDaysBitmask = chipOrTaskUiState.selectedWeekDaysBitmask,
                    setOrResetBitFlagForWeekday = setOrResetBitFlagForWeekday,
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
                            saveData()
                            onExitRequested()
                        }
                    },
                    enabled = chipOrTaskUiState.isEntryValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(stringResource(R.string.save))
                }
                OutlinedButton(onClick = onExitRequested, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }

}