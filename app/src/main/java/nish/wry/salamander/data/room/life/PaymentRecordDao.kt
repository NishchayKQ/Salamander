package nish.wry.salamander.data.room.life

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import nish.wry.salamander.data.UPIApp
import kotlin.time.Instant

@Suppress("FunctionName")
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

    /** confirms a pending transaction if any**/
    @Query(
        "update payment_record set " +
                "transaction_pending = 0, " +
                "time_of_transaction=:instant " +
                "where transaction_pending = 1 AND upi_app_enum = :upiApp " +
                "AND merchant_name = :merchantName AND amount = :amount"
    )
    suspend fun confirmPendingTransaction(
        upiApp: UPIApp,
        amount: Double,
        merchantName: String,
        instant: Instant,
    )

    /**
     * adds a new pending transaction for [upiApp] if there already exists one, then overwrites it.
     * therefore at max for a upiApp there can't be more than one pending transaction
     * it checks if amount and merchantName are different before updating
     * **/
    @Transaction
    suspend fun addPendingTransaction(
        upiApp: UPIApp,
        amount: Double,
        merchantName: String,
        instant: Instant,
    ) {
        val pendingTransaction = _returnPendingTransactionIfAny(upiApp)
        if (pendingTransaction != null) {
            // only update if they are actually changed, we don't check time as we update it when transaction completes anyways
            if (amount != pendingTransaction.amount || merchantName != pendingTransaction.merchantName) {
                _updatePendingTransaction(
                    paymentRecordId = pendingTransaction.paymentRecordId,
                    upiApp = upiApp,
                    amount = amount,
                    merchantName = merchantName,
                    instant = instant
                )
            }
        } else {
            _insertPendingTransaction(
                upiApp = upiApp,
                amount = amount,
                merchantName = merchantName,
                instant = instant
            )
        }
    }

    @Query(
        "SELECT payment_record_id, amount, merchant_name FROM payment_record " +
                "WHERE transaction_pending = 1 AND upi_app_enum = :upiApp"
    )
    suspend fun _returnPendingTransactionIfAny(upiApp: UPIApp): _PendingTransaction?

    @Query(
        "UPDATE payment_record SET " +
                "amount = :amount, " +
                "merchant_name = :merchantName, " +
                "time_of_transaction = :instant, " +
                "upi_app_enum = :upiApp " +
                "WHERE payment_record_id = :paymentRecordId "
    )
    suspend fun _updatePendingTransaction(
        paymentRecordId: Int,
        upiApp: UPIApp,
        amount: Double,
        merchantName: String,
        instant: Instant,
    )


    @Query(
        "INSERT INTO payment_record(transaction_pending, amount, merchant_name, time_of_transaction, upi_app_enum) " +
                "values(1, :amount, :merchantName, :instant, :upiApp) "
    )
    suspend fun _insertPendingTransaction(
        upiApp: UPIApp,
        amount: Double,
        merchantName: String,
        instant: Instant,
    )

}

/**internal Data class for [PaymentRecordDao.addPendingTransaction]**/
@Suppress("ClassName")
data class _PendingTransaction(
    @ColumnInfo("payment_record_id") val paymentRecordId: Int,

    @ColumnInfo(name = "amount") val amount: Double,

    @ColumnInfo("merchant_name") val merchantName: String,
)