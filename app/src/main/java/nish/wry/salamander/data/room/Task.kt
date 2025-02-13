package nish.wry.salamander.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import nish.wry.salamander.data.Priority
import java.util.Calendar

//TODO do we need custom onDelete and onUpdate policy?

// TODO do integrity check before adding stuff here, if floatingOffset is null then DateTime needs to set, but if its not null then dateTime = null
@Entity(
    tableName = "task",
    foreignKeys = [ForeignKey(
        entity = Chip::class,
        parentColumns = ["id"],
        childColumns = ["task_chip"],
    )]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "task_chip", index = true)
    val taskChipId: Int,

    val name: String,

    @ColumnInfo(name = "weekdays_bitflag")
    val repeatOnDaysBitFlag: Int = 0,

    @ColumnInfo(name = "date_time")
    val dateTime: Calendar? = null,

    @ColumnInfo(name = "floating_offset_hours")
    val floatingOffsetHours: Int? = null,

    val priority: Priority = Priority.Normal,
)
