import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import nish.wry.salamander.data.Week
import nish.wry.salamander.data.room.Chip
import nish.wry.salamander.data.room.ChipDao
import nish.wry.salamander.data.room.SalamanderRoomDatabase
import nish.wry.salamander.data.room.Task
import nish.wry.salamander.data.room.TaskDao
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Calendar

@Suppress("PrivatePropertyName")
@RunWith(AndroidJUnit4::class)
class DaoTest {
    private lateinit var chipDao: ChipDao
    private lateinit var taskDao: TaskDao

    private lateinit var salamanderRoomDatabase: SalamanderRoomDatabase

    // task with repeatOnDaysBitFlag = 0, floatingOffsetHours = null
    private lateinit var task1_oneTimeTask: Task

    // task with repeatOnDaysBitFlag = sun,mon,sat
    private lateinit var task2_oneTimeTask_repeatOnSunMonSat: Task

    // TODO make more combos for testing tasks

    private lateinit var chip1: Chip

    private lateinit var chip2: Chip


    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()

        salamanderRoomDatabase =
            Room.inMemoryDatabaseBuilder(context, SalamanderRoomDatabase::class.java)
                .allowMainThreadQueries()
                .build()

        chipDao = salamanderRoomDatabase.chipDao()
        taskDao = salamanderRoomDatabase.taskDao()
        chip1 = Chip(
            id = 1,
            name = "college",
            floatingOffsetHours = 1
        )

        chip2 = Chip(
            id = 2,
            name = "shopping",
            floatingOffsetHours = 2
        )


        val calendar = Calendar.getInstance()


        calendar.clear()
//        1/1/2025 4 am
        calendar.set(2025, 1, 1, 4, 0)

        task1_oneTimeTask = Task(
            id = 1,
            name = "go to college 💀 at 4am",
            dateTime = calendar,
            taskChipId = 1
        )

        calendar.clear()
//        1/1/2025 6 pm
        calendar.set(2025, 1, 1, 18, 0)

        task2_oneTimeTask_repeatOnSunMonSat = Task(
            id = 2,
            name = "buy bread 🍞",
            dateTime = calendar,
            repeatOnDaysBitFlag = Week.SUNDAY or (Week.MONDAY or Week.SATURDAY),
            taskChipId = 2
        )


    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        salamanderRoomDatabase.close()
    }

    private suspend fun addOneChipToDb() {
        chipDao.insert(chip1)
    }

    private suspend fun addTwoChipsToDb() {
        chipDao.insert(chip1)
        chipDao.insert(chip2)
    }

    private suspend fun addTwoTasksToDb(){
        addTwoChipsToDb()
        taskDao.insert(task1_oneTimeTask)
        taskDao.insert(task2_oneTimeTask_repeatOnSunMonSat)

    }

    @Test
    @Throws(Exception::class)
    fun chipDaoInsert_insertsChipIntoDb() = runBlocking {
        addOneChipToDb()
        val allChips = chipDao.getAllChips().first()
        assertEquals(chip1, allChips[0])
    }


    // FIXME cant get room to fucking use the default autogen primary key, manually made chip1 id to 2
    @Test
    @Throws(Exception::class)
    fun chipDaoGetAllItems_returnsAllChipsFromDb() = runBlocking {
        addTwoChipsToDb()
        val allChips = chipDao.getAllChips().first()

        assertEquals(chip1, allChips[0])

        assertEquals(chip2, allChips[1])
    }

    // TODO chipDao update and delete

    @Test
    @Throws(Exception::class)
    fun taskDaoInsert_insertAllTasksIntoDb() = runBlocking {
        addTwoTasksToDb()
        val task1 = taskDao.getTasksWithChip(chip1.id).first()[0]
        val task2 = taskDao.getTasksWithChip(chip2.id).first()[0]
        assertEquals(task1_oneTimeTask, task1)
        assertEquals(task2_oneTimeTask_repeatOnSunMonSat, task2)
    }

    @Test
    @Throws(Exception::class)
    fun taskDaoGetAllTaskWithRepeatPattern_returnsAllItemsFromDb() = runBlocking {
        addTwoTasksToDb()

        // we make time useless in our test
        val cal = Calendar.getInstance()
        cal.clear()

        val task = taskDao.getTasksForTwoDays(bitmask = Week.SATURDAY.mask, startDate = cal, endDate = cal).first()[0]
        assertEquals(task2_oneTimeTask_repeatOnSunMonSat, task)
    }



}