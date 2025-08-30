package nish.wry.salamander.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import nish.wry.salamander.data.room.life.PaymentChip
import nish.wry.salamander.data.room.life.PaymentChipDao
import nish.wry.salamander.data.room.life.PaymentRecord
import nish.wry.salamander.data.room.life.PaymentRecordDao
import nish.wry.salamander.data.room.suBase.ActivityInterval
import nish.wry.salamander.data.room.suBase.ActivityIntervalDao
import nish.wry.salamander.data.room.suBase.Category
import nish.wry.salamander.data.room.suBase.CategoryDao
import nish.wry.salamander.data.room.suBase.DailyLog
import nish.wry.salamander.data.room.suBase.DailyLogDao
import nish.wry.salamander.data.room.task.Chip
import nish.wry.salamander.data.room.task.ChipDao
import nish.wry.salamander.data.room.task.Task
import nish.wry.salamander.data.room.task.TaskDao

@TypeConverters(Converters::class)
@Database(
    entities = [Chip::class, Task::class, ActivityInterval::class, Category::class, DailyLog::class, PaymentChip::class, PaymentRecord::class],
    version = 9
)
abstract class SalamanderRoomDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    abstract fun chipDao(): ChipDao

    abstract fun activityIntervalDao(): ActivityIntervalDao

    abstract fun categoryDao(): CategoryDao

    abstract fun dailyLogDao(): DailyLogDao

    abstract fun paymentChipDao() : PaymentChipDao

    abstract fun paymentRecordDao(): PaymentRecordDao

    companion object {

        @Volatile
        private var Instance: SalamanderRoomDatabase? = null

        fun getDatabase(context: Context): SalamanderRoomDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context = context,
                    klass = SalamanderRoomDatabase::class.java,
                    name = "salamander_database"
                )
                    .fallbackToDestructiveMigration(true)
//                    .createFromAsset()
                    .build().also { Instance = it }
            }
        }
    }
}