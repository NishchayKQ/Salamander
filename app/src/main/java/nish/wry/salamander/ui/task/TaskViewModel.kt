package nish.wry.salamander.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import nish.wry.salamander.data.room.Chip
import nish.wry.salamander.di.GetAllChipsUseCase
import nish.wry.salamander.di.TaskRepository

class TaskViewModel(
    repository: TaskRepository,
    getAllChipsUseCase: GetAllChipsUseCase,
) : ViewModel() {

    // timeline stuff
    private val _timelineUiState = MutableStateFlow(TimelineUiState())
    val timelineUiState = _timelineUiState.asStateFlow()

    fun updateZoomAndScroll(newScale: Float, newOffsetY: Float) {
        _timelineUiState.update {
            TimelineUiState(scale = newScale, scrollOffset = newOffsetY)
        }
    }

    // searchbar stuff
    private val _taskScreenUiState = MutableStateFlow(TaskScreenUiState())

    val taskScreenUiState = _taskScreenUiState.asStateFlow()

    fun setSearchExpandedState(value: Boolean) {
        _taskScreenUiState.update { cur ->
            cur.copy(expanded = value)
        }
    }

    fun updateSearchQuery(query: String) {
        _taskScreenUiState.update { cur ->
            cur.copy(query = query)
        }
    }

    // chips
    val chips: StateFlow<List<Chip>> = getAllChipsUseCase(viewModelScope)

    fun onChipClicked(chipId: Int) {
        _timelineUiState.update { cur ->
            val updateChipIds = if (chipId in cur.selectedChipIDs) {
                cur.selectedChipIDs - chipId
            } else cur.selectedChipIDs + chipId
            cur.copy(selectedChipIDs = updateChipIds)
        }
    }

}

data class TimelineUiState(
    val scale: Float = 1f,
    val scrollOffset: Float = 0f,
    val selectedChipIDs: Set<Int> = emptySet(),
)

data class TaskScreenUiState(
    val query: String = "",
    val expanded: Boolean = false,
)
