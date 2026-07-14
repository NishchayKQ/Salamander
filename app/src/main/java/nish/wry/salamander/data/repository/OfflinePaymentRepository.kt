package nish.wry.salamander.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import nish.wry.salamander.data.room.life.PaymentChip
import nish.wry.salamander.data.room.life.PaymentChipDao
import nish.wry.salamander.data.room.life.PaymentRecord
import nish.wry.salamander.data.room.life.PaymentRecordDao
import nish.wry.salamander.data.room.life.PendingTransactionRecord
import nish.wry.salamander.domain.repository.PaymentRepository
import javax.inject.Inject
import kotlin.time.Clock

class OfflinePaymentRepository @Inject constructor(
    private val paymentChipDao: PaymentChipDao,
    private val paymentRecordDao: PaymentRecordDao,
    // we pass this so that in unit testing we can pass custom clocks
    // unfortunately hilt uses java so it can't see this default value, and we had to add a provides method
    private val clock: Clock = Clock.System,
) : PaymentRepository {
    override suspend fun addPaymentChip(paymentChip: PaymentChip) =
        paymentChipDao.insert(paymentChip = paymentChip)

    override suspend fun updatePaymentChip(paymentChip: PaymentChip) =
        paymentChipDao.update(paymentChip = paymentChip)

    override suspend fun deletePaymentChip(paymentChipId: Int) =
        paymentChipDao.delete(paymentChipId)

    override suspend fun addPaymentRecord(paymentRecord: PaymentRecord) =
        paymentRecordDao.insert(paymentRecord = paymentRecord)

    override suspend fun updatePaymentRecord(paymentRecord: PaymentRecord) =
        paymentRecordDao.update(paymentRecord = paymentRecord)

    override suspend fun deletePaymentRecord(paymentRecordId: Int) =
        paymentRecordDao.delete(paymentRecordId = paymentRecordId)

    override fun getAllPaymentChips(): Flow<List<PaymentChip>> = paymentChipDao.getAllPaymentChips()

    override fun getPaymentChip(paymentChipId: Int): Flow<PaymentChip> =
        paymentChipDao.getPaymentChip(paymentChipId = paymentChipId)

    override fun getPaymentRecord(paymentRecordId: Int): Flow<PaymentRecord> =
        paymentRecordDao.getPaymentRecord(paymentRecordId)

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