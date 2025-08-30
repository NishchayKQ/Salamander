package nish.wry.salamander.ui.life

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable
import nish.wry.salamander.ui.AppViewModelProvider
import nish.wry.salamander.ui.common.SalamanderSaveAndCancelButtons
import nish.wry.salamander.ui.common.SalamanderSwitch

@Serializable
object PaymentChipScreenDestination

@Composable
fun PaymentChipScreen(
    onExitRequested: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentChipViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {

    val uiState by viewModel.paymentChipUiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // name of chip
            TextField(
                value = uiState.chipName,
                onValueChange = viewModel::updateChipName,
                label = { Text("category name") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                isError = !uiState.isChipNameValid,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SalamanderSwitch(
                checked = uiState.defaultMerchantNameEnabled,
                onCheckedChange = viewModel::toggleDefaultMerchantNameEnabled,
                switchText = "default merchant name",
                toolTip = "whether to give this a default name that will override default QR names like 'verified merchant'",
                richToolTipEnabled = true
            )

            // default merchant name
            OutlinedTextField(
                value = uiState.merchantName,
                onValueChange = viewModel::updateMerchantName,
//                label = { Text("default merchant name") },
                enabled = uiState.defaultMerchantNameEnabled,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                isError = !uiState.isMerchantNameValid,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SalamanderSwitch(
                checked = uiState.defaultAmountEnabled,
                onCheckedChange = viewModel::toggleDefaultAmountEnabled,
                switchText = "default amount",
                toolTip = "payments of this category will have this amount by default\nnote: if the QR has an amount it won't override it",
                richToolTipEnabled = true
            )
            // default amount
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::updateAmount,
//                label = { Text("default amount") },
                enabled = uiState.defaultAmountEnabled,
                isError = !uiState.isAmountValid,
                prefix = { Text("₹") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SalamanderSwitch(
                checked = uiState.limitEnabled,
                onCheckedChange = viewModel::toggleLimitEnabled,
                switchText = "limit",
                toolTip = "it won't stop you from purchases~",
                richToolTipEnabled = true
            )

            // preferred limit
            OutlinedTextField(
                value = uiState.limit,
                onValueChange = viewModel::updateLimit,
                enabled = uiState.limitEnabled,
                isError = !uiState.isLimitValid,
                prefix = { Text("₹") },
                placeholder = { Text("0") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SalamanderSaveAndCancelButtons(
                coroutineScope = coroutineScope,
                isEntryValid = uiState.isStateValid,
                saveFunction = viewModel::savePaymentChip,
                exitFunction = onExitRequested,
                modifier = Modifier.padding(bottom = 16.dp)
            )

        }


    }
}