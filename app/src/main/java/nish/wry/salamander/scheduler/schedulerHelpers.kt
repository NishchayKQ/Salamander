package nish.wry.salamander.scheduler

import nish.wry.salamander.data.Week
import java.util.Calendar

// in milliseconds
private const val FIVE_MINS = 5 * 60 * 1000

/**
 * Calculates the next valid reminder time for a recurring task.
 * Works for both normal triggers (AlarmReceiver) and missed triggers (Boot_Complete).
 */
fun calculateNextReminderTime(
    lastScheduledTime: Long,
    weekdaysBitmask: Int,
    currentTime: Long = System.currentTimeMillis(),
): Long {
    require(weekdaysBitmask != 0)

    val calendar = Calendar.getInstance().apply {
        // we make a new calendar instance that is set to the time in task
        timeInMillis = lastScheduledTime

        // we extract the relevant fields from this
        val targetHour = get(Calendar.HOUR_OF_DAY)
        val targetMin = get(Calendar.MINUTE)

        // this calendar now represents current time
        // has current date, month etc
        timeInMillis = currentTime
        set(Calendar.HOUR_OF_DAY, targetHour)
        set(Calendar.MINUTE, targetMin)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)

        // Go back one day so Week.findNext... starts its search from "Today"
        add(Calendar.DAY_OF_MONTH, -1)
    }

    var nextCal = Week.findNextCalenderDayForRecurringReminder(calendar, weekdaysBitmask)

    // if this task is too old, jump to the NEXT future occurrence
    if (nextCal.timeInMillis < currentTime - FIVE_MINS) {
        nextCal = Week.findNextCalenderDayForRecurringReminder(nextCal, weekdaysBitmask)
    }

    return nextCal.timeInMillis
}