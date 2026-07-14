package nish.wry.salamander.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nish.wry.salamander.data.Constants
import nish.wry.salamander.data.Priority
import nish.wry.salamander.data.Week
import nish.wry.salamander.data.room.task.Task
import nish.wry.salamander.domain.repository.TaskRepository
import javax.inject.Inject


const val ALARM_RECEIVER_TAG = "Alarm Receiver"

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    // we need Scheduler to reschedule for repeating task
    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var taskRepository: TaskRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val reminderId = intent.getIntExtra(Constants.EXTRA_TASK_ID, -1)
            if (reminderId == -1) {
                Log.e(ALARM_RECEIVER_TAG, "onReceive: received null reminderID")
                return
            }

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {

                try {
                    val task: Task = taskRepository.getTaskWithId(reminderId) ?: return@launch

                    // if this task is repeating, schedule the next Alarm
                    if (task.repeatOnDaysBitFlag != 0) {
                        if (task.dateTime == null) {
                            Log.e(
                                ALARM_RECEIVER_TAG,
                                "datetime is null for a repeating task. task_id = ${task.id}"
                            )
                        } else {
                            val nextCalendar = Week.findNextCalenderDayForRecurringReminder(
                                task.dateTime,
                                task.repeatOnDaysBitFlag
                            )
                            val reminder =
                                Reminder(id = task.id, timeInMillis = nextCalendar.timeInMillis)
                            scheduler.schedule(reminder)
                            Log.d(
                                ALARM_RECEIVER_TAG,
                                "Scheduled next reminder for task id = ${task.id}, today = ${task.dateTime.time}, & next fire = ${nextCalendar.time}"
                            )
                        }
                    }

                    val notificationSender = NotificationSender()

                    when (task.priority) {
                        Priority.Low -> notificationSender.sendLowPriorityNotification(
                            context,
                            task.id,
                            task.name
                        )

                        Priority.Normal -> notificationSender.sendNormalPriorityNotification(
                            context,
                            task.id,
                            task.name
                        )

                        Priority.Critical -> notificationSender.sendHighPriorityNotification(
                            context,
                            task.id,
                            task.name
                        )
                    }

                } finally {
                    pendingResult.finish()
                }
            }

        }
    }
}