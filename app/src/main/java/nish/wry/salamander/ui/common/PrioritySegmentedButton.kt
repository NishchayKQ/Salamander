package nish.wry.salamander.ui.common

import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nish.wry.salamander.data.Priority

@Composable
fun PrioritySegmentButton(
    selectedPriority: Priority,
    changePriority: (Priority) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        Priority.entries.forEachIndexed { index: Int, priority: Priority ->
            SegmentedButton(
                selected = selectedPriority == priority,
                onClick = { changePriority(priority) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = Priority.entries.count()
                )
            ) {
                Text(priority.name)
            }
        }
    }
}