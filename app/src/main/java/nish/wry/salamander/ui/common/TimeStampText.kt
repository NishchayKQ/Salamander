package nish.wry.salamander.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.util.Date

@Composable
fun TimeStampText(
    time: Date,
    modifier: Modifier = Modifier,
) {
    Text(
        text = android.icu.text.DateFormat.getDateTimeInstance(
            android.icu.text.DateFormat.MEDIUM,
            android.icu.text.DateFormat.SHORT,
        ).format(time).toString(),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}