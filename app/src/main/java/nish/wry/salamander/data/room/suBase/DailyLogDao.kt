package nish.wry.salamander.data.room.suBase

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import java.time.LocalDate

@Dao
interface DailyLogDao {

    // TODO this should happen automatically if a day doesn't exist in db, do this repository
    @Insert
    suspend fun addDailyLog(dailyLog: DailyLog)


    /**should not be used directly, instead use [getDayIdForDay]**/
    @Query("select day_id from daily_log where date = :localDate")
    suspend fun getDayIdForDayInternal(localDate: LocalDate): Int?

    @Transaction
    suspend fun getDayIdForDay(localDate: LocalDate): Int {
        val existingId = getDayIdForDayInternal(localDate)
        return if (existingId != null) {
            existingId
        } else {
            addDailyLog(DailyLog(date = localDate))
            return getDayIdForDayInternal(localDate)
                ?: throw IllegalStateException("newId is also null, ie the ID corresponding calendar obj : $localDate")
        }
    }
}