package nish.wry.salamander.ui.chip.create

import android.os.Parcelable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import nish.wry.salamander.data.MutableSaveStateFlow
import nish.wry.salamander.data.Priority
import nish.wry.salamander.data.Week
import nish.wry.salamander.data.or
import nish.wry.salamander.data.room.Chip
import nish.wry.salamander.di.TaskRepository
import nish.wry.salamander.ui.navigation.EditChipDestination
import java.util.Calendar

class CreateChipViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository,
) : ViewModel() {

    @OptIn(ExperimentalSerializationApi::class)
    private val chipId: Int? = try {
        savedStateHandle.toRoute<EditChipDestination>().chipId
    } catch (e: MissingFieldException) {
        null
    }

    init {
        if (chipId != null) {
            viewModelScope.launch {
                val fetchedChipUiState = repository.getChipWithId(chipId).first().toChipUiState()
                _chipUiState.update { fetchedChipUiState }
                _uiState.update { UiState(offsetHoursString = fetchedChipUiState.offsetHours.toString()) }
            }
        }
    }

    private val _chipUiState = MutableSaveStateFlow(
        savedStateHandle = savedStateHandle,
        key = CHIP_UI_STATE_KEY,
        defaultValue = ChipOrTaskUiState()
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
            cur.copy(name = query, isEntryValid = query.isNotBlank())
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
                    offsetHoursString = inputString
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

    suspend fun saveCreatedChip(uiState: ChipOrTaskUiState = chipUiState.value) {
        if (uiState.name.isNotBlank()) {
            if (chipId != null) {
                repository.updateChip(chipUiState.value.toChip())
            } else {
                repository.createChip(chipUiState.value.toChip())
            }
            _chipUiState.update { ChipOrTaskUiState() }
            _uiState.update { UiState() }
        }
    }

    private companion object {
        private const val CHIP_UI_STATE_KEY = "CHIP_UI_STATE_KEY"
        private const val UI_STATE_KEY = "UI_STATE_KEY"
    }


}

@Parcelize
data class ChipOrTaskUiState(
    val chipId: Int? = null,
    val name: String = "",
    val selectedTime: Calendar = Calendar.getInstance(),
    val selectedWeekDaysBitmask: Int = 0,
    val priority: Priority = Priority.Normal,
    val timeless: Boolean = false,
    val offsetHours: Int = 1,
    val isEntryValid: Boolean = false,
) : Parcelable

@Parcelize
data class UiState(
    val showTimePicker: Boolean = false,
    val fastTimeIoInput: String = "",
    val offsetHoursString: String = "1",
) : Parcelable

fun ChipOrTaskUiState.toChip(): Chip {
    return Chip(
        id = chipId ?: 0,
        name = name,
        repeatOnDaysBitFlag = if (!timeless) selectedWeekDaysBitmask else 0,
        dateTime = if (!timeless) selectedTime else null,
        floatingOffsetHours = if (timeless) offsetHours else null,
        priority = priority
    )
}

fun Chip.toChipUiState(): ChipOrTaskUiState =
    ChipOrTaskUiState(
        chipId = id,
        name = name,
        selectedTime = dateTime ?: Calendar.getInstance(),
        selectedWeekDaysBitmask = repeatOnDaysBitFlag,
        priority = priority,
        timeless = dateTime == null,
        offsetHours = floatingOffsetHours ?: 1,
        isEntryValid = false
    )