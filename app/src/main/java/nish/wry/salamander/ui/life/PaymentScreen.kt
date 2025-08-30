package nish.wry.salamander.ui.life

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import nish.wry.salamander.data.UPIConstants
import nish.wry.salamander.data.room.life.PaymentChip
import nish.wry.salamander.ui.AppViewModelProvider
import nish.wry.salamander.ui.common.SalamanderSingleInputChip

@Serializable
object PaymentScreenDestination

const val PAY = "PAY"


@Composable
fun PaymentScreen(
    modifier: Modifier = Modifier,
    onCreatePaymentChipClicked: () -> Unit,
    onExitRequested: () -> Unit,
    viewModel: PaymentScreenViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {

    val paymentScreenUiState by viewModel.paymentScreenUiState.collectAsState()
    val paymentChips by viewModel.paymentChips.collectAsState()


    // scanner boiler
    val options = GmsBarcodeScannerOptions.Builder().setBarcodeFormats(
        Barcode.FORMAT_QR_CODE
    ).enableAutoZoom().build()

    val scanner = GmsBarcodeScanning.getClient(LocalContext.current, options)

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
            if (!paymentScreenUiState.qrScanDone) {
                // Retrieve a ClipboardManager object
                val clipboardManager = LocalClipboard.current
                val context = LocalContext.current


                scanner.startScan().addOnSuccessListener { barcode ->
                    // Task completed successfully
                    val rawValue: String? = barcode.rawValue

                    //"upi://pay?pa=BHARATPE.9N0D0U7R2O823898@fbpe&pn=Verified Merchant&cu=INR&tn=Pay to BharatPe Merchant"
                    if (rawValue != null) {
                        val uri = rawValue.toUri()
                        if (uri.scheme == UPIConstants.SCHEME) {
                            viewModel.saveAndParseOriginalUri(uri)
                        } else {
                            Toast.makeText(
                                context,
                                "not a valid upi QR, but we copied it to ur clipboard",
                                Toast.LENGTH_LONG
                            ).show()
                            val clipData = ClipData.newPlainText("plain text", rawValue)
                            val clipEntry = ClipEntry(clipData)
                            runBlocking {
                                clipboardManager.setClipEntry(clipEntry)
                            }
                            onExitRequested()
                        }

                    } else {
                        Toast.makeText(context, "QR data null", Toast.LENGTH_LONG).show()
                    }

                    viewModel.setQrScanAsDone()


                }.addOnCanceledListener {
                    // Task canceled
                    onExitRequested()

                }.addOnFailureListener { e ->
                    // Task failed with an exception
                    val clipData = ClipData.newPlainText("plain text", e.toString())
                    val clipEntry = ClipEntry(clipData)
                    Toast.makeText(context, "failed, err copied to clipboard", Toast.LENGTH_LONG)
                        .show()

                    runBlocking {
                        clipboardManager.setClipEntry(clipEntry)
                    }

                    onExitRequested()
                }
            } else {
                // qr scan is done by the time we reach here
                val context = LocalContext.current


                // TODO handle response
                val activityLauncher =
                    rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
                        // This callback is invoked when the started activity finishes
                        if (result.resultCode == Activity.RESULT_OK) {
                            // Handle the successful result

                            // You might get data back in result.data (an Intent) if the UPI app provides it
                            val responseData: Intent? = result.data

                            val extras: Bundle? = responseData?.extras
                            val extrasString = if (extras != null) {
                                buildString {
                                    append("Bundle[")
                                    for (key in extras.keySet()) {
                                        append("\n  $key = ${extras.getString(key)}")
                                    }
                                    if (extras.keySet().isEmpty()) {
                                        append("EMPTY")
                                    } else {
                                        append("\n") // Newline after the last item for better formatting
                                    }
                                    append("]")
                                }
                            } else {
                                "null"
                            }

                            // Process responseData if needed
                            Log.d(
                                PAY,
                                "Payment successful resp: $responseData, extras: $extrasString"
                            )
                        } else {
                            // Handle cancellation or failure
                            Log.d(
                                PAY,
                                "Payment cancelled or failed. Result code: ${result.resultCode}"
                            )
                        }
                    }

                // ui

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = paymentScreenUiState.merchantName,
                    onValueChange = viewModel::updateMerchantNameAsPerInput,
                    isError = !paymentScreenUiState.isMerchantNameValid,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    label = { Text("merchant name") }

                )

                val focusRequester = remember { FocusRequester() }
                // Optional: To clear focus later if needed
                // val focusManager = LocalFocusManager.current


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
                    readOnly = !paymentScreenUiState.isAmountEditable,
                    modifier = Modifier.focusRequester(focusRequester)
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

                Button(
                    onClick = {
                        try {
                            activityLauncher.launch(viewModel.buildPaymentIntent())
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(context, "no UPI app found", Toast.LENGTH_LONG)
                                .show()
                        }
                    },
                    enabled = paymentScreenUiState.isStateValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) { Text("Pay ₹${paymentScreenUiState.amount}") }


                LaunchedEffect(Unit) {
                    // idk if needed
//                    delay(100)
                    focusRequester.requestFocus()
                }
            }

        }

    }

}

// It's generally better not to force a chooser for UPI intents
// unless you have a specific reason. The system usually handles
// picking the right UPI app.
// val chooser = Intent.createChooser(paymentIntent, "Pay with")
// activityLauncher.launch(chooser)