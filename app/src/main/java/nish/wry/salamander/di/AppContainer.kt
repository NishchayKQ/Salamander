package nish.wry.salamander.di

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import nish.wry.salamander.data.DateTimeTracker
import nish.wry.salamander.data.room.SalamanderRoomDatabase

interface AppContainer {
    val taskRepository: TaskRepository
    val activityRepository: ActivityRepository
    val paymentRepository: PaymentRepository
    val dateTimeTracker: DateTimeTracker
}

class AppDataContainer(
    coroutineScope: CoroutineScope,
    context: Context,
) : AppContainer {
    private val database = SalamanderRoomDatabase.getDatabase(context)

    override val taskRepository: TaskRepository by lazy {
        OfflineTaskRepository(
            taskDao = database.taskDao(), chipDao = database.chipDao()
        )
    }

    override val activityRepository: ActivityRepository by lazy {
        OfflineActivityRepository(
            activityIntervalDao = database.activityIntervalDao(),
            categoryDao = database.categoryDao(),
            dailyLogDao = database.dailyLogDao(),
        )
    }
    override val paymentRepository: PaymentRepository by lazy {
        OfflinePaymentRepository(
            paymentChipDao = database.paymentChipDao(),
            paymentRecordDao = database.paymentRecordDao(),
        )
    }

    override val dateTimeTracker: DateTimeTracker by lazy {
        DateTimeTracker(context = context, coroutineScope = coroutineScope)
    }
}