package nish.wry.salamander.ui.task

import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import nish.wry.salamander.R
import nish.wry.salamander.data.Constants
import nish.wry.salamander.ui.AppViewModelProvider
import nish.wry.salamander.ui.navigation.NavigationDestination
import nish.wry.salamander.ui.navigation.Routes
import java.util.Calendar

object TaskDestination : NavigationDestination {
    override val route = Routes.Task
    override val titleRes: Int = R.string.task
    override val icon = R.drawable.outline_checklist_24
}

@Composable
fun TaskScreen(
    viewModel: TaskViewModel = viewModel(factory = AppViewModelProvider.Factory),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Timeline(
            uiState = uiState,
            updateScaleAndOffset = viewModel::updateZoomAndScroll
        )
    }
}

@Composable
fun Timeline(
    uiState: TaskUiState,
    updateScaleAndOffset: (scale: Float, offsetY: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val hourHeight = Constants.HOUR_HEIGHT.dp

    var scale by rememberSaveable { mutableFloatStateOf(uiState.scale) }
    var offsetY by rememberSaveable { mutableFloatStateOf(uiState.scrollOffset) }

    var currentTime by rememberSaveable { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(Unit) {
        while (true){
            delay(60_000)
            currentTime = Calendar.getInstance()
        }
    }

    val curMins = currentTime[Calendar.MINUTE]
    val curHours = currentTime[Calendar.HOUR_OF_DAY]
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)



    Canvas(
        modifier = modifier
            .fillMaxSize()
            // TODO its the villain, this thing makes a 'closure' that fucks the world up https://stackoverflow.com/questions/78395751/why-is-the-status-reset-in-compose
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val oldScale = scale


                    scale = (scale * zoom).coerceIn(0.5f, 5f)

                    val scaleFactor = scale / oldScale

                    //see deepseek for upperbound
                    offsetY =
                        (((offsetY + centroid.y) * scaleFactor) - centroid.y - pan.y).coerceAtLeast(
                            0f
                        )
                    updateScaleAndOffset(scale, offsetY)
                }
            }

    ) {
        val scaledHourHeight = hourHeight.toPx() * scale

        val startHour = (offsetY / scaledHourHeight).toInt().coerceAtLeast(0)
        val endHour = (((offsetY + size.height) / scaledHourHeight).toInt()).coerceAtMost(24)

        val currentTimeHeight: Float = ((scaledHourHeight) * (curHours + (curMins / 60f))) - offsetY
        drawLine(
            color = Color.Red,
            start = Offset(0f, currentTimeHeight),
            end = Offset(size.width, currentTimeHeight),
            strokeWidth = 4f
        )


        for (hour in startHour..endHour) {

            val yPos = hour * scaledHourHeight - offsetY

            drawText(
                textMeasurer = textMeasurer,
                text = if (is24Hour) {
                    hour.toString()
                } else {
                    "${hour % 12}"
                },
                topLeft = Offset(0f, yPos),

                )
            drawLine(
                color = Color.Gray,
                start = Offset(0f, yPos),
                end = Offset(size.width, yPos),
            )

        }

    }

}

@Preview
@Composable
fun TaskScreenPreview() {
    TaskScreen()
}