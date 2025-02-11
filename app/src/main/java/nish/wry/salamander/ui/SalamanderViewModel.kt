package nish.wry.salamander.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import nish.wry.salamander.ui.navigation.NavigationDestination
import nish.wry.salamander.ui.screens.NishchayDestination
import nish.wry.salamander.ui.screens.SuBaseDestination
import nish.wry.salamander.ui.screens.TaskDestination

class SalamanderViewModel : ViewModel() {

    val listOfDestination: List<NavigationDestination> = listOf(
        SuBaseDestination,
        TaskDestination,
        NishchayDestination
    )


    var currentDestination: NavigationDestination by mutableStateOf(listOfDestination[1])

}