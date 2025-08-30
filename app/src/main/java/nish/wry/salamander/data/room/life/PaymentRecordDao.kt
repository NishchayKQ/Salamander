package nish.wry.salamander.data.room.life

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PaymentRecordDao {
    @Insert
    suspend fun insert(paymentRecord: PaymentRecord)

    @Update
    suspend fun update(paymentRecord: PaymentRecord)

    @Query(
        "DELETE FROM payment_record " +
                "WHERE payment_record_id = :paymentRecordId"
// this limit messes up  "limit 1"
    )
    suspend fun delete(paymentRecordId: Int)
}