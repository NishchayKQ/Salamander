package nish.wry.salamander.di

import kotlinx.coroutines.flow.Flow
import nish.wry.salamander.data.room.Chip
import nish.wry.salamander.data.room.ChipDao
import nish.wry.salamander.data.room.Task
import nish.wry.salamander.data.room.TaskDao
import java.util.Calendar

interface TaskRepository {
    fun getAllChips(): Flow<List<Chip>>

    fun getChipWithId(id: Int): Flow<Chip>

    suspend fun createChip(chip: Chip)

    suspend fun deleteChip(chip: Chip)

    suspend fun updateChip(chip: Chip)

    fun getTasksWithChip(chipId: Int): Flow<List<Task>>

    fun getTaskWithId(id: Int): Flow<Task>

    fun getTaskForNextThreeDays(
        bitmask: Int,
        startDate: Calendar,
        endDate: Calendar,
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

    override fun getTaskForNextThreeDays(
        bitmask: Int,
        startDate: Calendar,
        endDate: Calendar,
    ): Flow<List<Task>> = taskDao.getTaskForNextThreeDays(bitmask, startDate, endDate)

    override suspend fun createTask(task: Task) = taskDao.insert(task)

    override suspend fun updateTask(task: Task) = taskDao.update(task)

    override suspend fun deleteTask(task: Task) = taskDao.delete(task)

}