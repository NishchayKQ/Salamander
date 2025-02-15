package nish.wry.salamander.ui.task

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import nish.wry.salamander.ui.task.TimelineScope.taskData
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TasksBox(
    clusterList: List<Cluster>,
    onTaskClicked: (Int) -> Unit,
    onDeleteTaskClicked: (Int) -> Unit,
) {
    var menuExpanded by remember { mutableIntStateOf(-1) }
    val rectColor = MaterialTheme.colorScheme.primaryContainer

    // TODO maybe flatten clusters and send out processed data from viewmodel
    clusterList.forEach { cluster: Cluster ->
        cluster.list.forEach { taskOnGraph: TaskOnGraph ->
            Box(
                modifier = Modifier
                    .combinedClickable(
                        interactionSource = null,
                        indication = LocalIndication.current,
                        onClick = {
                            onTaskClicked(taskOnGraph.id)
                            menuExpanded = -1
                        },
                        onLongClick = {
                            menuExpanded = taskOnGraph.id
                        },
                    )
                    .taskData(
                        index = taskOnGraph.column,
                        maxSimultaneous = cluster.maxTaskSimultaneously,
                        startMins = taskOnGraph.startMins,
                        endMins = taskOnGraph.endMins
                    )
                    .drawBehind {
                        drawRoundRect(color = rectColor, cornerRadius = CornerRadius(12f,12f))
                    }
                    .fillMaxSize()
            ) {
                Row(Modifier.padding(4.dp)) { Text(taskOnGraph.name) }

                DropdownMenu(expanded = menuExpanded == taskOnGraph.id,
                    onDismissRequest = { menuExpanded = -1 }) {
                    DropdownMenuItem(text = { Text("Delete") }, onClick = {
                        onDeleteTaskClicked(taskOnGraph.id)
                        menuExpanded = -1
                    }, leadingIcon = {
                        Icon(
                            Icons.Outlined.Delete,
                            null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    })

                }
            }
        }

    }


}
class TaskParentData(
    val index: Int,
    val maxSimultaneous: Int,
    val startMins: Int,
    val endMins: Int,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = this@TaskParentData
}

// TODO how to use this scope 💀
@LayoutScopeMarker
@Immutable
object TimelineScope {
    @Stable
    fun Modifier.taskData(
        index: Int,
        maxSimultaneous: Int,
        startMins: Int,
        endMins: Int,
    ): Modifier {
        return then(TaskParentData(index, maxSimultaneous, startMins, endMins))
    }
}

@Composable
fun HourlyDividers() {
    val dividerColor = MaterialTheme.colorScheme.onSurfaceVariant
    repeat(24) {
        Spacer(modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawLine(
                    color = dividerColor,
                    start = Offset.Zero,
                    end = Offset(size.width, 0f),
                )
            })
    }
}

@Composable
fun HourLabels() {
    val cal: Calendar = Calendar.getInstance().also { calendar ->
        calendar.clear()
        calendar.set(Calendar.HOUR_OF_DAY, 1)
    }
    val is24Hour = android.text.format.DateFormat.is24HourFormat(LocalContext.current)
    val sdf = SimpleDateFormat(if (is24Hour) "H" else "h a", Locale.getDefault())

    val incHourByOne: () -> String = {
        val res = sdf.format(cal.time)
        cal.add(Calendar.HOUR_OF_DAY, 1)
        res
    }
    repeat(24) {
        Text(
            incHourByOne(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
fun CurrentTimeText() {
    val df = DateFormat.getTimeInstance(DateFormat.SHORT)
    var currentTimeText by remember { mutableStateOf(df.format(Date())) }

    LaunchedEffect(Unit) {
        while (isActive) {
            currentTimeText = df.format(Date())
            delay(60_000)
        }
    }

    val errorColor = MaterialTheme.colorScheme.error

    Text(
        currentTimeText, style = MaterialTheme.typography.bodySmall, color = errorColor
    )
}

@Composable
fun CurrentTimeDivider() {
    val errorColor = MaterialTheme.colorScheme.error

    Spacer(modifier = Modifier
        .fillMaxWidth()
        .drawWithCache {
            val path = Path()
            // both the size of triangle + used for line offset (line starts where triangle ends))
            val len = 10.dp.toPx()
            val xPos = 5.dp.toPx()

            path.moveTo(xPos, len / 2)
            path.lineTo(len + xPos, 0f)
            path.lineTo(xPos, -len / 2)
            path.close()

            onDrawBehind {
                drawLine(
                    color = errorColor,
                    start = Offset(len + xPos, 0f),
                    end = Offset(size.width, 0f),
                )
                drawPath(path = path, color = errorColor, style = Fill)
            }
        })
}