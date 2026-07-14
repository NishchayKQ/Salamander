package nish.wry.salamander.data.repository

import kotlinx.coroutines.flow.Flow
import nish.wry.salamander.data.room.suBase.ActivityInterval
import nish.wry.salamander.data.room.suBase.ActivityIntervalDao
import nish.wry.salamander.data.room.suBase.ActivityUiData
import nish.wry.salamander.data.room.suBase.Category
import nish.wry.salamander.data.room.suBase.CategoryDao
import nish.wry.salamander.data.room.suBase.CategoryDurationUiData
import nish.wry.salamander.data.room.suBase.CategoryUiData
import nish.wry.salamander.data.room.suBase.CurrentActivityUiData
import nish.wry.salamander.data.room.suBase.DailyLog
import nish.wry.salamander.data.room.suBase.DailyLogDao
import nish.wry.salamander.domain.repository.ActivityRepository
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

class OfflineActivityRepository @Inject constructor(
    private val activityIntervalDao: ActivityIntervalDao,
    private val categoryDao: CategoryDao,
    private val dailyLogDao: DailyLogDao,
) : ActivityRepository {
    // TODO somehow get docs here?
    override fun getActivitiesForDay(localDate: LocalDate): Flow<List<ActivityUiData>> =
        activityIntervalDao.getAllActivityUiDataForDay(localDate)

    override fun getCurrentActivityInterval(): Flow<CurrentActivityUiData?> =
        activityIntervalDao.getCurrentActivityInterval()

    override fun getDurationPerCategoryForDay(localDate: LocalDate): Flow<List<CategoryDurationUiData>> =
        activityIntervalDao.getGraphDataForDay(localDate)

    override suspend fun startActivity(activityInterval: ActivityInterval) =
        activityIntervalDao.addActivityInterval(activityInterval)

    override suspend fun endCurrentActivity(localTime: LocalTime) {
        activityIntervalDao.endCurrentActivity(localTime)
    }


    override fun getAllCategories(): Flow<List<CategoryUiData>> = categoryDao.getAllCategory()

    override suspend fun addCategory(category: Category) = categoryDao.addCategory(category)


    /**
     * @return a Int corresponding to [DailyLog.dayId]
     * **/
    override suspend fun getDayIdForDay(localDate: LocalDate): Int =
        dailyLogDao.getDayIdForDay(localDate)

}