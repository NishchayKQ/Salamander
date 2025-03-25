package nish.wry.salamander.data.room.suBase

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    /**
     * @return List of [CategoryUiData] which have not been marked as deleted
     *
     * note: category deleted by user are kept for data integrity reasons**/
    @Query("select category_id, name, goal_time_in_mins from category where deleted = 0")
    fun getAllCategory(): Flow<List<CategoryUiData>>

    /**add a new [Category] to db**/
    @Insert
    suspend fun addCategory(category: Category)

//    #TODO for repository
    /**delete category only if no other data references it, hmm doesn't sqlite do this automatically?
     * maybe we should just catch this no change from sqlite and push an update instead,
     */
    @Delete
    suspend fun deleteCategory(category: Category)

    /**update a existing category**/
    @Update
    suspend fun updateCategory(category: Category)
}

data class CategoryUiData(
    @ColumnInfo("category_id")
    val id: Int,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "goal_time_in_mins")
    val goalTimeInMins: Int?,
)