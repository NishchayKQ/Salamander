package nish.wry.salamander.data

import android.content.Context
import android.text.format.DateFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

// TODO should not run on main dispatcher
class DateTimeTracker(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
) {
    private val _currentTime: MutableStateFlow<LocalTime> = MutableStateFlow(LocalTime.now())
    private val _currentDate: MutableStateFlow<LocalDate> = MutableStateFlow(LocalDate.now())
    private val _is24Hour: MutableStateFlow<Boolean> =
        MutableStateFlow(DateFormat.is24HourFormat(context))

    val currentTime: StateFlow<LocalTime> = _currentTime.asStateFlow()
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()
    val is24Hour: StateFlow<Boolean> = _is24Hour.asStateFlow()


    private var instance: Job? = null

    init {
        refresh()
    }

    private suspend inline fun startTracking() {
        while (true) {
            val now = LocalTime.now()
            val today = LocalDate.now()

            if (_currentTime.value.minute != now.minute || _currentTime.value.hour != now.hour) {
                _currentTime.update { now }
            }

            _currentDate.update { today }

            // Delay until next minute + 1 second to avoid drift
            val delayMillis: Long = (60 - now.second) * 1000L + 1000
            delay(delayMillis)
        }
    }

    /**immediately update time**/
    fun refresh() {
        instance?.cancel()
        _is24Hour.update { DateFormat.is24HourFormat(context) }
        instance = coroutineScope.launch { startTracking() }
    }

    /**stop job**/
    fun stop() {
        instance?.cancel()
    }
}

