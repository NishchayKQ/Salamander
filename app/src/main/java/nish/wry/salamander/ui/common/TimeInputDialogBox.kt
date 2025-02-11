package nish.wry.salamander.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nish.wry.salamander.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInputDialogBox(
    showTimePicker: Boolean,
    timePickerState: TimePickerState,
    toggleShowTimePicker: () -> Unit,
    setTime: (TimePickerState) -> Unit,
) {
    if (showTimePicker) {
        BasicAlertDialog(onDismissRequest = { setTime(timePickerState) }) {
            Column {
                Text("Select Time")

                TimeInput(timePickerState)

                Row(horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = toggleShowTimePicker,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(onClick = { setTime(timePickerState) }) {
                        Text(stringResource(R.string.ok))
                    }
                }

            }
        }
    }

}