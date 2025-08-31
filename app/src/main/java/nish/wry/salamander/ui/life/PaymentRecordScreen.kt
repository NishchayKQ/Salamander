package nish.wry.salamander.ui.life

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable
import nish.wry.salamander.data.room.life.PaymentChip
import nish.wry.salamander.ui.AppViewModelProvider
import nish.wry.salamander.ui.common.SalamanderSingleInputChip

@Serializable
object PaymentRecordScreenDestination

@Composable
fun PaymentRecordScreen(
    modifier: Modifier = Modifier,
    onCreatePaymentChipClicked: () -> Unit,
    onExitRequested: () -> Unit,
    viewModel: PaymentRecordScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {

    val paymentScreenUiState by viewModel.paymentScreenUiState.collectAsState()
    val paymentChips by viewModel.paymentChips.collectAsState()

    Scaffold(
        modifier = modifier
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            OutlinedTextField(
                value = paymentScreenUiState.merchantName,
                onValueChange = viewModel::updateMerchantNameAsPerInput,
                isError = !paymentScreenUiState.isMerchantNameValid,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                label = { Text("merchant name") }

            )


            OutlinedTextField(
                value = paymentScreenUiState.amount,
                onValueChange = viewModel::updateAmountAsPerInput,
                isError = !paymentScreenUiState.isAmountValid,
                label = { Text("amount") },
                prefix = { Text("₹") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done
                ),
            )

            SalamanderSingleInputChip(
                chips = paymentChips,
                selectedChipId = paymentScreenUiState.paymentChipId ?: -1,
                getChipId = PaymentChip::paymentChipId,
                getChipName = PaymentChip::name,
                getChipDeleted = PaymentChip::deleted,
                onChipSelected = viewModel::setPaymentChip,
                onCreateChipClicked = onCreatePaymentChipClicked
            )
        }

    }

}