package nish.wry.salamander.data.room.suBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate


/**Represents a day, like 17th june
 * this date is attached to each [ActivityInterval] via foreign key
 *
 * @param dayId: auto incrementing primary key
 * @param date a [LocalDate] date it refers to
 * @param note some note for the day user can attach
 * **/
@Entity(tableName = "daily_log")
data class DailyLog(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "day_id")
    val dayId: Int = 0,

    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "note")
    val note: String? = null,
)
