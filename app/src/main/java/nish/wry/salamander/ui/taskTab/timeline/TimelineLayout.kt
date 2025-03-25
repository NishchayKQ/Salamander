package nish.wry.salamander.ui.taskTab.timeline

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.launch
import nish.wry.salamander.data.Constants
import nish.wry.salamander.ui.taskTab.main.TaskDrawingData
import nish.wry.salamander.ui.taskTab.timeline.TimelineScope.taskData
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TimelineLayout(
    isToday: Boolean,
    hourLabels: @Composable () -> Unit,
    dividerBars: @Composable () -> Unit,
    currentTimeComposable: @Composable () -> Unit,
    currentTimeDivider: @Composable () -> Unit,
    tasksComposable: @Composable () -> Unit,
    saveScrollAndScale: (scroll: Int, scaleY: Float) -> Unit,
    currentTimeInHours: Float,
    scrollValue: Int,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    var scaleY by rememberSaveable { mutableFloatStateOf(scale) }
    val scrollState = rememberScrollState(scrollValue)

    DisposableEffect(Unit) {
        onDispose {
            saveScrollAndScale(scrollState.value, scaleY)
        }
    }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Layout(
            contents = listOf(
                hourLabels, dividerBars, currentTimeComposable, currentTimeDivider, tasksComposable
            ), modifier = modifier.pointerInput(Unit) {
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

        ) { (hoursLabelMeasurables, dividerBarMeasurables, currentTimeMeasurables, currentTimeDividerMeasurables, tasksMeasurables), constraints ->

            val avgHeight = (scaleY * Constants.HOUR_HEIGHT).dp.toPx().roundToInt()

            val hoursPlaceable = hoursLabelMeasurables.map { measurable ->
                measurable.measure(constraints)
            }

            // note the proper placement height wrt to zoom level is known only to divider
            var totalHeight = 0
            val dividersPlaceable = dividerBarMeasurables.map { measurable ->
                val placeable = measurable.measure(
                    constraints.copy(
                        minHeight = avgHeight,
                        maxHeight = avgHeight
                    )
                )
                totalHeight += placeable.height
                placeable
            }

            val currentTimePlaceable = currentTimeMeasurables.first().measure(constraints)
            val currentTimeDividerPlaceable =
                currentTimeDividerMeasurables.first().measure(constraints)

            val totalWidth = dividersPlaceable.first().width

            // the arrow's right most cord is 15.dp, text inside task box is 4.dp, so we add 11.dp
            val xStartOffset = currentTimePlaceable.width + 11.dp.toPx().roundToInt()
            val widthAvailableForTasks = dividersPlaceable.first().width - xStartOffset

            val singleHourHeight: Int = dividersPlaceable.first().height

            val taskVerticalPadding = 1.dp.toPx().roundToInt()
            val taskHorizontalPadding = 2.dp.toPx().roundToInt()
            val tasksPlaceable = tasksMeasurables.map { measurable ->
                val taskParentData = measurable.parentData as TaskParentData
                val taskWidth =
                    (widthAvailableForTasks / taskParentData.maxSimultaneous) - taskHorizontalPadding
                // we multiply by 2 as we need the padding both on top and bottom
                val taskHeight =
                    (((taskParentData.endMins - taskParentData.startMins) * singleHourHeight) / 60) - (2 * taskVerticalPadding)
                measurable.measure(
                    Constraints(
                        minWidth = taskWidth,
                        maxWidth = taskWidth,
                        minHeight = taskHeight,
                        maxHeight = taskHeight
                    )
                )
            }


            layout(totalWidth, totalHeight) {
                tasksPlaceable.forEach { taskPlaceable ->
                    val taskParentData = taskPlaceable.parentData as TaskParentData
                    val taskOffset =
                        ((widthAvailableForTasks / taskParentData.maxSimultaneous) * taskParentData.index)

                    taskPlaceable.place(
                        x = xStartOffset + taskOffset,
                        y = ((taskParentData.startMins * singleHourHeight) / 60) + taskVerticalPadding
                    )
                }

                val xPos = 0
                var yPos = singleHourHeight
                // fractional hour length (5:30am = 5.5hrs) * length of each hour
                val currentHourY = (currentTimeInHours * singleHourHeight).roundToInt()

                val timeHeight = currentTimePlaceable.height - 8.dp.toPx().roundToInt()
                val hourTextHeight = hoursPlaceable.first().height

                dividersPlaceable.forEachIndexed { index, dividerPlaceable ->
                    if (!isToday || ((currentHourY !in yPos..yPos + hourTextHeight) && (currentHourY + timeHeight !in yPos..yPos + hourTextHeight))) {
                        hoursPlaceable[index].place(xPos, yPos)
                    }
                    dividerPlaceable.place(xPos, yPos)
                    yPos += dividerPlaceable.height
                }

                if (isToday) {
                    // place them at top ie last
                    currentTimePlaceable.place(
                        xPos, (currentHourY - currentTimePlaceable.height / 2)
                    )
                    currentTimeDividerPlaceable.place(currentTimePlaceable.width, currentHourY)
                }
            }
        }
    }
}


@Preview
@Composable
private fun TimelineLayoutPreview() {
    val is24Hour = false
    TimelineLayout(
        isToday = true,
        hourLabels = { HourLabels(is24Hour = is24Hour) },
        dividerBars = { },
        currentTimeComposable = { CurrentTimeText(is24Hour, LocalTime.now()) },
        currentTimeDivider = { CurrentTimeDivider() },
        tasksComposable = {
            TasksBox(
                taskDrawingDataList = listOf(
                    TaskDrawingData(
                        0,
                        "meow",
                        1,
                        Modifier.taskData(
                            index = 0,
                            maxSimultaneous = 1,
                            startMins = 330,
                            endMins = 360
                        )
                    )
                ),
                chipIdsSelected = setOf(),
                onDeleteTaskClicked = {},
                onTaskClicked = {}
            )
        },
        currentTimeInHours = 7f,
        saveScrollAndScale = { _, _ -> },
        scrollValue = 0,
        scale = 1.5f,
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

                    if (zoomMotion > touchSlop) {
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


