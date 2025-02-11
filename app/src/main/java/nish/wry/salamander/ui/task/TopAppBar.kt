package nish.wry.salamander.ui.task

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import nish.wry.salamander.R
import nish.wry.salamander.data.room.Chip


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskTopAppBar(
    chips: List<Chip>,
    selectedChipIds: Set<Int>,
    onChipClicked: (Int) -> Unit,
    searchQuery: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSearch: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = searchQuery,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    expanded = expanded,
                    onExpandedChange = onExpandedChange,
                    placeholder = { Text(stringResource(R.string.search_for_a_task)) }
                )
            },
            expanded = expanded,
            onExpandedChange = onExpandedChange
        ) { }


        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            chips.forEach {
                FilterChip(
                    selected = it.id in selectedChipIds,
                    onClick = { onChipClicked(it.id) },
                    label = { Text(it.name) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }
}

