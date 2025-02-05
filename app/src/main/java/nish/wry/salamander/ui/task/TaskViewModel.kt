package nish.wry.salamander.ui.task

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TaskViewModel : ViewModel() {

    // timeline stuff
    private val _timelineUiState = MutableStateFlow(TimelineUiState())
    val timelineUiState = _timelineUiState.asStateFlow()

    fun updateZoomAndScroll(newScale: Float, newOffsetY: Float) {
        _timelineUiState.update {
            TimelineUiState(scale = newScale, scrollOffset = newOffsetY)
        }
    }

    // searchbar stuff
    private val _searchUiState = MutableStateFlow(SearchBarUiState())

    val searchUiState = _searchUiState.asStateFlow()

    fun setSearchExpandedState(value: Boolean) {
        _searchUiState.update { cur ->
            cur.copy(expanded = value)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchUiState.update { cur ->
            cur.copy(query = query)
        }
    }


}

data class TimelineUiState(
    val scale: Float = 1f,
    val scrollOffset: Float = 0f,
)

data class SearchBarUiState(
    val query: String = "",
    val expanded: Boolean = false,
)