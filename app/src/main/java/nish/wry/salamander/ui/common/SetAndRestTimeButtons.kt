package nish.wry.salamander.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nish.wry.salamander.R

@Composable
fun SetAndResetTimeButtons(
    toggleShowTimePicker: () -> Unit,
    onResetTimeButtonClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        Button(
            onClick = toggleShowTimePicker,
            modifier = Modifier.padding(end = 8.dp)
        ) { Text(stringResource(R.string.set_time)) }

        TextButton(onClick = onResetTimeButtonClicked) {
            Text("Reset time")
        }

    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun SetAndResetTimeButtonsPreview() {
    SetAndResetTimeButtons(
        toggleShowTimePicker = { },
        onResetTimeButtonClicked = {}
    )
}