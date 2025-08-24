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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nish.wry.salamander.R
import nish.wry.salamander.data.Priority
import nish.wry.salamander.data.Week
import nish.wry.salamander.data.room.task.Chip
import nish.wry.salamander.ui.common.DaysOfTheWeekIconButtons
import nish.wry.salamander.ui.common.PrioritySegmentButton
import nish.wry.salamander.ui.common.SalamanderSwitch
import nish.wry.salamander.ui.common.SetAndResetTimeButtons
import nish.wry.salamander.ui.common.TimeInputDialogBox
import nish.wry.salamander.ui.common.TimeStampText
import nish.wry.salamander.ui.taskTab.chip.GenericTaskOrChipUiState
import nish.wry.salamander.ui.taskTab.chip.UiState
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipOrTaskEntryBody(
    genericTaskOrChipUiState: GenericTaskOrChipUiState,
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
    modifier: Modifier = Modifier,
    chips: List<Chip>? = null,
    onCreateChip: (() -> Unit)? = null,
    onChipSelected: ((Int) -> Unit)? = null,
    onForGroupingOnlyToggled: (() -> Unit)? = null,
) {
    val isTaskScreen = onCreateChip != null

    val coroutineScope = rememberCoroutineScope()

    // remember messes up if edit task is clicked (it doesn't update by the initial values)
    val timePickerState = TimePickerState(
        initialHour = genericTaskOrChipUiState.selectedTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = genericTaskOrChipUiState.selectedTime.get(Calendar.MINUTE),
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
                value = genericTaskOrChipUiState.name,
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
                isError = genericTaskOrChipUiState.name.isBlank(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
            )
            if (!isTaskScreen) {
                SalamanderSwitch(
                    checked = genericTaskOrChipUiState.forGroupingOnly,
                    onCheckedChange = onForGroupingOnlyToggled,
                    switchText = stringResource(R.string.only_for_categorization)
                )
            }

            if (isTaskScreen && chips != null && onChipSelected != null) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 24.dp, bottom = 16.dp)
                ) {
                    chips.forEach {
                        // don't show deleted items
                        if (it.deleted) return@forEach

                        InputChip(
                            selected = it.id == genericTaskOrChipUiState.chipId,
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
            // if its for grouping only then all these are irrelevant
            PrioritySegmentButton(
                selectedPriority = genericTaskOrChipUiState.priority,
                changePriority = onSegmentedButtonPriorityClick,
                disabled = genericTaskOrChipUiState.forGroupingOnly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .alpha(if (genericTaskOrChipUiState.forGroupingOnly) 0.38f else 1f)
            )

            SalamanderSwitch(
                checked = genericTaskOrChipUiState.timeless,
                onCheckedChange = onTimelessSwitchClick,
                disabled = genericTaskOrChipUiState.forGroupingOnly,
                switchText = stringResource(R.string.timeless_switch_name),
                toolTip = stringResource(R.string.timeless_switch_help)
            )


            if (genericTaskOrChipUiState.timeless) {
                TextField(
                    value = uiState.offsetHoursString,
                    onValueChange = onOffsetTimeChange,
                    singleLine = true,
                    label = { Text(stringResource(R.string.offset_hours)) },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number,
                        imeAction = if (genericTaskOrChipUiState.isEntryValid) ImeAction.Done else ImeAction.Previous
                    ),
                    enabled = !genericTaskOrChipUiState.forGroupingOnly,
                    modifier = Modifier.padding(start = 32.dp, end = 32.dp)
                )
            }

            if (!genericTaskOrChipUiState.timeless) {
                SetTimeAndDayOfWeek(
                    isEntryValid = genericTaskOrChipUiState.isEntryValid,
                    fastTimeIoValue = uiState.fastTimeIoInput,
                    onFastIoInputChange = onFastIoInputChange,
                    timeForTimeStampText = genericTaskOrChipUiState.selectedTime.time,
                    toggleShowTimePicker = toggleShowTimePicker,
                    resetSelectedTimeToCurrentTime = resetSelectedTimeToCurrentTime,
                    showTimePicker = uiState.showTimePicker,
                    timePickerState = timePickerState,
                    setTime = setTime,
                    selectedWeekDaysBitmask = genericTaskOrChipUiState.selectedWeekDaysBitmask,
                    setOrResetBitFlagForWeekday = setOrResetBitFlagForWeekday,
                    disabled = genericTaskOrChipUiState.forGroupingOnly
                )
            }

            SaveAndCancelButtons(
                coroutineScope = coroutineScope,
                isEntryValid = genericTaskOrChipUiState.isEntryValid,
                saveFunction = saveData,
                exitFunction = onExitRequested,
            )
        }
    }

}

@Composable
fun SaveAndCancelButtons(
    coroutineScope: CoroutineScope,
    isEntryValid: Boolean,
    saveFunction: suspend () -> Unit,
    exitFunction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(
            onClick = {
                coroutineScope.launch {
                    saveFunction()
                    exitFunction()
                }
            },
            enabled = isEntryValid,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(stringResource(R.string.save))
        }
        OutlinedButton(onClick = exitFunction, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.cancel))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetTimeAndDayOfWeek(
    isEntryValid: Boolean,
    fastTimeIoValue: String,
    onFastIoInputChange: (String) -> Unit,
    timeForTimeStampText: Date,
    toggleShowTimePicker: () -> Unit,
    resetSelectedTimeToCurrentTime: () -> Unit,
    showTimePicker: Boolean,
    timePickerState: TimePickerState,
    setTime: (TimePickerState) -> Unit,
    selectedWeekDaysBitmask: Int,
    setOrResetBitFlagForWeekday: (Boolean, Week) -> Unit,
    disabled: Boolean = false,
) {
    OutlinedTextField(
        value = fastTimeIoValue,
        onValueChange = onFastIoInputChange,
        label = { Text(stringResource(R.string.fast_time_io)) },
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = if (isEntryValid) ImeAction.Done else ImeAction.Previous
        ),
        enabled = !disabled,
        modifier = Modifier.padding(start = 32.dp, end = 32.dp, bottom = 8.dp),
    )

    TimeStampText(
        time = timeForTimeStampText,
        modifier = Modifier
            .padding(start = 32.dp, end = 32.dp, bottom = 32.dp)
            .alpha(alpha = if (disabled) 0.38f else 1f)
    )

    SetAndResetTimeButtons(
        toggleShowTimePicker = toggleShowTimePicker,
        onResetTimeButtonClicked = resetSelectedTimeToCurrentTime,
        disabled = disabled,
        modifier = Modifier.padding(start = 32.dp, end = 32.dp)
    )

    TimeInputDialogBox(
        showTimePicker = showTimePicker,
        timePickerState = timePickerState,
        toggleShowTimePicker = toggleShowTimePicker,
        setTime = setTime
    )

    DaysOfTheWeekIconButtons(
        selectedWeekDaysBitmask = selectedWeekDaysBitmask,
        setOrResetBitFlagForWeekday = setOrResetBitFlagForWeekday,
        disabled = disabled,
        modifier = Modifier.fillMaxWidth()
    )
}