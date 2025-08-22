package nish.wry.salamander.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import nish.wry.salamander.data.Week
import nish.wry.salamander.data.and

@Composable
fun DaysOfTheWeekIconButtons(
    selectedWeekDaysBitmask: Int,
    setOrResetBitFlagForWeekday: (bool: Boolean, week: Week) -> Unit,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        Week.entries.forEachIndexed { index, week ->

            val toggledOn = (selectedWeekDaysBitmask and week) > 0

            OutlinedIconToggleButton(
                checked = toggledOn,
                onCheckedChange = { value ->
                    setOrResetBitFlagForWeekday(value, week)
                },
                enabled = !disabled,
                colors = IconButtonDefaults.outlinedIconToggleButtonColors(
                    containerColor = Color.Transparent,
                    checkedContainerColor = Color.Transparent,
                    checkedContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                border =
                    if (toggledOn) BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.onTertiaryContainer
                    ) else null
            ) {
                Text(
                    text = week.letter, color = if (index == 6) {
                        Color.Red
                    } else {
                        Color.Unspecified
                    }
                )

            }
        }
    }
}