package nish.wry.salamander.data.room.task

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChipDao {
    @Query("select * from chip")
    fun getAllChips(): Flow<List<Chip>>

    @Query("select * from chip where id = :id")
    fun getChipWithId(id: Int): Flow<Chip>

    @Insert
    suspend fun insert(chip: Chip)

    @Delete
    suspend fun delete(chip: Chip)

    @Update
    suspend fun update(chip: Chip)

}