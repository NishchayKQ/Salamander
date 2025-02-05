package nish.wry.salamander.ui.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import nish.wry.salamander.R
import nish.wry.salamander.ui.AppViewModelProvider
import nish.wry.salamander.ui.navigation.NavigationDestination
import nish.wry.salamander.ui.navigation.Routes

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
    val timelineUiState by viewModel.timelineUiState.collectAsState()
    val searchUiState by viewModel.searchUiState.collectAsState()

    Scaffold(
        topBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                TaskSearchBar(
                    searchQuery = searchUiState.query,
                    expanded = searchUiState.expanded,
                    onExpandedChange = viewModel::setSearchExpandedState,
                    onSearch = { viewModel.setSearchExpandedState(false) },
                    onQueryChange = viewModel::updateSearchQuery
                )
            }
        },
        bottomBar = {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {

                TextField(
                    value = "",
                    onValueChange = {},
                    shape = RoundedCornerShape(32.dp),
                    placeholder = { Text("Add event on 5 Feb" ) },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
                FloatingActionButton(
                    onClick = {},
                ) {
                    Icon(painter = painterResource(R.drawable.baseline_add_24), null)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->

        Timeline(
            uiState = timelineUiState,
            updateScaleAndOffset = viewModel::updateZoomAndScroll,
            modifier = Modifier
                .padding(innerPadding)
        )

    }
}


@Preview
@Composable
fun TaskScreenPreview() {
    // get expanded screen preview
    val viewModel: TaskViewModel = viewModel(factory = AppViewModelProvider.Factory)
    viewModel.setSearchExpandedState(false)
    TaskScreen(viewModel = viewModel)
}