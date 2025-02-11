package nish.wry.salamander.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@TypeConverters(Converters::class)
@Database(entities = [Chip::class, Task::class], version = 4)
abstract class SalamanderRoomDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    abstract fun chipDao(): ChipDao

    companion object {

        @Volatile
        private var Instance: SalamanderRoomDatabase? = null

        fun getDatabase(context: Context): SalamanderRoomDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context = context,
                    klass = SalamanderRoomDatabase::class.java,
                    name = "salamander_database"
                ).fallbackToDestructiveMigration().build().also { Instance = it }
            }
        }

    }


}