package nish.wry.salamander.di

import kotlinx.coroutines.flow.Flow
import nish.wry.salamander.data.room.life.PaymentChip
import nish.wry.salamander.data.room.life.PaymentChipDao
import nish.wry.salamander.data.room.life.PaymentRecord
import nish.wry.salamander.data.room.life.PaymentRecordDao

interface PaymentRepository {
    suspend fun addPaymentChip(paymentChip: PaymentChip)

    suspend fun updatePaymentChip(paymentChip: PaymentChip)

    suspend fun addPaymentRecord(paymentRecord: PaymentRecord)

    suspend fun updatePaymentRecord(paymentRecord: PaymentRecord)

    suspend fun deletePaymentRecord(paymentRecordId: Int)

    fun getAllPaymentChips(): Flow<List<PaymentChip>>

    fun getPaymentChip(paymentChipId: Int): Flow<PaymentChip>
}

class OfflinePaymentRepository(
    private val paymentChipDao: PaymentChipDao,
    private val paymentRecordDao: PaymentRecordDao,
) : PaymentRepository {
    override suspend fun addPaymentChip(paymentChip: PaymentChip) =
        paymentChipDao.insert(paymentChip = paymentChip)

    override suspend fun updatePaymentChip(paymentChip: PaymentChip) =
        paymentChipDao.update(paymentChip = paymentChip)

    override suspend fun addPaymentRecord(paymentRecord: PaymentRecord) =
        paymentRecordDao.insert(paymentRecord = paymentRecord)

    override suspend fun updatePaymentRecord(paymentRecord: PaymentRecord) =
        paymentRecordDao.update(paymentRecord = paymentRecord)

    override suspend fun deletePaymentRecord(paymentRecordId: Int) =
        paymentRecordDao.delete(paymentRecordId = paymentRecordId)

    override fun getAllPaymentChips(): Flow<List<PaymentChip>> = paymentChipDao.getAllPaymentChips()

    override fun getPaymentChip(paymentChipId: Int): Flow<PaymentChip> =
        paymentChipDao.getPaymentChip(paymentChipId = paymentChipId)

}