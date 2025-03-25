package nish.wry.salamander.ui.suBase.category

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import nish.wry.salamander.R
import nish.wry.salamander.data.Constants
import nish.wry.salamander.ui.AppViewModelProvider
import kotlin.math.roundToInt

@Serializable
object CreateCategoryDestination

@Serializable
data class EditCategoryDestination(
    val categoryId: Int,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    exitScreen: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // so that the screen is scrollable in small windows
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(
                    start = dimensionResource(R.dimen.start_end_padding),
                    end = dimensionResource(R.dimen.start_end_padding)
                )
        ) {

            TextField(
                value = uiState.categoryName,
                onValueChange = viewModel::updateCategoryName,
                label = { Text("Category Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.top_large_padding))
            )

            // TODO maybe it should use a interaction source, so that the appears clickable, (wrap it in a surface)
            // whatever its for later
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text("get a notification based on nearing end of goal time")
                    }
                },
                state = rememberTooltipState()
            ) {
                Text(
                    "Set goal Time",
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.top_large_padding))
                )
            }


            TimeSlider(
                updateGoalHrs = viewModel::updateGoalHrs,
                updateGoalMins = viewModel::updateGoalMins,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.top_medium_padding))
            )



            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text("Description") },
                modifier = Modifier.padding(
                    top = dimensionResource(R.dimen.top_large_padding)
                )
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.onSaveButtonClicked()
                        exitScreen()
                    }
                },
                enabled = uiState.validState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.top_large_padding))
            ) {
                Text(stringResource(R.string.save))
            }

            OutlinedButton(
                onClick = exitScreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.top_small_padding))
            ) {
                Text(stringResource(R.string.cancel))
            }


        }
    }
}

@Composable
private fun TimeSlider(
    updateGoalHrs: (Float) -> Unit,
    updateGoalMins: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var minuteSliderPosition by rememberSaveable { mutableFloatStateOf(Constants.GOAL_TIME_SLIDER_DEFAULT_MINS.toFloat()) }
    var hoursSliderPosition by rememberSaveable { mutableFloatStateOf(Constants.GOAL_TIME_SLIDER_DEFAULT_HRS.toFloat()) }

    val minStart = 0f
    val minEnd = 55f

    val hourStart = 0f
    val hourEnd = 23f

    val minsString = minuteSliderPosition.roundToInt().toString()
    val hrsString = hoursSliderPosition.roundToInt().toString()

    Column(modifier = modifier) {

        Text("set hours : $hrsString hrs")

        Slider(
            value = hoursSliderPosition,
            onValueChange = { pos -> hoursSliderPosition = pos },
            onValueChangeFinished = { updateGoalHrs(hoursSliderPosition) },
            // calculating how many points we have in between
            steps = (hourEnd.toInt() - hourStart.toInt()) - 1,
            valueRange = hourStart..hourEnd
        )


        Text(
            "set minutes : $minsString mins",
            modifier = Modifier.padding(top = dimensionResource(R.dimen.top_medium_padding))
        )

        Slider(
            value = minuteSliderPosition,
            onValueChange = { pos -> minuteSliderPosition = pos },
            steps = ((minEnd.toInt() - minStart.toInt()) / 5) - 1,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.secondary,
                activeTrackColor = MaterialTheme.colorScheme.secondary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            valueRange = minStart..minEnd,
            onValueChangeFinished = { updateGoalMins(minuteSliderPosition) },
        )
    }
}