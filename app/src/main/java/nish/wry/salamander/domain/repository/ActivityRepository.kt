package nish.wry.salamander.domain.repository

import kotlinx.coroutines.flow.Flow
import nish.wry.salamander.data.room.suBase.ActivityInterval
import nish.wry.salamander.data.room.suBase.ActivityUiData
import nish.wry.salamander.data.room.suBase.Category
import nish.wry.salamander.data.room.suBase.CategoryDurationUiData
import nish.wry.salamander.data.room.suBase.CategoryUiData
import nish.wry.salamander.data.room.suBase.CurrentActivityUiData
import java.time.LocalDate
import java.time.LocalTime

interface ActivityRepository {
    fun getActivitiesForDay(localDate: LocalDate): Flow<List<ActivityUiData>>

    fun getCurrentActivityInterval(): Flow<CurrentActivityUiData?>

    fun getDurationPerCategoryForDay(localDate: LocalDate): Flow<List<CategoryDurationUiData>>

    suspend fun startActivity(activityInterval: ActivityInterval)

    suspend fun endCurrentActivity(localTime: LocalTime)


    fun getAllCategories(): Flow<List<CategoryUiData>>

    suspend fun addCategory(category: Category)


    suspend fun getDayIdForDay(localDate: LocalDate): Int
}