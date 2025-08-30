package nish.wry.salamander.data.room.life

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * user made chip to categorise payments into
 * @param defaultAmount the amount that should be set for the transaction when the user clicks on the tag, note: null means its not set
 * @param deleted whether this chip is marked as deleted (but kept for data integrity purpose)
 * **/
@Entity(tableName = "payment_chip")
data class PaymentChip(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("payment_chip_id")
    val paymentChipId: Int = 0,

    @ColumnInfo("name")
    val name: String,

    @ColumnInfo("default_amount")
    val defaultAmount: Double? = null,

    @ColumnInfo("default_merchant_name")
    val defaultMerchantName: String? = null,

    @ColumnInfo("preferred_limit")
    val preferredLimit: Double? = null,

    @ColumnInfo("deleted")
    val deleted: Boolean = false,
)