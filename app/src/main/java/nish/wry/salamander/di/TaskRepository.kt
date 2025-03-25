package nish.wry.salamander.di

import kotlinx.coroutines.flow.Flow
import nish.wry.salamander.data.Week
import nish.wry.salamander.data.room.task.Chip
import nish.wry.salamander.data.room.task.ChipDao
import nish.wry.salamander.data.room.task.Task
import nish.wry.salamander.data.room.task.TaskDao
import java.util.Calendar

interface TaskRepository {
    fun getAllChips(): Flow<List<Chip>>

    fun getChipWithId(id: Int): Flow<Chip>

    suspend fun createChip(chip: Chip)

    suspend fun deleteChip(chip: Chip)

    suspend fun updateChip(chip: Chip)

    fun getTasksWithChip(chipId: Int): Flow<List<Task>>

    fun getTaskWithId(id: Int): Flow<Task>

    fun getTaskForDayIncludingOffsetTask(
        date: Calendar,
    ): Flow<List<Task>>

    fun getTaskForDay(
        bitmask: Int,
        date: Calendar,
    ): Flow<List<Task>>

    suspend fun createTask(task: Task)

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(task: Task)

}

class OfflineTaskRepository(private val taskDao: TaskDao, private val chipDao: ChipDao) :
    TaskRepository {
    override fun getAllChips(): Flow<List<Chip>> = chipDao.getAllChips()

    override fun getChipWithId(id: Int): Flow<Chip> = chipDao.getChipWithId(id)

    override suspend fun createChip(chip: Chip) = chipDao.insert(chip)

    override suspend fun deleteChip(chip: Chip) = chipDao.delete(chip)

    override suspend fun updateChip(chip: Chip) = chipDao.update(chip)

    override fun getTasksWithChip(chipId: Int): Flow<List<Task>> = taskDao.getTasksWithChip(chipId)

    override fun getTaskWithId(id: Int): Flow<Task> = taskDao.getTaskWithId(id)

    override fun getTaskForDayIncludingOffsetTask(
        date: Calendar,
    ): Flow<List<Task>> {
        val dateClone: Calendar = date.clone() as Calendar

        return taskDao.getTaskForDayIncludingOffsetTask(
            bitmask = Week.calenderToWeekEnum(date).mask,
            startDate = setCalender(date, 0),
            endDate = setCalender(dateClone, 0, 1)
        )
    }


    override fun getTaskForDay(bitmask: Int, date: Calendar): Flow<List<Task>> {
        val dateClone: Calendar = date.clone() as Calendar
        val startDate = setCalender(dateClone, 0)
        val endDate = setCalender(date, 24)
        return taskDao.getTaskForDay(bitmask, startDate, endDate)
    }

    override suspend fun createTask(task: Task) = taskDao.insert(task)

    override suspend fun updateTask(task: Task) = taskDao.update(task)

    override suspend fun deleteTask(task: Task) = taskDao.delete(task)

}

private fun setCalender(calendar: Calendar, hour: Int, addDate: Int = 0): Calendar {
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    if (addDate != 0) {
        calendar.add(Calendar.DATE, addDate)
    }
    return calendar
}