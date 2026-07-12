package nish.wry.salamander.ui.suBase.category

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import nish.wry.salamander.data.Constants
import nish.wry.salamander.data.MutableSaveStateFlow
import nish.wry.salamander.data.room.suBase.Category
import nish.wry.salamander.di.ActivityRepository
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableSaveStateFlow(
        savedStateHandle = savedStateHandle,
        key = CATEGORY_KEY,
        defaultValue = CategoryScreenUiState()
    )

    val uiState = _uiState.asStateFlow()

    init {
        // trying this type of input validation, this way if we want to verify something else we can
        viewModelScope.launch {
            uiState.collectLatest { value: CategoryScreenUiState ->

                val isValid = isValidUiState(value)

                // shouldn't be a infinite loop cuz updates to mutable stateflow updates
                // happen only if the new state is different
                _uiState.update { cur -> cur.copy(validState = isValid) }
            }
        }
    }


    fun updateGoalHrs(hrs: Float) {
        _uiState.update { cur ->
            cur.copy(goalHrs = hrs.roundToInt())
        }
    }

    fun updateGoalMins(mins: Float) {
        _uiState.update { cur ->
            cur.copy(goalMins = mins.roundToInt())
        }
    }

    fun updateCategoryName(input: String) {
        _uiState.update { cur ->
            cur.copy(categoryName = input)
        }
    }

    fun updateDescription(input: String) {
        _uiState.update { cur ->
            cur.copy(description = input)
        }
    }

    suspend fun onSaveButtonClicked(uiStateRef: CategoryScreenUiState = uiState.value) {
        with(uiStateRef) {
            if (isValidUiState(uiStateRef)) {
                var goalTimeInMins: Int? = goalHrs * 60 + goalMins

                if (goalTimeInMins == 0)
                    goalTimeInMins = null

                activityRepository.addCategory(
                    Category(name = categoryName, goalTimeInMins = goalTimeInMins)
                )
            }
        }
    }

    private fun isValidUiState(uiStateRef: CategoryScreenUiState): Boolean {
        with(uiStateRef) {
            val valid: Boolean = categoryName.isNotBlank()
            return valid
        }
    }


    private companion object {
        private const val CATEGORY_KEY = "CATEGORY_KEY"
    }
}

@Parcelize
data class CategoryScreenUiState(
    val categoryName: String = "",
    val goalHrs: Int = Constants.GOAL_TIME_SLIDER_DEFAULT_HRS,
    val goalMins: Int = Constants.GOAL_TIME_SLIDER_DEFAULT_MINS,
    val description: String = "",
    val validState: Boolean = false,
) : Parcelable