package nish.wry.salamander.data.room.suBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * is the foreign key for [ActivityInterval]
 *
 * represents a category user may select for how they are spending time, specifically how they are
 * spending a time interval, for example: study, gaming, shopping
 *
 * @param categoryId auto incrementing primary key
 * @param name name of the category
 * @param goalTimeInMins optional user specified target duration for a category
 * @param deleted a category may be marked as deleted ie won't show to user while selecting a category
 * but still exists in the database to maintain integrity for past records
 * **/
@Entity(tableName = "category")
data class Category(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("category_id")
    val categoryId: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "goal_time_in_mins")
    val goalTimeInMins: Int? = null,
    // icon
    // color
    @ColumnInfo(name = "deleted")
    val deleted: Boolean = false,
)