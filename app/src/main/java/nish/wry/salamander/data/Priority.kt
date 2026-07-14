package nish.wry.salamander.data

import android.app.NotificationManager

//note the CHANNEL_ID param of notification builder is the enum's .name
enum class Priority(val id: Int, val nameSeenToUser: String, val description: String, val importance: Int) {
    /**
     * low priority task with notification but no sound
     * **/
    Low(
        id = 0,
        nameSeenToUser = "low priority reminders",
        description = "used for reminders of low priority",
        importance = NotificationManager.IMPORTANCE_LOW
    ),

    /**
     * normal priority task with notifications + sound
     * **/
    Normal(
        id = 1,
        nameSeenToUser = "normal priority reminders",
        description = "used for reminders of normal priority",
        importance = NotificationManager.IMPORTANCE_HIGH
    ),

    /**
     * high priority task with full screen dismissable notification
     * **/
    Critical(
        id = 2,
        nameSeenToUser = "critical priority reminders",
        description = "used for reminders of critical priority",
        importance = NotificationManager.IMPORTANCE_HIGH
    );

    companion object {
        /**
         * Convert a valid priority Int to [Priority], throws [IllegalArgumentException] if id doesn't belong to any valid Priority
         * **/
        fun priorityById(id: Int): Priority {
            return when (id) {
                0 -> Low
                1 -> Normal
                2 -> Critical
                else -> {
                    throw IllegalArgumentException("ID accepted are 0 to 2, got $id")
                }
            }
        }
    }
}