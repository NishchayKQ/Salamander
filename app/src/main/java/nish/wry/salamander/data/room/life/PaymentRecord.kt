package nish.wry.salamander.data.room.life

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import nish.wry.salamander.data.UPIApp
import kotlin.time.Instant


/**
 * @param paymentChipId the [PaymentChip] this payment record is bound to, is null when a new transaction is detected by [nish.wry.salamander.ui.SalamanderAccessibilityService]
 * @param transactionPending raised when it is not confirm if the payment was successfully made. if its true, the payment could be cancelled
 * @param transactionNote user set transaction note for the payment
 * @param timeOfTransaction contains the time the transaction was completed, in case of pending transactions contains the time pending transaction started
 * @param upiApp the upi app this transaction took place in
 * **/
@Entity(
    tableName = "payment_record", foreignKeys = [ForeignKey(
        entity = PaymentChip::class,
        parentColumns = ["payment_chip_id"],
        childColumns = ["payment_chip_id"],
    )],
    indices = [Index("transaction_pending", "upi_app_enum")]
)
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("payment_record_id")
    val paymentRecordId: Int = 0,

    @ColumnInfo("payment_chip_id", index = true)
    val paymentChipId: Int? = null,

    @ColumnInfo("transaction_pending")
    val transactionPending: Boolean,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo("merchant_name")
    val merchantName: String,

    @ColumnInfo(name = "transaction_note")
    val transactionNote: String?,

    @ColumnInfo(name = "time_of_transaction")
    val timeOfTransaction: Instant,

    @ColumnInfo(name = "upi_app_enum")
    val upiApp: UPIApp,
)

data class PendingTransactionRecord(
    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo("merchant_name")
    val merchantName: String,

    @ColumnInfo(name = "upi_app_enum")
    val upiApp: UPIApp,
)