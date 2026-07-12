package nish.wry.salamander.di

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import nish.wry.salamander.data.Priority

@HiltAndroidApp
class SalamanderApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // create notification channels our app will use
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationManager: NotificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            // notification channel for tasks of all priorities
            Priority.entries.forEach { priority ->
                val name = priority.nameSeenToUser
                val descriptionText = priority.description
                val importance = priority.importance

                val channel = NotificationChannel(priority.name, name, importance).apply {
                    description = descriptionText
                }

                // Register the channel with the system.

                notificationManager.createNotificationChannel(channel)

            }

        }
    }
}