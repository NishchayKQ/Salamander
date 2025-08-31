package nish.wry.salamander.di

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import nish.wry.salamander.data.room.life.PaymentChip
import nish.wry.salamander.data.room.life.PaymentChipDao
import nish.wry.salamander.data.room.life.PaymentRecord
import nish.wry.salamander.data.room.life.PaymentRecordDao
import nish.wry.salamander.data.room.life.PendingTransactionRecord
import kotlin.time.Clock

interface PaymentRepository {
    suspend fun addPaymentChip(paymentChip: PaymentChip)

    suspend fun updatePaymentChip(paymentChip: PaymentChip)

    suspend fun addPaymentRecord(paymentRecord: PaymentRecord)

    suspend fun updatePaymentRecord(paymentRecord: PaymentRecord)

    suspend fun deletePaymentRecord(paymentRecordId: Int)

    fun getAllPaymentChips(): Flow<List<PaymentChip>>

    fun getPaymentChip(paymentChipId: Int): Flow<PaymentChip>

    fun getAllSuccessfulPayments(): Flow<PagingData<PaymentRecord>>

    suspend fun addPendingTransaction(pendingTransactionRecord: PendingTransactionRecord)

    suspend fun confirmPendingTransaction(pendingTransactionRecord: PendingTransactionRecord)
}

class OfflinePaymentRepository(
    private val paymentChipDao: PaymentChipDao,
    private val paymentRecordDao: PaymentRecordDao,
    // we pass this so that in unit testing we can pass custom clocks
    private val clock: Clock = Clock.System,
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

    override fun getAllSuccessfulPayments(): Flow<PagingData<PaymentRecord>> = Pager(
        config = PagingConfig(
            pageSize = 20, // no of items to load
            enablePlaceholders = false
        ), pagingSourceFactory = { paymentRecordDao.fetchAllSuccessfulPayments() }).flow

    override suspend fun addPendingTransaction(pendingTransactionRecord: PendingTransactionRecord) =
        paymentRecordDao.addPendingTransaction(
            upiApp = pendingTransactionRecord.upiApp,
            amount = pendingTransactionRecord.amount,
            merchantName = pendingTransactionRecord.merchantName,
            instant = clock.now()
        )


    override suspend fun confirmPendingTransaction(pendingTransactionRecord: PendingTransactionRecord) =
        paymentRecordDao.confirmPendingTransaction(
            upiApp = pendingTransactionRecord.upiApp,
            amount = pendingTransactionRecord.amount,
            merchantName = pendingTransactionRecord.merchantName,
            instant = clock.now()
        )

}