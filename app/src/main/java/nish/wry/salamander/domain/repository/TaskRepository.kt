package nish.wry.salamander.domain.repository

import kotlinx.coroutines.flow.Flow
import nish.wry.salamander.data.room.task.Chip
import nish.wry.salamander.data.room.task.Task
import java.util.Calendar

interface TaskRepository {
    fun getAllChips(): Flow<List<Chip>>

    fun getChipWithId(id: Int): Flow<Chip>

    suspend fun createChip(chip: Chip)

    suspend fun deleteChip(chip: Chip)

    suspend fun updateChip(chip: Chip)

    fun getTasksWithChip(chipId: Int): Flow<List<Task>>

    suspend fun getTaskWithId(id: Int): Task?

    fun getTaskForDayIncludingOffsetTask(
        date: Calendar,
    ): Flow<List<Task>>

    fun getTaskForDay(
        bitmask: Int,
        date: Calendar,
    ): Flow<List<Task>>

    suspend fun createTask(task: Task) : Long

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(task: Task)

}