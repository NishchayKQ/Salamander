package nish.wry.salamander.fake

import kotlinx.coroutines.flow.Flow
import nish.wry.salamander.data.room.task.Chip
import nish.wry.salamander.data.room.task.Task
import nish.wry.salamander.di.TaskRepository

// to test task for 1 day not including offset hours
abstract class FakeTaskFetchRepository : TaskRepository {
//    override fun getTaskForDayIncludingOffsetTask(date: Calendar): Flow<List<Task>> {
//        return flow {
//            emit(listOf())
//            emit(FakeTask.allTask_NoOffsetTask)
//        }
//    }
//
//    override fun getTaskForDay(bitmask: Int, date: Calendar): Flow<List<Task>> {
//        return flow {
//            emit(listOf())
//            emit(FakeTask.allTask_NoOffsetTask)
//        }
//    }


    override fun getAllChips(): Flow<List<Chip>> {
        throw NotImplementedError()
    }

    override fun getChipWithId(id: Int): Flow<Chip> {
        throw NotImplementedError()
    }

    override suspend fun createChip(chip: Chip) {
        throw NotImplementedError()
    }

    override suspend fun deleteChip(chip: Chip) {
        throw NotImplementedError()
    }

    override suspend fun updateChip(chip: Chip) {
        throw NotImplementedError()
    }

    override fun getTasksWithChip(chipId: Int): Flow<List<Task>> {
        throw NotImplementedError()
    }

    override fun getTaskWithId(id: Int): Flow<Task> {
        throw NotImplementedError()
    }

    override suspend fun createTask(task: Task) {
        throw NotImplementedError()
    }

    override suspend fun updateTask(task: Task) {
        throw NotImplementedError()
    }

    override suspend fun deleteTask(task: Task) {
        throw NotImplementedError()
    }
}