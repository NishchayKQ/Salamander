package nish.wry.salamander.data.room.suBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalTime

// TODO check if there is a compile time check by room if i put foreign key name something that
//  doesn't match

/**
 * a time interval that represents what a user is doing - category, when did they start,
 * and when did they end if they did
 *
 * @param activityId auto incrementing primary key
 * @param categoryId foreign key to a [Category], the category of the activity interval
 * @param dayId foreign key to a [DailyLog], represents which day this activity interval belongs to
 * @param start [LocalTime] time stamp of the time it started
 * @param end [LocalTime] time stamp of when the activity ended, null means its ongoing,
 * there can only be one activity active at any time
 * @param description optional user specified description of the activity interval
 * **/
@Entity(
    tableName = "activity_interval", foreignKeys = [
        ForeignKey(
            entity = DailyLog::class,
            parentColumns = ["day_id"],
            childColumns = ["day_id"]
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["category_id"],
            childColumns = ["category_id"]
        )
    ]
)
data class ActivityInterval(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "activity_id")
    // required default is 0, for insert method to set it automatically (0 is treated as not-set)
    // https://developer.android.com/reference/androidx/room/PrimaryKey?hl=en#getAutoGenerate()
    val activityId: Int = 0,

    @ColumnInfo(name = "category_id")
    val categoryId: Int,

    @ColumnInfo(name = "day_id")
    val dayId: Int,

    @ColumnInfo(name = "start")
    val start: LocalTime,

    @ColumnInfo(name = "end")
    val end: LocalTime?,

    @ColumnInfo(name = "desc")
    val description: String? = null,
)
