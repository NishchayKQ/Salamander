package nish.wry.salamander.ui.life

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parcelize
import nish.wry.salamander.data.MutableSaveStateFlow
import nish.wry.salamander.data.room.life.PaymentChip
import nish.wry.salamander.di.PaymentRepository

class PaymentChipViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: PaymentRepository,
) : ViewModel() {
    //    private val paymentChipId: Int = checkNotNull(savedStateHandle["paymentChipId"])
    private val _paymentChipUiState =
        MutableSaveStateFlow(savedStateHandle, PAYMENT_CHIP_KEY, PaymentChipUiState())

    val paymentChipUiState = _paymentChipUiState.asStateFlow()

    private companion object {
        const val PAYMENT_CHIP_KEY = "PAYMENT_CHIP_KEY"
    }

    fun toggleLimitEnabled() {
        _paymentChipUiState.update {
            it.copy(limitEnabled = !it.limitEnabled)
        }
    }

    fun toggleDefaultAmountEnabled() {
        _paymentChipUiState.update {
            it.copy(defaultAmountEnabled = !it.defaultAmountEnabled)
        }
    }

    fun toggleDefaultMerchantNameEnabled() {
        _paymentChipUiState.update {
            it.copy(defaultMerchantNameEnabled = !it.defaultMerchantNameEnabled)
        }
    }

    fun updateChipName(value: String) {
        _paymentChipUiState.update {
            it.copy(chipName = value)
        }
    }

    fun updateLimit(value: String) {
        if (value.toDoubleOrNull() == null && value.isNotBlank()) return
        _paymentChipUiState.update {
            it.copy(limit = value)
        }
    }

    fun updateMerchantName(value: String) {
        _paymentChipUiState.update {
            it.copy(merchantName = value)
        }
    }

    fun updateAmount(value: String) {
        if (value.toDoubleOrNull() == null && value.isNotBlank()) return
        _paymentChipUiState.update {
            it.copy(amount = value)
        }
    }

    suspend fun savePaymentChip(uiState: PaymentChipUiState = paymentChipUiState.value) {
        if (uiState.isStateValid) {
            repository.addPaymentChip(uiState.toPaymentChip())
        }
    }

}

@Parcelize
data class PaymentChipUiState(
    val limitEnabled: Boolean = false,
    val defaultAmountEnabled: Boolean = false,
    val defaultMerchantNameEnabled: Boolean = false,
    val chipName: String = "",
    val limit: String = "",
    val amount: String = "",
    val merchantName: String = "",
) : Parcelable {
    val isStateValid: Boolean
        get() = isChipNameValid && isMerchantNameValid && isAmountValid && isLimitValid

    val isChipNameValid: Boolean
        get() = chipName.isNotBlank()

    val isMerchantNameValid: Boolean
        get() = !defaultMerchantNameEnabled || merchantName.isNotBlank()

    val isAmountValid: Boolean
        get() = !defaultAmountEnabled || amount.isNotBlank()

    val isLimitValid: Boolean
        get() = !limitEnabled || limit.isNotBlank()
}

fun PaymentChipUiState.toPaymentChip(): PaymentChip {
    return PaymentChip(
        name = chipName,
        defaultAmount = if (defaultAmountEnabled) amount.toDoubleOrNull() else null,
        defaultMerchantName = if (defaultMerchantNameEnabled) merchantName else null,
        preferredLimit = if (limitEnabled) limit.toDoubleOrNull() else null,
        deleted = false
    )
}