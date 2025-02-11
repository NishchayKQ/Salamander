package nish.wry.salamander.ui.chip.create

import android.os.Parcelable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parcelize
import nish.wry.salamander.data.MutableSaveStateFlow
import nish.wry.salamander.data.Priority
import nish.wry.salamander.data.Week
import nish.wry.salamander.data.or
import nish.wry.salamander.data.room.Chip
import nish.wry.salamander.di.TaskRepository
import java.util.Calendar

class CreateChipViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository,
) : ViewModel() {
    
    private val _chipUiState = MutableSaveStateFlow(
        savedStateHandle = savedStateHandle,
        key = CHIP_UI_STATE_KEY,
        defaultValue = ChipUiState()
    )


    private val _uiState = MutableSaveStateFlow(
        savedStateHandle = savedStateHandle,
        key = UI_STATE_KEY,
        defaultValue = UiState()
    )

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

    private companion object {
        private const val CHIP_UI_STATE_KEY = "CHIP_UI_STATE_KEY"
        private const val UI_STATE_KEY = "UI_STATE"
    }


}

@Parcelize
data class ChipUiState(
    val name: String = "",
    val selectedTime: Calendar = Calendar.getInstance(),
    val selectedWeekDaysBitmask: Int = 0,
    val priority: Priority = Priority.Normal,
    val timeless: Boolean = false,
    val offsetHours: Int = 1,
    val isInputValid: Boolean = false,
) : Parcelable

@Parcelize
data class UiState(
    val showTimePicker: Boolean = false,
    val fastTimeIoInput: String = "",
    val floatingOffsetString: String = "1",
) : Parcelable

fun ChipUiState.toChip(): Chip {
    return Chip(
        name = name,
        repeatOnDaysBitFlag = if (!timeless) selectedWeekDaysBitmask else 0,
        dateTime = if (!timeless) selectedTime else null,
        floatingOffsetHours = if (timeless) offsetHours else null,
        priority = priority
    )
}