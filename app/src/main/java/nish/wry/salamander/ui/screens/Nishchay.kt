package nish.wry.salamander.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
//    onBackButtonPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
//    BackHandler(onBack = onBackButtonPressed)

    Scaffold(modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("system destroyed???????🤯🤯🤯")
        }
    }
}