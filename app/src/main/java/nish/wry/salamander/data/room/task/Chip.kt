package nish.wry.salamander.data.room.task

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import nish.wry.salamander.data.Priority
import java.util.Calendar

/**
 * a chip associated with a task, can be used to prepopulate a task with some defaults
 * @param forGroupingOnly set a chip as name only, ie don't use it as preset for task
 * @param repeatOnDaysBitFlag bitflag for days of the week task should repeat on, zero for no repetition
 * @param dateTime time for the task to trigger, null for no time set, ex: user wants to use current time, or wants a [floatingOffsetHours] instead
 * @param floatingOffsetHours how many hours from current time the task should appear floating for, null if this is off
 * @param priority a task may have one of three priorities, [Priority.Low], [Priority.Normal] or [Priority.Critical]
 * @param deleted a chip may be marked as deleted ie won't show to user while selecting a chip but still exists in the database to maintain integrity for past tasks
 * **/
@Entity(tableName = "chip")
data class Chip(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "for_grouping_only")
    val forGroupingOnly: Boolean = false,

    @ColumnInfo(name = "weekdays_bitflag")
    val repeatOnDaysBitFlag: Int = 0,

    @ColumnInfo(name = "date_time")
    val dateTime: Calendar? = null,

    @ColumnInfo(name = "floating_offset_hours")
    val floatingOffsetHours: Int? = null,

    @ColumnInfo(name = "priority")
    val priority: Priority = Priority.Normal,

    @ColumnInfo(name = "deleted")
    val deleted: Boolean = false,
)