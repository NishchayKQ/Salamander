package nish.wry.salamander.scheduler

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import nish.wry.salamander.data.Constants
import javax.inject.Inject
import javax.inject.Singleton

interface Scheduler {
    fun schedule(reminder: Reminder)

    /** @param id id of the [nish.wry.salamander.data.room.task.Task] to cancel**/
    fun cancel(id: Int)
}

data class Reminder(
    val id: Int,
    val timeInMillis: Long,
)

@Singleton
class AlarmScheduler @Inject constructor(@param:ApplicationContext private val context: Context) :
    Scheduler {
    private val alarmMgr: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun schedule(reminder: Reminder) {

        // Create an Intent to point to your BroadcastReceiver
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            // Pass the reminder's ID to the receiver
            putExtra(Constants.EXTRA_TASK_ID, reminder.id)
        }

        // Create the PendingIntent. The request code (item.id) must be unique
        // for each reminder to avoid them overwriting each other.
        val alarmIntent: PendingIntent = PendingIntent.getBroadcast(
            /* context = */ context,
            /* requestCode = */ reminder.id,
            /* intent = */ intent,
            /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmMgr.setExactAndAllowWhileIdle(
            /* type = */ AlarmManager.RTC_WAKEUP,
            /* triggerAtMillis = */ reminder.timeInMillis,
            /* operation = */ alarmIntent
        )
    }

    override fun cancel(id: Int) {
        // To cancel an alarm, you must create a PendingIntent that is
        // identical to the one you used to set it.
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id, // The same unique request code
            Intent(context, AlarmReceiver::class.java), // The same intent
            // update current makes it update the pending intent with same request code instead of creating a new intent
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmMgr.cancel(pendingIntent)
    }


}

