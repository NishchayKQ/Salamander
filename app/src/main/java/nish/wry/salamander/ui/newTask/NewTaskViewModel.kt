package nish.wry.salamander.ui.newTask

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import nish.wry.salamander.data.Priority
import nish.wry.salamander.data.Week
import nish.wry.salamander.data.or
import nish.wry.salamander.data.room.Chip
import nish.wry.salamander.data.room.Task
import nish.wry.salamander.di.GetAllChipsUseCase
import nish.wry.salamander.di.TaskRepository
import java.util.Calendar

class NewTaskViewModel(
    private val repository: TaskRepository,
    getAllChipsUseCase: GetAllChipsUseCase,
) : ViewModel() {


    val chips: StateFlow<List<Chip>> = getAllChipsUseCase(viewModelScope)

    private val _taskUiStateUiState = MutableStateFlow(TaskUiState())
    val taskUiState = _taskUiStateUiState.asStateFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()


    fun setOrResetBitFlagForWeekday(boolean: Boolean, week: Week) {
        _taskUiStateUiState.update { cur ->
            if (boolean) {
                cur.copy(selectedWeekDaysBitmask = cur.selectedWeekDaysBitmask.or(week))
            } else {
                cur.copy(selectedWeekDaysBitmask = cur.selectedWeekDaysBitmask and week.inv())
            }
        }
    }

    fun onChipSelected(chipId: Int) {
        _taskUiStateUiState.update { cur ->
            cur.copy(chipId = chipId)
        }
    }

    fun onTaskNameChange(query: String) {
        _taskUiStateUiState.update { cur ->
            cur.copy(
                taskName = query, isTaskNameValid = query.isNotBlank()
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
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun setTime(timePickerState: TimePickerState) {
        taskUiState.value.selectedTime.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
        taskUiState.value.selectedTime.set(Calendar.MINUTE, timePickerState.minute)
        toggleShowTimePicker()
    }

    fun onTimelessSwitchClick() {
        _taskUiStateUiState.update { cur ->
            cur.copy(timeless = !cur.timeless)
        }
    }

    fun onSegmentedButtonPriorityClick(priority: Priority) {
        _taskUiStateUiState.update { cur ->
            cur.copy(priority = priority)
        }
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
    }

    suspend fun onSaveTaskClicked(uiState: TaskUiState = taskUiState.value) {
        if (uiState.taskName.isNotBlank() && uiState.chipId != null) {
            repository.createTask(uiState.toTask())
            _taskUiStateUiState.update { TaskUiState() }
            _uiState.update { UiState() }
        }
    }


}

data class TaskUiState(
    val taskName: String = "",
    val selectedTime: Calendar = Calendar.getInstance(),
    val selectedWeekDaysBitmask: Int = 0,
    val chipId: Int? = null,
    val priority: Priority = Priority.Normal,
    val timeless: Boolean = false,
    val offsetHours: Int = 1,
    val isTaskNameValid: Boolean = false,
)

data class UiState(
    val fastTimeIoInput: String = "",
    val showTimePicker: Boolean = false,
    val offsetHoursString: String = "1",
)

// TODO we need some default chips that cant be deleted or something in db
fun TaskUiState.toTask(): Task {
    return Task(
        name = taskName,
        repeatOnDaysBitFlag = if (!timeless) selectedWeekDaysBitmask else 0,
        dateTime = if (!timeless) selectedTime else null,
        floatingOffsetHours = if (timeless) offsetHours else null,
        taskChipId = chipId!!,
        priority = priority
    )
}