package nish.wry.salamander.data.room

import androidx.room.TypeConverter
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

}