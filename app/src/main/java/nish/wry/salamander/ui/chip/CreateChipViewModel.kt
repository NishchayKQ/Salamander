package nish.wry.salamander.ui.chip

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import nish.wry.salamander.data.Priority
import nish.wry.salamander.data.Week
import nish.wry.salamander.data.or
import nish.wry.salamander.data.room.Chip
import nish.wry.salamander.di.TaskRepository
import java.util.Calendar

class CreateChipViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _chipUiState = MutableStateFlow(ChipUiState())

    private val _uiState = MutableStateFlow(UiState())

    val uiState = _uiState.asStateFlow()

    val chipUiState = _chipUiState.asStateFlow()

    fun onChipNameChange(query: String) {
        _chipUiState.update { cur ->
            cur.copy(name = query, isInputValid = query.isNotBlank())
        }
    }

    fun onFastIoInputChange(query: String) {
        _uiState.update { cur ->
            cur.copy(fastTimeIoInput = query)
        }
    }

    fun onSegmentedButtonPriorityClick(priority: Priority) {
        _chipUiState.update { cur ->
            cur.copy(priority = priority)
        }
    }

    fun onOffsetTimeChange(inputString: String) {
        val number = inputString.toIntOrNull()
        if ((number != null && number in 1..23) || inputString.isBlank()) {
            _uiState.update { cur ->
                cur.copy(
                    floatingOffsetString = inputString
                )
            }
            if (number != null) {
                _chipUiState.update { cur ->
                    cur.copy(
                        offsetHours = number
                    )
                }
            }
        }
    }

    fun setOrResetBitFlagForWeekday(boolean: Boolean, week: Week) {
        _chipUiState.update { cur ->
            if (boolean) {
                cur.copy(selectedWeekDaysBitmask = cur.selectedWeekDaysBitmask or week)
            } else {
                cur.copy(selectedWeekDaysBitmask = cur.selectedWeekDaysBitmask and week.inv())
            }
        }
    }

    fun resetSelectedTimeToCurrentTime() {
        _chipUiState.update { cur ->
            cur.copy(selectedTime = Calendar.getInstance())
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun setTime(timePickerState: TimePickerState) {
        chipUiState.value.selectedTime.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
        chipUiState.value.selectedTime.set(Calendar.MINUTE, timePickerState.minute)

        _chipUiState.update { cur ->
            cur.copy()
        }
        toggleShowTimePicker()
    }

    fun onTimelessSwitchClick() {
        _chipUiState.update { cur ->
            cur.copy(timeless = !cur.timeless)
        }
    }

    fun toggleShowTimePicker() {
        _uiState.update { cur ->
            cur.copy(showTimePicker = !cur.showTimePicker)
        }
    }

    suspend fun saveCreatedChip(uiState: ChipUiState = chipUiState.value) {
        if (uiState.name.isNotBlank()) {
            repository.createChip(chipUiState.value.toChip())
            _chipUiState.update { ChipUiState() }
            _uiState.update { UiState() }
        }
    }


}

data class ChipUiState(
    val name: String = "",
    val selectedTime: Calendar = Calendar.getInstance(),
    val selectedWeekDaysBitmask: Int = 0,
    val priority: Priority = Priority.Normal,
    val timeless: Boolean = false,
    val offsetHours: Int = 1,
    val isInputValid: Boolean = false,
)

data class UiState(
    val showTimePicker: Boolean = false,
    val fastTimeIoInput: String = "",
    val floatingOffsetString: String = "1",
)

fun ChipUiState.toChip(): Chip {
    return Chip(
        name = name,
        repeatOnDaysBitFlag = if (!timeless) selectedWeekDaysBitmask else 0,
        dateTime = if (!timeless) selectedTime else null,
        floatingOffsetHours = if (timeless) offsetHours else null,
        priority = priority
    )
}