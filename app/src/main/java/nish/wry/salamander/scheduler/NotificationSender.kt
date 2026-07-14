package nish.wry.salamander.scheduler

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import nish.wry.salamander.MainActivity
import nish.wry.salamander.ReminderActivity
import nish.wry.salamander.R
import nish.wry.salamander.data.Constants
import nish.wry.salamander.data.Priority

class NotificationSender {
    fun sendLowPriorityNotification(
        context: Context,
        notificationId: Int,
        title: String,
    ) {

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, Priority.Low.name)
            .setSmallIcon(R.drawable.outline_timer_24)
            .setContentTitle(title)
            // android 7.1 and below use this. android 8 and above instead use the channel's importance
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array&lt;out String&gt;,
                //                                        grantResults: IntArray)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return@with
            }
            // notificationId is a unique int for each notification that you must define.
            notify(notificationId, builder.build())
        }

    }

    @SuppressLint("FullScreenIntentPolicy")
    fun sendNormalPriorityNotification(
        context: Context,
        notificationId: Int,
        title: String,
    ) {
        val reminderIntent = Intent(context, ReminderActivity::class.java).apply {
            putExtra(Constants.EXTRA_TASK_NAME, title)
            putExtra(Constants.EXTRA_TASK_ID, notificationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }

        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, reminderIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, Priority.Normal.name)
            .setSmallIcon(R.drawable.outline_timer_24)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true) // Wakes the screen!
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }
            // notificationId is a unique int for each notification that you must define.
            notify(notificationId, builder.build())
        }
    }

    /** sends high priority notifications with sound and full screen alerts**/
    @SuppressLint("FullScreenIntentPolicy")
    fun sendHighPriorityNotification(
        context: Context,
        notificationId: Int,
        title: String,
    ) {
        val reminderIntent = Intent(context, ReminderActivity::class.java).apply {
            putExtra(Constants.EXTRA_TASK_NAME, title)
            putExtra(Constants.EXTRA_TASK_ID, notificationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                context,
                notificationId,
                reminderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val builder = NotificationCompat.Builder(context, Priority.Critical.name)
            .setSmallIcon(R.drawable.outline_timer_24)
            .setContentTitle(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true)
            // this will remove the notif and sound but not the activity
            // meaning the screen + text of reminder remains, perfect!
            .setTimeoutAfter(300_000L)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }
            val notification = builder.build()
            // this makes the alarm sound loop
            notification.flags = notification.flags or Notification.FLAG_INSISTENT
            notify(notificationId, notification)
        }
    }
}