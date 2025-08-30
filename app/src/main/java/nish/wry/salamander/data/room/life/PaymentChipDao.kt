package nish.wry.salamander.data.room.life

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentChipDao {
    @Insert
    suspend fun insert(paymentChip: PaymentChip)

    @Update
    suspend fun update(paymentChip: PaymentChip)

    @Query("SELECT * FROM payment_chip")
    fun getAllPaymentChips(): Flow<List<PaymentChip>>

    @Query("SELECT * FROM payment_chip WHERE payment_chip_id = :paymentChipId " +
            "LIMIT 1")
    fun getPaymentChip(paymentChipId: Int): Flow<PaymentChip>

}