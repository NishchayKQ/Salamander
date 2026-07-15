package nish.wry.salamander.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.ColumnInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nish.wry.salamander.domain.repository.TaskRepository
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var alarmScheduler: Scheduler

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val currentTimeInMillis = System.currentTimeMillis()
                    val scheduledTasks =
                        taskRepository.getAllCalendarRelevantTask(currentTimeInMillis)

                    scheduledTasks.forEach { scheduledTask: ScheduledTask ->
                        // this is an old scheduledTask that's of repeating type
                        if (scheduledTask.timeInMillis <= currentTimeInMillis) {

                            if (scheduledTask.weekdaysBitflag == 0) {
                                Timber.e("it's not possible for those task to be here, db query is (relevant time or bitflag set) [Task#${scheduledTask.id}]")
                                return@forEach
                            }

                            val nextTime = calculateNextReminderTime(
                                lastScheduledTime = scheduledTask.timeInMillis,
                                weekdaysBitmask = scheduledTask.weekdaysBitflag,
                                currentTime = currentTimeInMillis
                            )

                            alarmScheduler.schedule(
                                Reminder(
                                    id = scheduledTask.id,
                                    timeInMillis = nextTime
                                )
                            )
                        } else
                            alarmScheduler.schedule(scheduledTask.toReminder())
                    }

                    Timber.d("BOOT_COMPLETE : scheduled ${scheduledTasks.size} alarms")

                } finally {
                    pendingResult.finish()
                }
            }

        }

    }
}


data class ScheduledTask(
    val id: Int,
    @ColumnInfo(name = "date_time")
    val timeInMillis: Long,
    @ColumnInfo(name = "weekdays_bitflag")
    val weekdaysBitflag: Int,
)

fun ScheduledTask.toReminder(): Reminder {
    return Reminder(
        id, timeInMillis
    )
}