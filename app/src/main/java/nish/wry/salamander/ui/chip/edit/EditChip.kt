package nish.wry.salamander.ui.chip.edit

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.serialization.Serializable
import nish.wry.salamander.ui.AppViewModelProvider

@Serializable
data class EditChipDestination(
    val chipId: Int,
)

@Composable
fun EditChip(
    viewModel: EditChipViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    viewModel.chipId

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Text("is anything visible?", modifier = Modifier.padding(paddingValues))
    }

}