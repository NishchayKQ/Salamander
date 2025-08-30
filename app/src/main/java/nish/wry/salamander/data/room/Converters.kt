package nish.wry.salamander.data.room

import androidx.room.TypeConverter
import nish.wry.salamander.data.Priority
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import kotlin.time.Instant


class Converters {
    @TypeConverter
    fun epochTimeToCalender(value: Long?): Calendar? {
        return if (value != null) {
            val cal = Calendar.getInstance()
            cal.timeInMillis = value
            cal
        } else {
            null
        }
    }

    @TypeConverter
    fun calendarToEpoch(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }

    @TypeConverter
    fun priorityToInt(priority: Priority): Int {
        return priority.id
    }

    @TypeConverter
    fun intToPriority(id: Int): Priority {
        return Priority.priorityById(id)
    }

    @TypeConverter
    fun localDateToLong(localDate: LocalDate): Long {
        return localDate.toEpochDay()
    }

    @TypeConverter
    fun longToLocalDate(long: Long): LocalDate {
        return LocalDate.ofEpochDay(long)
    }

    @TypeConverter
    fun localTimeToInt(localTime: LocalTime): Int {
        return localTime.toSecondOfDay()
    }

    @TypeConverter
    fun intToLocalTime(int: Int): LocalTime {
        return LocalTime.ofSecondOfDay(int.toLong())
    }

    @TypeConverter
    fun instantToLong(instant: Instant): Long {
        return instant.epochSeconds
    }

    @TypeConverter
    fun longToInstant(long: Long): Instant {
        return Instant.fromEpochSeconds(long)
    }
}