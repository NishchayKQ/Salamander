package nish.wry.salamander.ui.taskTab.chip

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
import nish.wry.salamander.data.room.task.Chip
import nish.wry.salamander.di.TaskRepository
import nish.wry.salamander.ui.navigation.EditChipDestination
import java.util.Calendar

// TODO merge both Task and Chip viewmodel with use case...
class CreateChipViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository,
) : ViewModel() {

    @OptIn(ExperimentalSerializationApi::class)
    private val chipId: Int? = try {
        savedStateHandle.toRoute<EditChipDestination>().chipId
    } catch (_: MissingFieldException) {
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
        defaultValue = GenericTaskOrChipUiState()
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
                _chipUiState.update { cur ->
                    cur.copy(
                        offsetHours = number,
                        isEntryValid = cur.isEntryValid || cur.name.isNotBlank()
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
        verifyState()
    }

    fun resetSelectedTimeToCurrentTime() {
        _chipUiState.update { cur ->
            cur.copy(selectedTime = Calendar.getInstance())
        }
        verifyState()
    }

    // TODO shift common viewmodel? though then states will intermix, better is useCase ig?
    @OptIn(ExperimentalMaterial3Api::class)
    private fun setCalToTimePickerState(cal: Calendar, timePickerState: TimePickerState): Calendar {
        cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
        cal.set(Calendar.MINUTE, timePickerState.minute)
        return cal
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun setTime(timePickerState: TimePickerState) {
        _chipUiState.update { cur ->
            cur.copy(selectedTime = setCalToTimePickerState(cur.selectedTime, timePickerState))
        }
        verifyState()
        toggleShowTimePicker()
    }

    fun onTimelessSwitchClick() {
        _chipUiState.update { cur ->
            cur.copy(timeless = !cur.timeless)
        }
        verifyState()
    }

    fun toggleShowTimePicker() {
        _uiState.update { cur ->
            cur.copy(showTimePicker = !cur.showTimePicker)
        }
    }

    fun toggleForGroupingOnly(value: Boolean) {
        _chipUiState.update { cur ->
            cur.copy(forGroupingOnly = value)
        }
        verifyState()
    }

    suspend fun saveCreatedChip(uiState: GenericTaskOrChipUiState = chipUiState.value) {
        if (uiState.name.isNotBlank()) {
            if (chipId != null) {
                repository.updateChip(chipUiState.value.toChip())
            } else {
                repository.createChip(chipUiState.value.toChip())
            }
            _chipUiState.update { GenericTaskOrChipUiState() }
            _uiState.update { UiState() }
        }
    }

    private fun verifyState() {
        _chipUiState.update { cur ->
            cur.copy(isEntryValid = cur.name.isNotBlank())
        }
    }

    private companion object {
        private const val CHIP_UI_STATE_KEY = "CHIP_UI_STATE_KEY"
        private const val UI_STATE_KEY = "UI_STATE_KEY"
    }


}

// for verifying valid state just check if name is not blank
@Parcelize
data class GenericTaskOrChipUiState(
    val chipId: Int? = null,
    val name: String = "",
    val forGroupingOnly: Boolean = false,
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

fun GenericTaskOrChipUiState.toChip(): Chip {
    return Chip(
        id = chipId ?: 0,
        name = name,
        forGroupingOnly = forGroupingOnly,
        repeatOnDaysBitFlag = if (!timeless) selectedWeekDaysBitmask else 0,
        dateTime = if (!timeless) selectedTime else null,
        floatingOffsetHours = if (timeless) offsetHours else null,
        priority = priority
    )
}

fun Chip.toChipUiState(): GenericTaskOrChipUiState =
    GenericTaskOrChipUiState(
        chipId = id,
        name = name,
        forGroupingOnly = forGroupingOnly,
        selectedTime = dateTime ?: Calendar.getInstance(),
        selectedWeekDaysBitmask = repeatOnDaysBitFlag,
        priority = priority,
        timeless = floatingOffsetHours != null,
        offsetHours = floatingOffsetHours ?: 1,
        isEntryValid = false
    )