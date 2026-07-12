package nish.wry.salamander.ui.life

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.serialization.Serializable
import nish.wry.salamander.R
import nish.wry.salamander.ui.navigation.MainDestination
import kotlin.time.Instant

@Serializable
object MainNishchayDestination : MainDestination {
    override val titleRes: Int = R.string.nishchay
    override val iconRes: Int = R.drawable.outline_person_24
}

@Serializable
object NishchayScreenDestination

// payment records will move over to subset of this screen in future
@Composable
fun NishchayScreen(
    onPaymentRecordClicked: (Int) -> Unit,
    onAddPaymentRecordClicked: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PaymentHistoryViewModel = hiltViewModel(),
) {
    // Collect the Flow as LazyPagingItems
    val lazyTransactionItems = viewModel.transactions.collectAsLazyPagingItems()
    Scaffold(
        modifier.fillMaxSize(), floatingActionButton = {
            FloatingActionButton(onClick = onAddPaymentRecordClicked) {
                Icon(painterResource(R.drawable.outline_add_24), null)
            }
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn {
                items(
                    count = lazyTransactionItems.itemCount,
                    key = lazyTransactionItems.itemKey { it.paymentRecordId }) { index ->
                    val paymentRecord = lazyTransactionItems[index]
                    if (paymentRecord != null) {
                        PaymentRecordCard(
                            merchantName = paymentRecord.merchantName,
                            amount = paymentRecord.amount,
                            timeOfTransaction = paymentRecord.timeOfTransaction,
                            modifier = Modifier.clickable(onClick = {
                                onPaymentRecordClicked(
                                    paymentRecord.paymentRecordId
                                )
                            })
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentRecordCard(
    merchantName: String,
    amount: Double,
    timeOfTransaction: Instant,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(merchantName)

        Text("₹$amount")
    }
}