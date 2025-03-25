package nish.wry.salamander.data.room.suBase

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

@Dao
interface ActivityIntervalDao {
    /**
     * fetch a [ActivityInterval] based on [ActivityInterval.activityId]
     * **/
    @Query(
        "select * from activity_interval where activity_id = :activityId " +
                "limit 1"
    )
    fun getActivityIntervalWithId(activityId: Int): Flow<ActivityInterval>

    /**
     * @return a flow of [CurrentActivityUiData] if there is an activity going on else null
     * **/
    @Query(
        "select category.name as name, " +
                "category.category_id as category_id, " +
                "start, " +
                "daily_log.date as date " +
                "from activity_interval " +
                "join category on activity_interval.category_id = category.category_id " +
                "join daily_log on daily_log.day_id = activity_interval.day_id " +
                "where `end` is null " +
                "limit 1"
    )
    fun getCurrentActivityInterval(): Flow<CurrentActivityUiData?>

    // TODO is this query ok?
    /**
     * can be used to fetch all activities that happened on a particular day
     * **/
    @Query(
        "select name, (`end` - start) / 3600.0 as duration, start, `end` from activity_interval " +
                "join category on activity_interval.category_id = category.category_id " +
                "where day_id = (select day_id from daily_log where date = :localDate) " +
                "order by start desc"
    )
    fun getAllActivityUiDataForDay(localDate: LocalDate): Flow<List<ActivityUiData>>

    // we don't want to update multiple places
    // if there are multiple end's null then db integrity has failed
    @Query(
        "update activity_interval " +
                "set `end` = :localTime " +
                "where activity_id = (select activity_id from activity_interval where `end` is null limit 1)"
    )
    suspend fun endCurrentActivity(localTime: LocalTime)

    @Insert
    suspend fun addActivityInterval(activityInterval: ActivityInterval)

    @Delete
    suspend fun deleteActivityInterval(activityInterval: ActivityInterval)
}

data class ActivityUiData(
    @ColumnInfo("name")
    val categoryName: String,

    @ColumnInfo("duration")
    val duration: Float?,

    @ColumnInfo("start")
    val start: LocalTime,

    @ColumnInfo("end")
    val end: LocalTime?,
)

data class CurrentActivityUiData(
    @ColumnInfo("name")
    val categoryName: String,

    @ColumnInfo("category_id")
    val categoryId: Int,

    @ColumnInfo("start")
    val start: LocalTime,

    @ColumnInfo("date")
    val date: LocalDate,
)