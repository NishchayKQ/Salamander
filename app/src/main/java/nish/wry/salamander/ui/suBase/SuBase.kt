package nish.wry.salamander.ui.suBase

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import nish.wry.salamander.R
import nish.wry.salamander.data.room.suBase.ActivityUiData
import nish.wry.salamander.data.room.suBase.CategoryUiData
import nish.wry.salamander.ui.AppViewModelProvider
import nish.wry.salamander.ui.navigation.MainDestination
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Serializable
object MainSuBaseDestination : MainDestination {
    override val titleRes: Int = R.string.subase
    override val iconRes: Int = R.drawable.outline_timer_24
}

@Serializable
object SuBaseScreenDestination

@Composable
fun SuBase(
    onAddCategoryClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubBaseViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val suBaseUiState by viewModel.suBaseUiState.collectAsState()
    val is24Hour by viewModel.is24Hour.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()

    val currentActivityInterval by viewModel.currentActivity.collectAsState()
    val activityHistoryList by viewModel.activityUiDataFlow.collectAsState()
    val categoryList by viewModel.categoryListFlow.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val dtf = DateTimeFormatter.ofPattern(if (is24Hour) "H:mm" else "h:mm a")

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(
                    start = dimensionResource(R.dimen.start_end_padding),
                    end = dimensionResource(R.dimen.start_end_padding)
                )
        ) {
            if (currentActivityInterval == null) {

                CategorySelector(
                    categoryList,
                    selectedCategoryId = suBaseUiState.selectedCategoryId,
                    onCategorySelected = viewModel::updateSelectedCategoryId,
                    onAddCategoryClicked = onAddCategoryClicked
                )

                Button(
                    onClick = { coroutineScope.launch { viewModel.startActivity() } },
                    enabled = suBaseUiState.startTrackingButtonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = dimensionResource(R.dimen.top_small_padding),
                            bottom = dimensionResource(R.dimen.bottom_large_padding),
                        )
                ) {
                    Text("Start")
                }

            }

            if (currentActivityInterval != null) {
                Text(
                    "currently ${currentActivityInterval?.categoryName} since " +
                            "${currentActivityInterval?.start?.format(dtf)}"
                )

                Button(
                    onClick = { coroutineScope.launch { viewModel.endCurrentActivity() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = dimensionResource(R.dimen.top_small_padding),
                            bottom = dimensionResource(R.dimen.bottom_large_padding),
                        )
                ) {
                    Text("End activity")
                }
            }

            ActivityHistory(
                activityHistoryList = activityHistoryList,
                dateTimeFormatter = dtf,
                currentTime = currentTime,
                modifier = Modifier.fillMaxWidth()
            )


        }
    }
}

@Composable
private fun CategorySelector(
    categoryList: List<CategoryUiData>,
    selectedCategoryId: Int,
    onAddCategoryClicked: () -> Unit,
    onCategorySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        // start and end padding are take care by the parent Column
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
        modifier = modifier
            // LazyHorizontalGrid has to be constrained in height.
            // However, we can't set a fixed height because the horizontal grid contains
            // vertical text that can be rescaled.
            // When the fontScale is at most 1, we know that the horizontal grid will be at most
            // 240dp tall, so this is an upper bound for when the font scale is at most 1.
            // When the fontScale is greater than 1, the height required by the text inside the
            // horizontal grid will increase by at most the same factor, so 240sp is a valid
            // upper bound for how much space we need in that case.
            // The maximum of these two bounds is therefore a valid upper bound in all cases.
            .heightIn(max = max(240.dp, with(LocalDensity.current) { 240.sp.toDp() }))
            .fillMaxWidth()
    ) {
        items(categoryList, key = { category -> category.id }) {
            SingleCategoryButton(
                id = it.id,
                name = it.name,
                isSelected = selectedCategoryId == it.id,
                onCategorySelected = onCategorySelected
            )
        }
        item {
            AddCategory(onClick = onAddCategoryClicked)
        }
    }
}

// to make this reusable we make it receive name, id, and stuff directly instead of CategoryUiData
// like now in android does
@Composable
private fun SingleCategoryButton(
    id: Int,
    name: String,
    isSelected: Boolean,
    onCategorySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(corner = CornerSize(8.dp)),
        onClick = { onCategorySelected(id) },
        color = MaterialTheme.colorScheme.surface,
        selected = isSelected,
        modifier = modifier
            .width(312.dp)
            .heightIn(min = 56.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = 8.dp),
        ) {
            // TODO category icons
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (isSelected) {
                Icon(Icons.Default.Check, null)
            }
        }

    }
}

@Composable
private fun AddCategory(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
        IconButton(onClick = onClick) {
            Icon(Icons.Default.Add, null)
        }
    }
}

@Composable
private fun ActivityHistory(
    activityHistoryList: List<ActivityUiData>,
    currentTime: LocalTime,
    dateTimeFormatter: DateTimeFormatter,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text("Today's Activities")
        activityHistoryList.forEach { activity ->
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "%.2f hrs".format(
                        activity.duration ?: (ChronoUnit.MINUTES.between(
                            activity.start, currentTime
                        ) / 60f)
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.End)
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "${activity.start.format(dateTimeFormatter)} - ${
                            activity.end?.format(
                                dateTimeFormatter
                            ) ?: "now"
                        } ",
//                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(activity.categoryName)
                }

                HorizontalDivider()
            }
        }
    }
}

@Composable
@Preview
private fun ActivityHistoryPreview() {
    ActivityHistory(
        listOf(),
        LocalTime.now(),
        dateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    )
}
