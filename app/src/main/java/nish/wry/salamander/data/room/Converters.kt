package nish.wry.salamander.data.room

import androidx.room.TypeConverter
import nish.wry.salamander.data.Priority
import java.util.Calendar

class Converters {
    @TypeConverter
    fun epochTimeToCalender(value: Long?): Calendar? {
        return if (value != null) {
            val cal = Calendar.getInstance()
            cal.setTimeInMillis(value)
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

}