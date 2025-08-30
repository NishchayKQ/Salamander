package nish.wry.salamander.data.room.life

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Calendar


/**
 * @param paymentChipId the [PaymentChip] this payment record is bound to
 * @param transactionNote user set transaction note for the payment
 * @param originalUri the original uri the qr code had
 * **/
@Entity(
    tableName = "payment_record", foreignKeys = [ForeignKey(
        entity = PaymentChip::class,
        parentColumns = ["payment_chip_id"],
        childColumns = ["payment_chip_id"],
    )]
)
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("payment_record_id")
    val paymentRecordId: Int = 0,

    @ColumnInfo("payment_chip_id", index = true)
    val paymentChipId: Int,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo("merchant_name")
    val merchantName: String,

    @ColumnInfo(name = "transaction_note")
    val transactionNote: String?,

    @ColumnInfo(name = "time_of_transaction")
    val timeOfTransaction: Calendar,

    @ColumnInfo(name = "original_uri")
    val originalUri: String,
)
