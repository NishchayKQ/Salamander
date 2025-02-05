package nish.wry.salamander.data

import androidx.annotation.VisibleForTesting
import java.util.Calendar

enum class Week(
    @get:VisibleForTesting
    val mask: Int
) {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(4),
    THURSDAY(8),
    FRIDAY(16),
    SATURDAY(32),
    SUNDAY(64);

    @VisibleForTesting
    operator fun plus(other: Week): Int {
        // TODO write in notes, we use 'bitwise or' but simple '+' is also ok, though '+' fails if same flag is passed twice
        return this.mask or other.mask
    }

    @VisibleForTesting
    operator fun plus(other: Int): Int {
        return this.mask or other
    }

    fun bitMaskForNextThreeDaysFromToday(calender: Calendar): Int {
        val currentDayOfWeek = calender.get(Calendar.DAY_OF_WEEK)
        val nextDay = giveNextValidWeekdayNumber(currentDayOfWeek)
        val nextNextDay = giveNextValidWeekdayNumber(nextDay)
        return calenderDayToWeekEnum(currentDayOfWeek) + (calenderDayToWeekEnum(nextDay) + calenderDayToWeekEnum(
            nextNextDay
        ))

    }

    private fun giveNextValidWeekdayNumber(dayOfWeek: Int): Int {
        return if ((dayOfWeek + 1) % 8 == 0) {
            1
        } else {
            (dayOfWeek + 1) % 8
        }
    }

    private fun calenderDayToWeekEnum(dayOfWeek: Int): Week {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> SUNDAY
            Calendar.MONDAY -> MONDAY
            Calendar.TUESDAY -> TUESDAY
            Calendar.WEDNESDAY -> WEDNESDAY
            Calendar.THURSDAY -> THURSDAY
            Calendar.FRIDAY -> FRIDAY
            Calendar.SATURDAY -> SATURDAY
            else -> {
                throw IllegalArgumentException("dayOfWeek should be in between 1 to 7, was given $dayOfWeek")
            }
        }
    }
}
