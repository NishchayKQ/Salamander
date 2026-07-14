package nish.wry.salamander.ui.common

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import nish.wry.salamander.R


/**
 * @param useInputChips whether to use input chips or filter chips
 * **/
@Composable
fun SalamanderChip(
    id: Int,
    selected: Boolean,
    chipName: String,
    onClick: (Int) -> Unit,
    onEditChipClicked: (Int) -> Unit,
    onDeleteChipClicked: (Int) -> Unit,
    useInputChips: Boolean,
    modifier: Modifier = Modifier,
) {
    val chipInteractionSource = remember { MutableInteractionSource() }
    var menuExpanded by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    // double box to override chip's single click support only
    Box(modifier = modifier) {
        if (useInputChips) {
            InputChip(
                selected = selected,
                // not used
                onClick = {},
                label = { Text(chipName) },
                modifier = Modifier.padding(end = 8.dp),
                interactionSource = chipInteractionSource
            )
        } else {
            FilterChip(
                selected = selected,
                // not used
                onClick = {},
                label = { Text(chipName) },
                modifier = Modifier.padding(end = 8.dp),
                interactionSource = chipInteractionSource
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .combinedClickable(
                    indication = null,
                    interactionSource = chipInteractionSource,
                    onClick = { onClick(id) },
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuExpanded = true
                    }
                )
        )
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = { onEditChipClicked(id) },
                leadingIcon = {
                    Icon(painterResource(R.drawable.outline_edit_24), null) }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    menuExpanded = false
                    onDeleteChipClicked(id)
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.outline_delete_24),
                        null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }


}