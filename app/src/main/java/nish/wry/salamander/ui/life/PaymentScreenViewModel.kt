package nish.wry.salamander.ui.life

import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.util.Log
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
import nish.wry.salamander.data.UPIConstants
import nish.wry.salamander.data.room.life.PaymentChip
import nish.wry.salamander.di.GetAllChipsUseCase
import nish.wry.salamander.di.PaymentRepository
import java.time.LocalTime

class PaymentScreenViewModel(
    savedStateHandle: SavedStateHandle,
    getAllPaymentChipsUseCase: GetAllChipsUseCase<PaymentChip>,
    private val paymentRepository: PaymentRepository,
) : ViewModel() {
    private val _paymentScreenUiState =
        MutableSaveStateFlow(savedStateHandle, PAYMENT_SCREEN_KEY, PaymentScreenUiState())

    val paymentScreenUiState = _paymentScreenUiState.asStateFlow()
    val paymentChips: StateFlow<List<PaymentChip>> = getAllPaymentChipsUseCase(viewModelScope)

    private var originalMerchantName: String = ""
    private var originalAmount: String = ""

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
                _paymentScreenUiState.update { cur ->
                    cur.copy(
                        amount = paymentChip.defaultAmount?.toString() ?: originalAmount,
                        merchantName = paymentChip.defaultMerchantName ?: originalMerchantName
                    )
                }


            }
        }
        // flow's don't start until they are subscribed, so we manually launch it, yay! no more dummy Stateflow's
        .launchIn(viewModelScope)

    fun setQrScanAsDone() {
        _paymentScreenUiState.update { cur ->
            cur.copy(qrScanDone = true)
        }
    }

    fun saveAndParseOriginalUri(uri: Uri) {
        val mapOfExtraParams = mutableMapOf<String, String>()

        uri.queryParameterNames.forEach { param ->
            val value = uri.getQueryParameter(param)
            if (value != null) mapOfExtraParams[param] = value
        }

        val amount: Double? = mapOfExtraParams[UPIConstants.AMOUNT]?.toDoubleOrNull()
        mapOfExtraParams.remove(UPIConstants.AMOUNT)

        val merchantName: String? = mapOfExtraParams[UPIConstants.MERCHANT_NAME]
        mapOfExtraParams.remove(UPIConstants.MERCHANT_NAME)

        val transactionNote: String? = mapOfExtraParams[UPIConstants.TRANSACTION_NOTE]
        mapOfExtraParams.remove(UPIConstants.TRANSACTION_NOTE)

        val upiId: String? = mapOfExtraParams[UPIConstants.UPI_ID]
        mapOfExtraParams.remove(UPIConstants.UPI_ID)

        originalMerchantName = merchantName ?: ""
        originalAmount = amount?.toString() ?: ""

        _paymentScreenUiState.update { cur ->
            cur.copy(
                originalUri = uri,
                amount = amount?.toString() ?: cur.amount,
                // if amount is null, allow user to edit it
                isAmountEditable = amount == null,
                merchantName = merchantName ?: cur.merchantName,
                transactionNote = transactionNote ?: cur.transactionNote,
                upiId = upiId ?: cur.upiId,
                mapOfExtraParams = mapOfExtraParams
            )
        }
    }

    // TODO gpay strips all chars and extracts the nums so 3r45f becomes 345, try this
    fun updateAmountAsPerInput(value: String) {
        if (value.toDoubleOrNull() == null && value.isNotEmpty()) return
        _paymentScreenUiState.update { cur ->
            cur.copy(amount = value)
        }
    }

    fun updateMerchantNameAsPerInput(value: String) {
        _paymentScreenUiState.update { cur ->
            cur.copy(merchantName = value)
        }
    }

    fun setPaymentChip(paymentChipId: Int) {
        _paymentScreenUiState.update { cur ->
            cur.copy(paymentChipId = paymentChipId)
        }
    }

    private fun buildUpiUri(uiState: PaymentScreenUiState = paymentScreenUiState.value): Uri {
        if (uiState.isStateValid) {
            val uri = Uri.Builder()
                .scheme(UPIConstants.SCHEME)
                .authority(UPIConstants.AUTHORITY)
                .appendQueryParameter(UPIConstants.UPI_ID, uiState.upiId)
                .appendQueryParameter(UPIConstants.MERCHANT_NAME, uiState.merchantName)
                .appendQueryParameter(UPIConstants.AMOUNT, uiState.amount)
                .appendQueryParameter(UPIConstants.CURRENCY, UPIConstants.INR)

            if (uiState.transactionNote.isNotEmpty())
                uri.appendQueryParameter(UPIConstants.TRANSACTION_NOTE, uiState.transactionNote)

            // adding back the unused params in the upi QR
            uiState.mapOfExtraParams.forEach { (key, value) ->
                uri.appendQueryParameter(key, value)
            }

            return uri.build()

        } else return Uri.EMPTY
    }

    fun buildPaymentIntent(): Intent {
        val uri = buildUpiUri()
        if (uri != Uri.EMPTY) {
            val paymentIntent = Intent(Intent.ACTION_VIEW)
            paymentIntent.data = uri
//            paymentIntent.setPackage(UPIAppsPackageName.GOOGLE_PAY)
            Log.d(PAY, "buildPaymentIntent: $uri")
            return paymentIntent
        } else {
            return Intent()

        }
    }


    private companion object {
        private const val PAYMENT_SCREEN_KEY = "PAYMENT_SCREEN_KEY"
    }

}

/**
 * @param mapOfExtraParams the params in the upi uri that were not extracted, (we only parse [amount], [merchantName], [transactionNote]
 * **/
@Parcelize
data class PaymentScreenUiState(
    val qrScanDone: Boolean = false,
    val isAmountEditable: Boolean = false,
    val amount: String = "",
    val merchantName: String = "",
    val transactionNote: String = "",
    val upiId: String = "",
    val paymentChipId: Int? = null,
    // TODO we need it in ui state, as user could edit the time later
    val timeOfTransaction: LocalTime? = null,
    val originalUri: Uri = Uri.EMPTY,
    val mapOfExtraParams: Map<String, String> = emptyMap(),
) : Parcelable {
    val isMerchantNameValid: Boolean
        get() = merchantName.isNotBlank()

    val isAmountValid: Boolean
        get() = amount.isNotBlank()

    val isStateValid: Boolean
        get() = isMerchantNameValid && isAmountValid && upiId.isNotBlank() && paymentChipId != null
}

