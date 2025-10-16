package nish.wry.salamander.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import nish.wry.salamander.R

// TODO someday we will standardise padding, like don't hardcode these vals, instead for
//  start/end padding == column
// ✅ [done for this function in particular] top/bottom == particular composable decides how much space i need (top and bottom)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalamanderSwitch(
    checked: Boolean,
    onCheckedChange: (() -> Unit)?,
    switchText: String,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
    toolTip: String? = null,
    richToolTipEnabled: Boolean = false,
) {
    require((richToolTipEnabled && toolTip != null) || !richToolTipEnabled) {
        "if richToolTipEnabled is true, then toolTip cannot be null"
    }
    val interactionSource = remember { MutableInteractionSource() }

    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            RichTooltip(
                title = { Text(switchText) }) {
                Text(toolTip ?: "")
            }
        },
        enableUserInput = richToolTipEnabled,
        state = rememberTooltipState()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
//                indication = null,
                    onClick = if (onCheckedChange != null) onCheckedChange else {
                        {}
                    }, enabled = !disabled)
                .padding(
                    start = 32.dp,
                    end = 32.dp,
                    top = dimensionResource(R.dimen.top_medium_padding),
                    bottom = dimensionResource(R.dimen.bottom_medium_padding)
                )
                .alpha(if (disabled) 0.38f else 1f)) {
            if (richToolTipEnabled) {
                Text(switchText, modifier = Modifier.weight(1f))
            } else {
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
            }

            Switch(
                checked = checked,
                onCheckedChange = null,
                interactionSource = interactionSource,
                thumbContent = if (checked) {
                    {
                        Icon(
                            painter = painterResource(R.drawable.outline_check_24),
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                } else null)
        }
    }

}