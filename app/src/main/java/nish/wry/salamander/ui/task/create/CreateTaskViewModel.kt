package nish.wry.salamander.ui.task.create

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import nish.wry.salamander.data.MutableSaveStateFlow
import nish.wry.salamander.data.Priority
import nish.wry.salamander.data.Week
import nish.wry.salamander.data.or
import nish.wry.salamander.data.room.Chip
import nish.wry.salamander.data.room.Task
import nish.wry.salamander.di.GetAllChipsUseCase
import nish.wry.salamander.di.TaskRepository
import nish.wry.salamander.ui.chip.create.ChipOrTaskUiState
import nish.wry.salamander.ui.chip.create.UiState
import java.util.Calendar

class CreateTaskViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository,
    getAllChipsUseCase: GetAllChipsUseCase,
) : ViewModel() {


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

    suspend fun onSaveTaskClicked(uiState: ChipOrTaskUiState = taskUiState.value) {
        if (uiState.name.isNotBlank() && uiState.chipId != null) {
            repository.createTask(uiState.toTask())
            _taskUiStateUiState.update { ChipOrTaskUiState() }
            _uiState.update { UiState() }
        }
    }

    private companion object {
        private const val UI_STATE_KEY = "UI_STATE_KEY"
        private const val TASK_UI_STATE_KEY = "TASK_UI_STATE_KEY"
    }


}

// TODO we need some default chips that cant be deleted or something in db
fun ChipOrTaskUiState.toTask(): Task {
    return Task(
        name = name,
        repeatOnDaysBitFlag = if (!timeless) selectedWeekDaysBitmask else 0,
        dateTime = if (!timeless) selectedTime else null,
        floatingOffsetHours = if (timeless) offsetHours else null,
        taskChipId = chipId!!,
        priority = priority
    )
}