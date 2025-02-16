package nish.wry.salamander.data

import androidx.annotation.VisibleForTesting
import java.util.Calendar

enum class Week(
    @get:VisibleForTesting
    val mask: Int,
    val letter: String,
) {
    MONDAY(1, "M"),
    TUESDAY(2, "T"),
    WEDNESDAY(4, "W"),
    THURSDAY(8, "T"),
    FRIDAY(16, "F"),
    SATURDAY(32, "S"),
    SUNDAY(64, "S");

    infix fun or(other: Week): Int {
        // TODO write in notes, we use 'bitwise or' but simple '+' is also ok, though '+' fails if same flag is passed twice
        return this.mask or other.mask
    }

    infix fun or(other: Int): Int {
        return this.mask or other
    }

    infix fun and(other: Int): Int {
        return this.mask and other
    }

    fun inv(): Int {
        return this.mask.inv()
    }

    companion object {
        fun bitMaskForTodayAndTomorrow(calender: Calendar): Int {
            val currentDayOfWeek = calender.get(Calendar.DAY_OF_WEEK)
            val nextDay = giveNextValidWeekdayNumber(currentDayOfWeek)

            return calenderDayToWeekEnum(currentDayOfWeek) or calenderDayToWeekEnum(nextDay)
        }

        private fun giveNextValidWeekdayNumber(dayOfWeek: Int): Int {
            return if ((dayOfWeek + 1) % 8 == 0) {
                1
            } else {
                (dayOfWeek + 1) % 8
            }
        }

        fun calenderToWeekEnum(calender: Calendar): Week {
            return calenderDayToWeekEnum(calender[Calendar.DAY_OF_WEEK])
        }

        fun calenderDayToWeekEnum(dayOfWeek: Int): Week {
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
}

infix fun Int.or(other: Week): Int {
    return this or other.mask
}

infix fun Int.and(other: Week): Int {
    return this and other.mask
}
