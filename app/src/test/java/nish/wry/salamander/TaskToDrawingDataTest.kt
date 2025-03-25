package nish.wry.salamander

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import nish.wry.salamander.data.TaskToTaskDrawingData
import nish.wry.salamander.data.room.task.Task
import nish.wry.salamander.fake.FakeTaskFetchRepository
import nish.wry.salamander.fake.calendarFromTime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

class TaskToDrawingDataTest {
    @Test
    fun emptyTaskList_noHeapError() = runBlocking {
        class Repo : FakeTaskFetchRepository() {
            override fun getTaskForDayIncludingOffsetTask(date: Calendar): Flow<List<Task>> = flow {
                emit(emptyList())
            }


            override fun getTaskForDay(bitmask: Int, date: Calendar): Flow<List<Task>> = flow {
                emit(emptyList())
            }

        }

        val taskToTaskDrawingData = TaskToTaskDrawingData.getInstance(Repo())

        // subtract today's epoch - 15th feb 2025's epoch
        val diff = Calendar.getInstance().timeInMillis - calendarFromTime(0, 0).timeInMillis
        val dateOffset = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
        val flw = taskToTaskDrawingData[-dateOffset.toInt()]
        assertEquals(flw.first(), emptyList<TaskToTaskDrawingData>())
    }

}