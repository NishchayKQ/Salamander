package nish.wry.salamander.ui.life

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.parcelize.Parcelize
import nish.wry.salamander.data.MutableSaveStateFlow
import nish.wry.salamander.data.room.life.PaymentChip
import nish.wry.salamander.di.GetAllChipsUseCase
import nish.wry.salamander.di.PaymentRepository
import kotlin.time.Instant

class PaymentRecordScreenViewModel(
    savedStateHandle: SavedStateHandle,
    getAllPaymentChipsUseCase: GetAllChipsUseCase<PaymentChip>,
    private val paymentRepository: PaymentRepository,
) : ViewModel() {
    private val _paymentRecordScreenUiData =
        MutableSaveStateFlow(savedStateHandle, PAYMENT_SCREEN_KEY, PaymentRecordScreenUiData())

    val paymentScreenUiState = _paymentRecordScreenUiData.asStateFlow()
    val paymentChips: StateFlow<List<PaymentChip>> = getAllPaymentChipsUseCase(viewModelScope)

    private var originalMerchantName: String = ""

    // TODO limit check is not enabled rn paymentChip.preferredLimit != null
    @Suppress("unused")
    @OptIn(ExperimentalCoroutinesApi::class)
    private val _selectChipSideEffects: Job = paymentScreenUiState
        // on each update of uiState we extract just the paymentChipId
        .map { uiState ->
            uiState.paymentChipId
        }
        // even if paymentScreenUiState is a StateFlow, we map and create a flow for the paymentChipId, this makes duplicate entries in flow
        .distinctUntilChanged()
        // it might be null so drop it here
        .filterNotNull()
        // if paymentChipId changes, cancel previous repository fetch
        .flatMapLatest { paymentChipId ->
            paymentRepository.getPaymentChip(paymentChipId).map { paymentChip ->

                // if no preset show original stuff
                _paymentRecordScreenUiData.update { cur ->
                    cur.copy(
                        merchantName = paymentChip.defaultMerchantName ?: originalMerchantName
                    )
                }


            }
        }
        // flow's don't start until they are subscribed, so we manually launch it, yay! no more dummy Stateflow's
        .launchIn(viewModelScope)

    // TODO gpay strips all chars and extracts the nums so 3r45f becomes 345, try this
    fun updateAmountAsPerInput(value: String) {
        if (value.toDoubleOrNull() == null && value.isNotEmpty()) return
        _paymentRecordScreenUiData.update { cur ->
            cur.copy(amount = value)
        }
    }

    fun updateMerchantNameAsPerInput(value: String) {
        _paymentRecordScreenUiData.update { cur ->
            cur.copy(merchantName = value)
        }
    }

    fun setPaymentChip(paymentChipId: Int) {
        _paymentRecordScreenUiData.update { cur ->
            cur.copy(paymentChipId = paymentChipId)
        }
    }


    private companion object {
        private const val PAYMENT_SCREEN_KEY = "PAYMENT_SCREEN_KEY"
    }

}


@Parcelize
data class PaymentRecordScreenUiData(
    val amount: String = "",
    val merchantName: String = "",
    val transactionNote: String = "",
    val paymentChipId: Int? = null,
    // TODO we need it in ui state, as user could edit the time later
    val timeOfTransaction: Instant? = null,
) : Parcelable {
    val isMerchantNameValid: Boolean
        get() = merchantName.isNotBlank()

    val isAmountValid: Boolean
        get() = amount.isNotBlank()

    val isStateValid: Boolean
        get() = isMerchantNameValid && isAmountValid && paymentChipId != null
}

