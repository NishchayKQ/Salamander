package nish.wry.salamander.ui.task

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import nish.wry.salamander.data.Constants
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TimelineLayout(
    hourLabels: @Composable () -> Unit,
    currentTimeComposable: @Composable () -> Unit,
    currentTimeDivider: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    var scaleY by rememberSaveable { mutableFloatStateOf(1f) }

    val avgHeight = (scaleY * Constants.HOUR_HEIGHT).dp

    var cal = Calendar.getInstance()
    var currentTimePos by remember { mutableFloatStateOf(cal[Calendar.HOUR_OF_DAY] + cal[Calendar.MINUTE] / 60f) }

    LaunchedEffect(Unit) {
        while (isActive){
            cal = Calendar.getInstance()
            currentTimePos = cal[Calendar.HOUR_OF_DAY] + cal[Calendar.MINUTE] / 60f
            delay(60_000)
        }
    }

    val dividerColor = MaterialTheme.colorScheme.onSurfaceVariant

    val dividerBars: @Composable () -> Unit = @Composable {
        repeat(24) {
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(avgHeight)
                .drawBehind {
                    drawLine(
                        color = dividerColor,
                        start = Offset.Zero,
                        end = Offset(size.width, 0f),
                    )
                })
        }
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Layout(
            contents = listOf(hourLabels, dividerBars, currentTimeComposable, currentTimeDivider),
            modifier = modifier
                .pointerInput(Unit) {
                    customDetectZoom { centroid, zoomChange ->
                        val oldScaleY = scaleY
                        // Constrain min/max zoom
                        scaleY = (scaleY * zoomChange).coerceIn(0.75f..5f)

                        // Don't move scroll position if no effective zoom occurred
                        val actualZoom = scaleY / oldScaleY
                        val scrollY = scrollState.value * actualZoom

                        val scrollOffset = (zoomChange - 1) * (scrollY - centroid.y)
                        coroutineScope.launch {
                            scrollState.scrollTo(scrollY.roundToInt() - scrollOffset.roundToInt())
                        }

                    }
                }

        ) { (hoursLabelMeasurables, dividerBarMeasurables, currentTimeMeasurables, currentTimeDividerMeasurables), constraints ->

            val hoursPlaceable = hoursLabelMeasurables.map { measurable ->
                measurable.measure(constraints)
            }

            // note the proper placement height wrt to zoom level is known only to divider
            var totalHeight = 0
            val dividersPlaceable = dividerBarMeasurables.map { measurable ->
                val placeable = measurable.measure(constraints)
                totalHeight += placeable.height
                placeable
            }

            val currentTimePlaceable = currentTimeMeasurables.first().measure(constraints)
            val currentTimeDividerPlaceable =
                currentTimeDividerMeasurables.first().measure(constraints)

            val totalWidth = dividersPlaceable.first().width

            val singleHourHeight: Int = dividersPlaceable.first().height

            layout(totalWidth, totalHeight) {
                val xPos = 0
                var yPos = singleHourHeight
                // fractional hour length (5:30am = 5.5hrs) * length of each hour
                val currentHourY = (currentTimePos * singleHourHeight).roundToInt()

                val timeHeight = currentTimePlaceable.height - 8.dp.toPx().roundToInt()
                val hourTextHeight = hoursPlaceable.first().height

                dividersPlaceable.forEachIndexed { index, dividerPlaceable ->
                    if ((currentHourY !in yPos..yPos + hourTextHeight) && (currentHourY + timeHeight !in yPos..yPos + hourTextHeight)) {
                        hoursPlaceable[index].place(xPos, yPos)
                    }
                    dividerPlaceable.place(xPos, yPos)
                    yPos += dividerPlaceable.height
                }
                currentTimePlaceable.place(xPos, (currentHourY - currentTimePlaceable.height / 2))
                currentTimeDividerPlaceable.place(currentTimePlaceable.width, currentHourY)

            }
        }
    }
}

@Preview
@Composable
private fun TimelineLayoutPreview() {
    TimelineLayout(
        hourLabels = { HourLabels() },
        currentTimeComposable = { CurrentTimeText() },
        currentTimeDivider = { CurrentTimeDivider() },
    )
}


/**[PointerInputScope.detectTransformGestures] but only consumes the zoom events**/
private suspend fun PointerInputScope.customDetectZoom(
    onGesture: (centroid: Offset, zoom: Float) -> Unit,
) {
    awaitEachGesture {
        var zoom = 1f
//        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
//        var lockedToPanZoom = false

        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.fastAny { it.isConsumed }
            if (!canceled) {
                val zoomChange = event.calculateZoom()
//                val rotationChange = event.calculateRotation()
//                val panChange = event.calculatePan()

                if (!pastTouchSlop) {
                    zoom *= zoomChange
//                    rotation += rotationChange
//                    pan += panChange

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
//                    val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
//                    val panMotion = pan.getDistance()

                    if (zoomMotion > touchSlop
                    ) {
                        pastTouchSlop = true
//                        lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                    }
                }

                if (pastTouchSlop) {
                    val centroid = event.calculateCentroid(useCurrent = false)
////                    val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                    if (zoomChange != 1f) {
                        onGesture(centroid, zoomChange)
                    }
                    event.changes.fastForEach {
                        if (it.positionChanged()) {
                            it.consume()
                        }
                    }
                }
            }
        } while (!canceled && event.changes.fastAny { it.pressed })
    }
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
        }
    )
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
        currentTimeText,
        style = MaterialTheme.typography.bodySmall,
        color = errorColor
    )
}
