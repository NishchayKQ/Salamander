package nish.wry.salamander.ui.task

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.parcelize.Parcelize
import nish.wry.salamander.data.MutableSaveStateFlow
import nish.wry.salamander.data.room.Chip
import nish.wry.salamander.di.GetAllChipsUseCase
import nish.wry.salamander.di.TaskRepository

class TaskViewModel(
    savedStateHandle: SavedStateHandle,
    repository: TaskRepository,
    getAllChipsUseCase: GetAllChipsUseCase,
) : ViewModel() {

    // timeline stuff
    private val _timelineUiState = MutableSaveStateFlow(
        savedStateHandle = savedStateHandle,
        key = TIMELINE_UI_STATE_KEY,
        defaultValue = TimelineUiState()
    )
    val timelineUiState = _timelineUiState.asStateFlow()

    fun updateZoomAndScroll(newScale: Float, newOffsetY: Float) {
        _timelineUiState.update {
            TimelineUiState(scale = newScale, scrollOffset = newOffsetY)
        }
    }

    // searchbar stuff
    private val _taskScreenUiState = MutableSaveStateFlow(
        savedStateHandle = savedStateHandle,
        key = TASK_SCREEN_UI_STATE_KEY,
        defaultValue = TaskScreenUiState()
    )

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

    private companion object {
        private const val TIMELINE_UI_STATE_KEY = "TIME_LINE_UI_STATE"
        private const val TASK_SCREEN_UI_STATE_KEY = "TASK_SCREEN_UI_STATE"
    }

}

@Parcelize
data class TimelineUiState(
    val scale: Float = 1f,
    val scrollOffset: Float = 0f,
    val selectedChipIDs: Set<Int> = emptySet(),
) : Parcelable

@Parcelize
data class TaskScreenUiState(
    val query: String = "",
    val expanded: Boolean = false,
) : Parcelable
