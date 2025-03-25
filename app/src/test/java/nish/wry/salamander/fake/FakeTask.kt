package nish.wry.salamander.fake

import nish.wry.salamander.data.room.task.Task
import java.util.Calendar

internal object FakeTask {
    private var one = 0

    private val timeBased_1 = Task(
        id = one++,
        name = "go to college 💀 at 4am",
        dateTime = calendarFromTime(4, 0),
        chipId = 1
    )

    private val timeBased_2 = Task(
        id = one++,
        name = "buy bread 🍞 at 6pm",
        dateTime = calendarFromTime(18, 0),
        chipId = 1
    )

    private val offsetBased = Task(
        id = one++,
        name = "offset 1hr task",
        offsetHours = 1,
        chipId = 1,
    )

    private val collision_1 = Task(
        id = one++,
        name = "collision at 3pm",
        dateTime = calendarFromTime(15, 0),
        chipId = 1
    )

    private val collision_2 = Task(
        id = one++,
        name = "collision at 3pm",
        dateTime = calendarFromTime(15, 0),
        chipId = 1
    )

    private val collision_3 = Task(
        id = one++,
        name = "collision at 3:15pm",
        dateTime = calendarFromTime(15, 15),
        chipId = 1
    )

    // if i add this it should take space worth 2 cols but doesn't (the space by 1+2)
    val fail_collision_1 = Task(
        id = one++,
        name = "collision at 3:35 pm",
        dateTime = calendarFromTime(15, 35),
        chipId = 1
    )

    val allTask_NoOffsetTask = listOf(
        timeBased_1,
        timeBased_2,
        offsetBased,
        collision_1,
        collision_2,
        collision_3,
    )


}

private const val DATE_15TH = 15

@Suppress("SameParameterValue")
internal fun calendarFromTime(hour: Int, mins: Int): Calendar {
    val calendar = Calendar.getInstance()
    calendar.clear()
    calendar.set(2025, 2, DATE_15TH)
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, mins)
    return calendar
}