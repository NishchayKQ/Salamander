package nish.wry.salamander.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import nish.wry.salamander.R

// TODO someday we will standardise padding, like don't hardcode these vals, instead for
//  start/end padding == column
// ✅ [done for this function in particular] top/bottom == particular composable decides how much space i need (top and bottom)
@Composable
fun SalamanderSwitch(
    checked: Boolean,
    onCheckedChange: (() -> Unit)?,
    switchText: String,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    toolTip: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
//                indication = null,
                onClick = if (onCheckedChange != null) onCheckedChange else {
                    {}
                },
                enabled = !disabled
            )
            .padding(
                start = 32.dp,
                end = 32.dp,
                top = dimensionResource(R.dimen.top_medium_padding),
                bottom = dimensionResource(R.dimen.bottom_medium_padding)
            )
            .alpha(if (disabled) 0.38f else 1f)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(switchText)
            if (toolTip != null) {
                Text(
                    text = toolTip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = null,
            interactionSource = interactionSource,
            thumbContent = if (checked) {
                {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize)
                    )
                }
            } else null
        )
    }

}