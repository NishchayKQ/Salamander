package nish.wry.salamander.ui.task

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import nish.wry.salamander.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskSearchBar(
    searchQuery: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSearch: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
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
        onExpandedChange = onExpandedChange,
        modifier = modifier
    ) { }
}