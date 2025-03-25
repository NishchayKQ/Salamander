package nish.wry.salamander.ui.taskTab.task

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import nish.wry.salamander.data.MutableSaveStateFlow
import nish.wry.salamander.data.Priority
import nish.wry.salamander.data.Week
import nish.wry.salamander.data.or
import nish.wry.salamander.data.room.task.Chip
import nish.wry.salamander.data.room.task.Task
import nish.wry.salamander.di.GetAllChipsUseCase
import nish.wry.salamander.di.TaskRepository
import nish.wry.salamander.ui.navigation.EditTaskDestination
import nish.wry.salamander.ui.taskTab.chip.ChipOrTaskUiState
import nish.wry.salamander.ui.taskTab.chip.UiState
import java.util.Calendar

class CreateTaskViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository,
    getAllChipsUseCase: GetAllChipsUseCase,
) : ViewModel() {

    @OptIn(ExperimentalSerializationApi::class)
    private val taskId: Int? = try {
        savedStateHandle.toRoute<EditTaskDestination>().taskId
    } catch (e: MissingFieldException) {
        null
    }

    init {
        if (taskId != null) {
            viewModelScope.launch {
                val fetchedTaskUiState = repository.getTaskWithId(taskId).first().toTaskUiState()
                _taskUiStateUiState.update { fetchedTaskUiState }
                _uiState.update { UiState(offsetHoursString = fetchedTaskUiState.offsetHours.toString()) }
            }
        }
    }

    val chips: StateFlow<List<Chip>> = getAllChipsUseCase(viewModelScope)

    private val _taskUiStateUiState =
        MutableSaveStateFlow(
            savedStateHandle = savedStateHandle,
            key = TASK_UI_STATE_KEY,
            defaultValue = ChipOrTaskUiState()
        )
    val taskUiState = _taskUiStateUiState.asStateFlow()

    private val _uiState = MutableSaveStateFlow(
        savedStateHandle = savedStateHandle,
        key = UI_STATE_KEY,
        defaultValue = UiState()
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()


    fun setOrResetBitFlagForWeekday(boolean: Boolean, week: Week) {
        _taskUiStateUiState.update { cur ->
            if (boolean) {
                cur.copy(selectedWeekDaysBitmask = cur.selectedWeekDaysBitmask.or(week))
            } else {
                cur.copy(selectedWeekDaysBitmask = cur.selectedWeekDaysBitmask and week.inv())
            }
        }
        verifyState()
    }

    fun onChipSelected(chipId: Int) {
        _taskUiStateUiState.update { cur ->
            cur.copy(chipId = chipId, isEntryValid = cur.name.isNotBlank())
        }
    }

    fun onTaskNameChange(query: String) {
        _taskUiStateUiState.update { cur ->
            cur.copy(
                name = query, isEntryValid = query.isNotBlank() && cur.chipId != null
            )
        }
    }

    fun onFastIoInputChange(query: String) {
        _uiState.update { cur ->
            cur.copy(fastTimeIoInput = query)
        }
    }

    fun toggleShowTimePicker() {
        _uiState.update { cur ->
            cur.copy(
                showTimePicker = !cur.showTimePicker
            )
        }
    }

    fun resetSelectedTimeToCurrentTime() {
        _taskUiStateUiState.update { cur ->
            cur.copy(selectedTime = Calendar.getInstance())
        }
        verifyState()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun setCalToTimePickerState(cal: Calendar, timePickerState: TimePickerState): Calendar {
        cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
        cal.set(Calendar.MINUTE, timePickerState.minute)
        return cal
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun setTime(timePickerState: TimePickerState) {
        _taskUiStateUiState.update { cur ->
            cur.copy(selectedTime = setCalToTimePickerState(cur.selectedTime, timePickerState))
        }
        toggleShowTimePicker()
        verifyState()
    }

    fun onTimelessSwitchClick() {
        _taskUiStateUiState.update { cur ->
            cur.copy(timeless = !cur.timeless)
        }
        verifyState()
    }

    fun onSegmentedButtonPriorityClick(priority: Priority) {
        _taskUiStateUiState.update { cur ->
            cur.copy(priority = priority)
        }
        verifyState()
    }

    fun onOffsetTimeChange(inputString: String) {
        val number = inputString.toIntOrNull()
        if ((number != null && number in 1..23) || inputString.isBlank()) {
            _uiState.update { cur ->
                cur.copy(
                    offsetHoursString = inputString
                )
            }
            if (number != null) {
                _taskUiStateUiState.update { cur ->
                    cur.copy(
                        offsetHours = number
                    )
                }
            }
        }
        verifyState()
    }

    suspend fun onSaveTaskClicked(uiState: ChipOrTaskUiState = taskUiState.value) {
        if (uiState.name.isNotBlank() && uiState.chipId != null) {
            if (taskId != null) {
                repository.updateTask(uiState.toTask(taskId))
            } else {
                repository.createTask(uiState.toTask())
            }
            _taskUiStateUiState.update { ChipOrTaskUiState() }
            _uiState.update { UiState() }
        }
    }

    private fun verifyState() {
        _taskUiStateUiState.update { cur ->
            cur.copy(isEntryValid = cur.name.isNotBlank() && cur.chipId != null)
        }
    }

    private companion object {
        private const val UI_STATE_KEY = "UI_STATE_KEY"
        private const val TASK_UI_STATE_KEY = "TASK_UI_STATE_KEY"
    }


}

// TODO we need some default chips that cant be deleted or something in db
fun ChipOrTaskUiState.toTask(taskId: Int = 0): Task {
    return Task(
        id = taskId,
        name = name,
        repeatOnDaysBitFlag = if (!timeless) selectedWeekDaysBitmask else 0,
        dateTime = if (!timeless) selectedTime else null,
        offsetHours = if (timeless) offsetHours else null,
        chipId = chipId!!,
        priority = priority
    )
}

fun Task.toTaskUiState(): ChipOrTaskUiState =
    ChipOrTaskUiState(
        chipId = chipId,
        name = name,
        selectedTime = dateTime ?: Calendar.getInstance(),
        selectedWeekDaysBitmask = repeatOnDaysBitFlag,
        priority = priority,
        timeless = dateTime == null,
        offsetHours = offsetHours ?: 1,
        isEntryValid = false
    )
