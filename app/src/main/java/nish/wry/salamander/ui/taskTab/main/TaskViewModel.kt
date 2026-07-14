package nish.wry.salamander.ui.taskTab.main

import android.os.Parcelable
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import nish.wry.salamander.data.Constants
import nish.wry.salamander.data.DateTimeTracker
import nish.wry.salamander.data.MutableSaveStateFlow
import nish.wry.salamander.data.TaskDataSource
import nish.wry.salamander.data.room.task.Chip
import nish.wry.salamander.domain.repository.TaskRepository
import nish.wry.salamander.domain.usecase.GetAllChipsUseCase
import nish.wry.salamander.scheduler.Scheduler
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    val taskDataSource: TaskDataSource,
    private val repository: TaskRepository,
    dateTimeTracker: DateTimeTracker,
    savedStateHandle: SavedStateHandle,
    getAllChipsUseCase: GetAllChipsUseCase<Chip>,
    private val alarmScheduler: Scheduler
) : ViewModel() {

    val localTime: StateFlow<LocalTime> = dateTimeTracker.currentTime
    val is24Hour: StateFlow<Boolean> = dateTimeTracker.is24Hour
    //    val localDate:Nothing = dateTimeTracker.currentDate.collect { }

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

    fun onDeleteTaskClicked(taskId: Int) {
        viewModelScope.launch {
            val task = repository.getTaskWithId(taskId) ?: return@launch
            repository.deleteTask(task)
            alarmScheduler.cancel(task.id)
        }
    }

    // when a user's scroll is saved we don't need to use firstLoadScrollValue anymore
    fun saveScrollState(scroll: Int, scale: Float) {
        _timelineUiState.update { cur ->
            cur.copy(scrollValue = scroll, scale = scale, firstLoadCompleted = true)
        }
    }

    var goodBoiMap = mutableMapOf<Int, StateFlow<List<TaskDrawingData>>>()
        private set

    init {
        viewModelScope.launch {
            val startDate = taskScreenUiState.value.startDate
            addPage(startDate)

            addPage(startDate + 1)
            // 1 day before
            addPage(startDate - 1)
        }
    }

    // our datasource stores data relative to today, like key=0 means today...
    // so we have to convert it from the pager's way of remembering today to this
    fun addPage(key: Int) {
        if (key !in goodBoiMap) {
            goodBoiMap[key] = taskDataSource[key - taskScreenUiState.value.startDate].stateIn(
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
// #FIXME selected chips remembers the ones that were deleted
/**
 * @param scale the zoom level of the timeline
 * @param scrollValue the user scrolled position
 * @param firstLoadScrollValue current time position in float, convert to dp and then pixels for scrollPosition
 * @param firstLoadCompleted if [TimelineUiState] has been created before, this is for [firstLoadScrollValue], we don't want to override user's scroll value
 * @param selectedChipIDs chips user has toggled
 * **/
@Parcelize
data class TimelineUiState(
    val scale: Float = 1.5f,
    val scrollValue: Int = 0,
    val firstLoadScrollValue: Float = (scale * Constants.HOUR_HEIGHT) * with(LocalTime.now()) { (minute / 60f) + hour },
    val firstLoadCompleted: Boolean = false,
    val selectedChipIDs: Set<Int> = emptySet(),
) : Parcelable

@Parcelize
data class TaskScreenUiState(
    val query: String = "",
    val expanded: Boolean = false,
    val startDate: Int = Constants.HALF_PAGE_LIMIT,
) : Parcelable

data class TaskDrawingData(
    val id: Int,
    val name: String,
    val chipId: Int,
    val modifier: Modifier,
)