package nish.wry.salamander.di

import android.content.Context
import nish.wry.salamander.data.room.SalamanderRoomDatabase

interface AppContainer {
    val taskRepository: TaskRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val taskRepository: TaskRepository by lazy {
        val database = SalamanderRoomDatabase.getDatabase(context)
        OfflineTaskRepository(
            taskDao = database.taskDao(), chipDao = database.chipDao()
        )
    }
}