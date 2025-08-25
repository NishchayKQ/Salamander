package nish.wry.salamander.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nish.wry.salamander.R

/**
 * @param chips list of some chips to display
 * @param selectedChipId id of chip selected
 * @param getChipId must be mapped to the actual Object's id ex MyChip::myChipId
 * **/
@Composable
fun <T> SalamanderSingleInputChip(
    chips: List<T>,
    selectedChipId: Int,
    getChipId: (T) -> Int,
    getChipName: (T) -> String,
    getChipDeleted: (T) -> Boolean,
    onChipSelected: (Int) -> Unit,
    onCreateChipClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
    ) {
        chips.forEach {
            // don't show deleted items
            if (getChipDeleted(it)) return@forEach

            InputChip(
                selected = getChipId(it) == selectedChipId,
                onClick = { onChipSelected(getChipId(it)) },
                label = { Text(getChipName(it)) },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        IconButton(onClick = onCreateChipClicked) {
            Icon(
                Icons.Default.Add,
                contentDescription = stringResource(R.string.add_event_chip)
            )
        }
    }
}