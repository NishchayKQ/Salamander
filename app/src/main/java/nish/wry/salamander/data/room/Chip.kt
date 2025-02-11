package nish.wry.salamander.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import nish.wry.salamander.data.Priority
import nish.wry.salamander.ui.chip.create.ChipUiState
import java.util.Calendar

@Entity(tableName = "chip")
data class Chip(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    @ColumnInfo(name = "weekdays_bitflag")
    val repeatOnDaysBitFlag: Int = 0,

    @ColumnInfo(name = "date_time")
    val dateTime: Calendar? = null,

    @ColumnInfo(name = "floating_offset_hours")
    val floatingOffsetHours: Int? = null,

    val priority: Priority = Priority.Normal,
)

fun Chip.toChipUiState(): ChipUiState =
    ChipUiState(
        chipId = id,
        name = name,
        selectedTime = dateTime ?: Calendar.getInstance(),
        selectedWeekDaysBitmask = repeatOnDaysBitFlag,
        priority = priority,
        timeless = dateTime == null,
        offsetHours = floatingOffsetHours ?: 0,
        isInputValid = false
    )



