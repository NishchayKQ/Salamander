package nish.wry.salamander.ui.trash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun PeopleInteractionScreen(modifier: Modifier = Modifier) {
    var testShow by rememberSaveable { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {

        
        Canvas(modifier = Modifier) {

        }

        Text("will have chips to click to search interaction with specific people. like dhoobi chip i user can add and that will let me put all interaction with them in it")
        Button(onClick = {testShow = !testShow}) {
            Text("boop")
        }
        if (testShow){
            DaBaby()
        }
    }
}

@Composable
fun DaBaby(modifier: Modifier=Modifier){
    // notice how even if its remember Saveable, it gets forgotten, cuz once its out of composition remember stuff is forgotten
    var currValue by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        value = currValue,
        onValueChange = {currValue = it},
        modifier = modifier,
    )
}