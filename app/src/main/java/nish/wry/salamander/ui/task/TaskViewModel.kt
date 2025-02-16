package nish.wry.salamander.ui.task

import android.os.Parcelable
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import nish.wry.salamander.data.Constants
import nish.wry.salamander.data.MutableSaveStateFlow
import nish.wry.salamander.data.TaskDataSource
import nish.wry.salamander.data.room.Chip
import nish.wry.salamander.di.GetAllChipsUseCase
import nish.wry.salamander.di.TaskRepository

class TaskViewModel(
    val taskDataSource: TaskDataSource,
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle,
    getAllChipsUseCase: GetAllChipsUseCase,
) : ViewModel() {

    // timeline stuff
    private val _timelineUiState = MutableSaveStateFlow(
        savedStateHandle = savedStateHandle,
        key = TIMELINE_UI_STATE_KEY,
        defaultValue = TimelineUiState()
    )
    val timelineUiState = _timelineUiState.asStateFlow()

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

    // TODO the scope and stuff, repo should put it in recycle bin with today's date as deleted date, delete in 30 days
    fun onDeleteTaskClicked(taskId: Int) {
        viewModelScope.launch {
            val task = repository.getTaskWithId(taskId).first()
            repository.deleteTask(task)
        }
    }

    fun saveScrollState(scroll: Int, scale: Float) {
        _timelineUiState.update { cur ->
            cur.copy(scrollValue = scroll, scale = scale)
        }
    }

    var goodBoiMap = mutableMapOf<Int, StateFlow<List<TaskDrawingData>>>()
        private set

    init {
        viewModelScope.launch {
            addPage(Constants.HALF_PAGE_LIMIT)

            addPage(Constants.HALF_PAGE_LIMIT + 1)
            // 1 day before
            addPage(Constants.HALF_PAGE_LIMIT - 1)
        }
    }

    // zero == Constants.PAGER_LIMIT / 2 == 50,000
    fun addPage(key: Int) {
        if (key !in goodBoiMap) {
            goodBoiMap[key] = taskDataSource[key - Constants.HALF_PAGE_LIMIT].stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                listOf()
            )
//        TODO check if while subscribed stops indefinitely
//        }.let { existingFlow ->
//            if (existingFlow.subscriptionCount.value == 0) {
//                // Restart collection if stale
//                existingFlow.reset()
//            }
        }
    }

    private companion object {
        private const val TIMELINE_UI_STATE_KEY = "TIMELINE_UI_STATE_KEY"
        private const val TASK_SCREEN_UI_STATE_KEY = "TASK_SCREEN_UI_STATE_KEY"
        private const val TIMEOUT_MILLIS = 5_000L
    }

}

@Parcelize
data class TimelineUiState(
    val scale: Float = 1.5f,
    val scrollValue: Int = 0,
    val selectedChipIDs: Set<Int> = emptySet(),
) : Parcelable

@Parcelize
data class TaskScreenUiState(
    val query: String = "",
    val expanded: Boolean = false,
) : Parcelable

data class TaskDrawingData(
    val id: Int,
    val name: String,
    val chipId: Int,
    val modifier: Modifier,
)