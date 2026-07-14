package nish.wry.salamander.ui.suBase

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import nish.wry.salamander.data.DateTimeTracker
import nish.wry.salamander.data.MutableSaveStateFlow
import nish.wry.salamander.data.room.suBase.ActivityInterval
import nish.wry.salamander.data.room.suBase.ActivityUiData
import nish.wry.salamander.data.room.suBase.Category
import nish.wry.salamander.data.room.suBase.CategoryDurationUiData
import nish.wry.salamander.data.room.suBase.CategoryUiData
import nish.wry.salamander.data.room.suBase.CurrentActivityUiData
import nish.wry.salamander.domain.repository.ActivityRepository
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SubBaseViewModel @Inject constructor(
    dateTimeTracker: DateTimeTracker,
    private val activityRepository: ActivityRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val localDateStateFlow: StateFlow<LocalDate> = dateTimeTracker.currentDate

    val currentTime: StateFlow<LocalTime> = dateTimeTracker.currentTime

    @OptIn(ExperimentalCoroutinesApi::class)
    val activityUiDataFlow: StateFlow<List<ActivityUiData>> =
        localDateStateFlow.flatMapLatest { localDate ->
            activityRepository.getActivitiesForDay(localDate)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val perCategoryDurationUiData: StateFlow<List<CategoryDurationUiData>> =
        localDateStateFlow.flatMapLatest { value: LocalDate ->
            activityRepository.getDurationPerCategoryForDay(value)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    val is24Hour: StateFlow<Boolean> = dateTimeTracker.is24Hour

    val currentActivity: StateFlow<CurrentActivityUiData?> =
        activityRepository.getCurrentActivityInterval().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = null
        )

    val categoryListFlow: StateFlow<List<CategoryUiData>> =
        activityRepository.getAllCategories().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    private val _suBaseUiState =
        MutableSaveStateFlow(savedStateHandle, SU_BASE_KEY, SuBaseUiState())

    val suBaseUiState = _suBaseUiState.asStateFlow()

    init {
        viewModelScope.launch {
            // TODO which thread should we run these 2 at?
            // will it cause jitter on UI Thread?, but we need it blocking to not allow user to mess it up
            // should we then make use of isCurrentlySavingUiState and update it hold a lock?
            // then we can acquire the save protection, and safely run on diff thread
//            Log.d("kaw", Thread.currentThread().name)

            // had to move it up cuz kotlin is synchronous unless specified
            /**
             * if the app was not active and the day changes, so on viewmodel initialisation we see
             * the first not null activity emitted by [currentActivity] and see if the date it belongs to
             * is today
             **/
            val ref: CurrentActivityUiData = currentActivity.filterNotNull().first()
            // if current date is after the current activity's date
            if (localDateStateFlow.value.isAfter(ref.date)) {
                endAndStart(ref)
            }

            /**
             * if the day ends and we still have a activity going on then end the activity (at 11:59pm)
             * and start it for the next day (at 12am)
             **/
            withContext(Dispatchers.Default) {
                localDateStateFlow.collectLatest { localDate: LocalDate ->
                    val currentActivityRef: CurrentActivityUiData? = currentActivity.value

                    if (currentActivityRef != null) {
                        if (localDate.isAfter(currentActivityRef.date)) {
                            endAndStart(currentActivityRef)
                        }
                    }
                }
            }
        }
    }

    fun updateSelectedCategoryId(id: Int) {
        _suBaseUiState.update { cur ->
            cur.copy(selectedCategoryId = id, startTrackingButtonEnabled = true)
        }
    }

    private suspend fun endAndStart(currentActivityUiData: CurrentActivityUiData) {
        activityRepository.endCurrentActivity(LocalTime.of(23, 59))
        val dayId = activityRepository.getDayIdForDay(localDateStateFlow.value)

        activityRepository.startActivity(
            ActivityInterval(
                categoryId = currentActivityUiData.categoryId,
                dayId = dayId,
                start = LocalTime.of(0, 0),
                end = null
            )
        )
    }


    suspend fun startActivity() {

        // we don't want multiple activities to be running simultaneously
        require(currentActivity.value == null)

        _suBaseUiState.update { cur ->
            cur.copy(startTrackingButtonEnabled = false)
        }
        with(suBaseUiState.value) {
            val dayId = activityRepository.getDayIdForDay(localDateStateFlow.value)
            activityRepository.startActivity(
                ActivityInterval(
                    categoryId = selectedCategoryId,
                    start = LocalTime.now(),
                    end = null,
                    dayId = dayId
                )
            )
        }
    }

    suspend fun endCurrentActivity() {
        require(currentActivity.value != null)
        activityRepository.endCurrentActivity(LocalTime.now())
    }

    private companion object {
        private const val SU_BASE_KEY = "SU_BASE_KEY"
        private const val TIMEOUT_MILLIS = 5_000L
    }
}


/**
 * @param selectedCategoryId the id corresponding to the [Category.categoryId] user selected, by default -1
 * **/
@Parcelize
data class SuBaseUiState(
    val selectedCategoryId: Int = -1,
    val startTrackingButtonEnabled: Boolean = false,
    // TODO implement
    val currentlySavingData: Boolean = false,
) : Parcelable