package nish.wry.salamander.ui.life

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import kotlinx.serialization.Serializable
import nish.wry.salamander.R
import nish.wry.salamander.ui.navigation.MainDestination

@Serializable
object MainNishchayDestination : MainDestination {
    override val titleRes: Int = R.string.nishchay
    override val iconRes: Int = R.drawable.outline_person_24
}

@Serializable
object NishchayScreenDestination

@Composable
fun NishchayScreen(
    onScanQrClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(onClick = onScanQrClicked) {
                Icon(painterResource(R.drawable.baseline_qr_code_scanner_24), null)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

        }
    }
}
