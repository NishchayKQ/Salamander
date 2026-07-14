package nish.wry.salamander.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import nish.wry.salamander.data.room.life.PaymentChip
import nish.wry.salamander.data.room.life.PaymentRecord
import nish.wry.salamander.data.room.life.PendingTransactionRecord

interface PaymentRepository {
    suspend fun addPaymentChip(paymentChip: PaymentChip)

    suspend fun updatePaymentChip(paymentChip: PaymentChip)

    suspend fun deletePaymentChip(paymentChipId: Int)

    suspend fun addPaymentRecord(paymentRecord: PaymentRecord)

    suspend fun updatePaymentRecord(paymentRecord: PaymentRecord)

    suspend fun deletePaymentRecord(paymentRecordId: Int)

    fun getAllPaymentChips(): Flow<List<PaymentChip>>

    fun getPaymentChip(paymentChipId: Int): Flow<PaymentChip>

    fun getPaymentRecord(paymentRecordId: Int): Flow<PaymentRecord>

    fun getAllSuccessfulPayments(): Flow<PagingData<PaymentRecord>>

    suspend fun addPendingTransaction(pendingTransactionRecord: PendingTransactionRecord)

    suspend fun confirmPendingTransaction(pendingTransactionRecord: PendingTransactionRecord)
}