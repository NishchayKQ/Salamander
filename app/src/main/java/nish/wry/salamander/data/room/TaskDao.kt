package nish.wry.salamander.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

@Dao
interface TaskDao {
    @Query("select * from task where task_chip = :chipId")
    fun getTasksWithChip(chipId: Int): Flow<List<Task>>

    @Query("select * from task where id = :id")
    fun getTaskWithId(id: Int): Flow<Task>

    @Query("select * from task where (date_time between :startDate and :endDate) or (weekdays_bitflag & :bitmask > 0) or (floating_offset_hours is not null)")
    fun getTasksForTwoDays(
        bitmask: Int,
        startDate: Calendar,
        endDate: Calendar,
    ): Flow<List<Task>>

//    think order by is not needed cuz some dates are in asc while others are there just cuz weekdays match
    /**does not return task with offset as it must either have datetime or weekly set neither of which offset task have**/
    @Query("select * from task where (date_time between :startDate and :endDate) or (weekdays_bitflag & :bitmask > 0)")
    fun getTaskForDay(
        bitmask: Int,
        startDate: Calendar,
        endDate: Calendar
    ): Flow<List<Task>>

    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)


}